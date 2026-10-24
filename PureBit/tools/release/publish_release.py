#!/usr/bin/env python3
"""Run on GitHub Actions after the verified build; only versioned Releases distribute APKs."""
import hashlib
import json
import os
from pathlib import Path
import subprocess
import tempfile
import urllib.request

from build_release import ROOT, EXPECTED_PACKAGE, EXPECTED_CERT, VERSION, write_json


def gh(*args, optional=False):
    result = subprocess.run(["gh", *args], capture_output=True, text=True)
    if result.returncode:
        if optional and "404" in result.stderr:
            return None
        raise RuntimeError(result.stderr.strip() or "GitHub command failed")
    return result.stdout


def api(method, path, data=None, optional=False):
    args = ["api", "--method", method, path]
    if data is not None:
        # Structured JSON avoids shell interpolation of user inputs and release notes.
        result = subprocess.run(["gh", *args, "--input", "-"], input=json.dumps(data), capture_output=True, text=True)
        if result.returncode:
            raise RuntimeError(result.stderr.strip() or "GitHub API request failed")
        return json.loads(result.stdout)
    value = gh(*args, optional=optional)
    return json.loads(value) if value else None


def stable_version(version):
    if not VERSION.fullmatch(version) or "-" in version:
        raise ValueError("Not a stable semantic version")
    return tuple(map(int, version.split(".")))


def validate_bundle(directory, commit):
    meta = json.loads((directory / "build-info.json").read_text())
    if (meta.get("application_id") != EXPECTED_PACKAGE or meta.get("commit") != commit
            or not meta.get("build_success") or meta.get("signer_sha256") != EXPECTED_CERT
            or not VERSION.fullmatch(meta.get("version", ""))
            or meta.get("tag") != "bitwire-v" + meta["version"]
            or meta.get("prerelease") != ("-" in meta["version"])):
        raise ValueError("Release bundle identity/version does not match this successful workflow")
    digest = hashlib.sha256((directory / "BITWIRE.apk").read_bytes()).hexdigest()
    expected = (directory / "BITWIRE.apk.sha256").read_text().strip()
    if digest != meta.get("sha256") or expected != digest + "  BITWIRE.apk":
        raise ValueError("APK checksum mismatch")
    return meta


def tag_commit(repo, tag):
    ref = api("GET", f"repos/{repo}/git/ref/tags/{tag}", optional=True)
    if ref is None:
        return None
    obj = ref["object"]
    for _ in range(8):
        if obj["type"] == "commit":
            return obj["sha"]
        if obj["type"] != "tag":
            break
        obj = api("GET", f"repos/{repo}/git/tags/{obj['sha']}")["object"]
    raise ValueError("Release tag does not resolve to a commit")


def publish():
    repo = os.environ["GITHUB_REPOSITORY"]
    commit = os.environ["GITHUB_SHA"]
    dist = ROOT / "dist"
    meta = validate_bundle(dist, commit)
    tag = meta["tag"]
    if api("GET", f"repos/{repo}/releases/tags/{tag}", optional=True) is not None:
        raise ValueError("This version already has a release/draft. Existing versions are never overwritten; resolve an abandoned draft explicitly or increment versionName/versionCode.")
    info = api("GET", f"repos/{repo}")
    comparison = api("GET", f"repos/{repo}/compare/{commit}...{info['default_branch']}")
    if comparison["status"] not in ("identical", "ahead"):
        raise ValueError("Release source must be on the default branch history")
    existing_tag = tag_commit(repo, tag)
    if existing_tag is not None and existing_tag != commit:
        raise ValueError("Version tag points at a different source commit")
    latest = api("GET", f"repos/{repo}/releases/latest", optional=True)
    if latest and not meta["prerelease"] and latest["tag_name"].startswith("bitwire-v"):
        if stable_version(meta["version"]) <= stable_version(latest["tag_name"][9:]):
            raise ValueError("Refusing to replace latest stable with an equal/older BITWIRE version")
    release = api("POST", f"repos/{repo}/releases", {
        "tag_name": tag, "target_commitish": commit, "name": f"BITWIRE {meta['version']}",
        "body": (dist / "release-notes.md").read_text(), "draft": True,
        "prerelease": meta["prerelease"], "make_latest": "false"})
    assets = ["BITWIRE.apk", "BITWIRE.apk.sha256", "build-info.json"]
    gh("release", "upload", tag, *(str(dist / name) for name in assets), "--repo", repo)
    # Verify bytes from the draft's real assets BEFORE making it public/latest.
    with tempfile.TemporaryDirectory(prefix="bitwire-download-") as temp:
        gh("release", "download", tag, "--repo", repo, "--dir", temp)
        for name in assets:
            if (Path(temp) / name).read_bytes() != (dist / name).read_bytes():
                raise ValueError("Draft release download differs from the verified build: " + name)
    release = api("PATCH", f"repos/{repo}/releases/{release['id']}", {
        "draft": False, "prerelease": meta["prerelease"],
        "make_latest": "false" if meta["prerelease"] else "true"})
    if tag_commit(repo, tag) != commit:
        raise ValueError("Published tag source does not match the built commit")
    base = f"https://github.com/{repo}/releases"
    urls = [f"{base}/download/{tag}/BITWIRE.apk"]
    if not meta["prerelease"]:
        latest = api("GET", f"repos/{repo}/releases/latest")
        if latest["id"] != release["id"]:
            raise ValueError("GitHub latest stable does not point at this release")
        urls.append(base + "/latest/download/BITWIRE.apk")
    results = []
    if info["private"]:
        # Private repository visibility remains unchanged; downloads require GitHub authorization.
        results.append({"verification": "authenticated draft assets matched before publication", "anonymous_download": False})
    else:
        for url in urls:
            request = urllib.request.Request(url, headers={"User-Agent": "BITWIRE-release-verifier"})
            with urllib.request.urlopen(request, timeout=60) as response:
                if response.status != 200:
                    raise ValueError("Release APK HTTP status is not 200")
                data = response.read(256 * 1024 * 1024 + 1)
            digest = hashlib.sha256(data).hexdigest()
            if digest != meta["sha256"]:
                raise ValueError("Published APK download SHA-256 differs from the built APK")
            results.append({"url": url, "http_status": 200, "bytes": len(data), "sha256": digest, "anonymous_download": True})
    verification = {"release_url": release["html_url"], "commit": commit, "downloads": results}
    write_json(dist / "download-verification.json", verification)
    summary = "\n## BITWIRE published\n\n" + release["html_url"] + "\n\n" + "\n".join(urls) + "\n\nSHA-256: `" + meta["sha256"] + "`\n"
    if os.environ.get("GITHUB_STEP_SUMMARY"):
        with open(os.environ["GITHUB_STEP_SUMMARY"], "a") as stream:
            stream.write(summary)
    print(json.dumps(verification, indent=2))


if __name__ == "__main__":
    try:
        publish()
    except (ValueError, KeyError, RuntimeError) as error:
        raise SystemExit(f"BITWIRE publication stopped: {error}") from None
