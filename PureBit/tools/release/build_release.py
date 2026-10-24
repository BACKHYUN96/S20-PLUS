#!/usr/bin/env python3
"""Existing assembleDebug signing identity, immutable release metadata, no third-party Python deps."""
import argparse
import base64
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[2]
EXPECTED_PACKAGE = "app.purebit"
EXPECTED_CERT = "a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6"
VERSION = re.compile(r"(?:0|[1-9][0-9]*)\.(?:0|[1-9][0-9]*)\.(?:0|[1-9][0-9]*)(?:-[0-9A-Za-z][0-9A-Za-z.-]*)?")


def source_version(text):
    def one(pattern):
        values = re.findall(pattern, text)
        if len(values) != 1:
            raise ValueError("Version/package must be a single explicit Gradle value")
        return values[0]
    package = one(r'\bapplicationId\s*=\s*"([^"\n]+)"')
    version = one(r'\bversionName\s*=\s*"([^"\n]+)"')
    code = int(one(r'\bversionCode\s*=\s*([0-9]+)\b'))
    if package != EXPECTED_PACKAGE or not VERSION.fullmatch(version) or code < 1:
        raise ValueError("Invalid version or unexpected app package; publication stopped")
    return {"application_id": package, "version": version, "version_code": code,
            "tag": "bitwire-v" + version, "prerelease": "-" in version}


def should_publish(event, ref, requested, tag):
    if event == "push" and ref.startswith("refs/tags/bitwire-v"):
        if ref != "refs/tags/" + tag:
            raise ValueError("Release tag must exactly match Gradle versionName: " + tag)
        return True
    if event == "workflow_dispatch" and requested.lower() == "true":
        if ref != "refs/heads/main":
            raise ValueError("Manual publication is allowed only from main")
        return True
    return False


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


def source():
    meta = source_version((ROOT / "app/build.gradle.kts").read_text())
    meta["publish"] = should_publish(os.environ.get("GITHUB_EVENT_NAME", ""), os.environ.get("GITHUB_REF", ""), os.environ.get("BITWIRE_PUBLISH", "false"), meta["tag"])
    write_json(ROOT / "ci-logs/source.json", meta)
    if os.environ.get("GITHUB_OUTPUT"):
        with open(os.environ["GITHUB_OUTPUT"], "a") as output:
            output.write(f"publish={str(meta['publish']).lower()}\ntag={meta['tag']}\n")
    print(f"BITWIRE {meta['version']} / code {meta['version_code']}; publish={meta['publish']}")


def restore_key():
    encoded = os.environ.get("BITWIRE_DEBUG_KEYSTORE_BASE64", "")
    if not encoded:
        raise ValueError("Missing Actions secret BITWIRE_DEBUG_KEYSTORE_BASE64. Refusing to generate a replacement signing identity or publish an incompatible APK.")
    try:
        data = base64.b64decode("".join(encoded.split()), validate=True)
    except ValueError:
        raise ValueError("Signing secret is not valid base64") from None
    if not 256 <= len(data) <= 1024 * 1024:
        raise ValueError("Unexpected signing material size")
    home = Path(os.environ["ANDROID_USER_HOME"]).resolve()
    if home == ROOT or ROOT in home.parents:
        raise ValueError("Signing material must be outside the source tree")
    home.mkdir(parents=True, exist_ok=True, mode=0o700)
    target = home / "debug.keystore"
    target.write_bytes(data)
    target.chmod(0o600)
    print("Existing signing material restored to the runner's temporary Android home")


def checked(command):
    return subprocess.run([str(x) for x in command], check=True, text=True, capture_output=True).stdout


def test_counts(directory):
    files = list(directory.glob("TEST-*.xml"))
    if not files:
        raise ValueError("No JVM test results; refusing to claim verification")
    counts = {name: 0 for name in ("tests", "failures", "errors", "skipped")}
    for path in files:
        root = ET.parse(path).getroot()
        for name in counts:
            counts[name] += int(root.get(name, "0"))
    if counts["tests"] == 0 or counts["failures"] or counts["errors"]:
        raise ValueError("JVM tests did not pass")
    return counts


def certificate(output):
    values = re.findall(r"Signer #\d+ certificate SHA-256 digest: ([0-9a-fA-F]{64})", output)
    if [v.lower() for v in values] != [EXPECTED_CERT]:
        raise ValueError("Signing certificate differs from installed BITWIRE APKs; publication stopped")
    return EXPECTED_CERT


def package():
    meta = json.loads((ROOT / "ci-logs/source.json").read_text())
    if not meta["publish"]:
        raise ValueError("This event cannot publish")
    for name in ("gradle", "native"):
        if not (ROOT / f"ci-logs/{name}-success.txt").is_file():
            raise ValueError("Missing successful build/native check receipt")
    tests = test_counts(ROOT / "app/build/test-results/testDebugUnitTest")
    sdk = Path(os.environ.get("ANDROID_HOME") or os.environ["ANDROID_SDK_ROOT"])
    tools = sdk / "build-tools/36.0.0"
    apk = ROOT / "app/build/outputs/apk/debug/app-debug.apk"
    cert = certificate(checked([tools / "apksigner", "verify", "--print-certs", apk]))
    checked([tools / "zipalign", "-c", "-P", "16", "4", apk])
    badging = checked([tools / "aapt2", "dump", "badging", apk])
    expected = f"package: name='{EXPECTED_PACKAGE}' versionCode='{meta['version_code']}' versionName='{meta['version']}'"
    if not badging.startswith(expected):
        raise ValueError("Built APK metadata does not match the Gradle source")
    sha = os.environ["GITHUB_SHA"]
    if not re.fullmatch(r"[0-9a-f]{40}", sha):
        raise ValueError("Expected an exact source commit SHA")
    dest = ROOT / "dist"
    dest.mkdir(exist_ok=True)
    shutil.copyfile(apk, dest / "BITWIRE.apk")
    digest = hashlib.sha256((dest / "BITWIRE.apk").read_bytes()).hexdigest()
    (dest / "BITWIRE.apk.sha256").write_text(digest + "  BITWIRE.apk\n")
    repository = os.environ["GITHUB_REPOSITORY"]
    run = f"https://github.com/{repository}/actions/runs/{os.environ['GITHUB_RUN_ID']}"
    meta.update({"commit": sha, "sha256": digest, "signer_sha256": cert,
                 "build_success": True, "checks": {"gradle": "passed", "lint": "passed", "native_transport": "passed", "jvm": tests},
                 "variant": "debug (existing installed signing identity)", "workflow_run": run})
    write_json(dest / "build-info.json", meta)
    body = f"""# BITWIRE {meta['version']}

- Build: **SUCCESS** — Gradle APK/JVM/Lint and native transport checks passed.
- Version: `{meta['version']}` / versionCode `{meta['version_code']}`
- Package: `{EXPECTED_PACKAGE}`
- Source commit: `{sha}`
- APK SHA-256: `{digest}`
- Signing certificate SHA-256: `{cert}`
- JVM tests: {tests['tests']}; failures {tests['failures']}; errors {tests['errors']}; skipped {tests['skipped']}
- APK: `BITWIRE.apk` (existing debug variant/signing identity preserved)
- [GitHub Actions build and logs]({run})

기존 앱 위에 업데이트 설치하세요. 저장 데이터 삭제/앱 제거는 필요 없습니다.
빌드 검사는 실기기 기능·시각 품질·DAC bit-perfect 검증을 뜻하지 않습니다.

Version-specific APK: https://github.com/{repository}/releases/download/{meta['tag']}/BITWIRE.apk
Latest stable APK: https://github.com/{repository}/releases/latest/download/BITWIRE.apk

`BITWIRE.apk.sha256` and `build-info.json` are attached for verification.
"""
    (dest / "release-notes.md").write_text(body, encoding="utf-8")
    print(f"Verified BITWIRE.apk: {digest}; source {sha}")


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("operation", choices=("source", "restore-key", "package"))
    args = parser.parse_args()
    try:
        {"source": source, "restore-key": restore_key, "package": package}[args.operation]()
    except (ValueError, KeyError, subprocess.CalledProcessError) as error:
        raise SystemExit(f"BITWIRE release stopped: {error}") from None
