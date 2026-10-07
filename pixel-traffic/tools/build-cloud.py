#!/usr/bin/env python3
"""Build this checkout with the retained update-signing key and managed proxy/CA."""
import hashlib
import os
from pathlib import Path
import subprocess
import sys
import urllib.parse

PROJECT = Path(__file__).resolve().parents[1]
KEY_SHA256 = "6e3050b987c1baa866c2c98cab3853bc4eee2fe05c6d1f778d3ad6eeece2b66c"


def android_user_home():
    configured = os.environ.get("ANDROID_USER_HOME")
    if configured:
        return Path(configured).expanduser()
    retained = Path("/workspace/toolchains/android-user")
    return retained if retained.is_dir() else PROJECT / ".local/android-user"


def main():
    if sys.argv[1:] == ["--help"]:
        print("Usage: python tools/build-cloud.py [Gradle options/tasks]\n"
              "Default tasks: :app:assembleDebug :app:lintDebug\n"
              "Requires the checkpoint debug.keystore; restore with tools/restore-debug-signing.py.\n"
              "Uses JAVA_HOME/ANDROID_HOME/ANDROID_USER_HOME/GRADLE_USER_HOME when configured.")
        return 0
    env = os.environ.copy()
    home = android_user_home()
    key = home / "debug.keystore"
    if not key.is_file() or hashlib.sha256(key.read_bytes()).hexdigest() != KEY_SHA256:
        print("Checkpoint signing key is missing or different. Restore the separate signing backup\n"
              "with tools/restore-debug-signing.py before building an update.", file=sys.stderr)
        return 2
    env["ANDROID_USER_HOME"] = str(home)
    for name, location in (("JAVA_HOME", "/workspace/toolchains/jdk-21.0.8+9"),
                           ("ANDROID_HOME", "/workspace/toolchains/android-sdk"),
                           ("GRADLE_USER_HOME", "/workspace/toolchains/gradle-home")):
        if name not in env and Path(location).is_dir():
            env[name] = location
    if "GRADLE_USER_HOME" not in env:
        env["GRADLE_USER_HOME"] = str(PROJECT / ".local/gradle-home")
    wrapper = "gradlew.bat" if os.name == "nt" else "gradlew"
    command = [str(PROJECT / wrapper), "--no-daemon"]
    proxy = urllib.parse.urlsplit(env.get("HTTPS_PROXY") or env.get("HTTP_PROXY", ""))
    if proxy.hostname:
        for protocol in ("http", "https"):
            command += [f"-D{protocol}.proxyHost={proxy.hostname}",
                        f"-D{protocol}.proxyPort={proxy.port or 80}"]
    ca = Path("/etc/ssl/certs/java/cacerts")
    if ca.is_file():
        command.append(f"-Djavax.net.ssl.trustStore={ca}")
    command += sys.argv[1:] or [":app:assembleDebug", ":app:lintDebug"]
    return subprocess.call(command, cwd=PROJECT, env=env)


if __name__ == "__main__":
    raise SystemExit(main())
