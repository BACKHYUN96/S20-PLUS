#!/usr/bin/env python3
"""One-time administrator setup. Pipe the existing key directly to GitHub's encrypted Secret API."""
import argparse
import base64
import re
import subprocess
from pathlib import Path

from build_release import EXPECTED_CERT


def register(path, repo):
    path = Path(path)
    # Existing Gradle debug alias/password; never generate or rewrite this keystore.
    info = subprocess.run(["keytool", "-list", "-v", "-keystore", str(path), "-storepass", "android", "-alias", "androiddebugkey"], check=True, capture_output=True, text=True).stdout
    values = re.findall(r"SHA256:\s*([A-Fa-f0-9:]{95})", info)
    if len(values) != 1 or values[0].replace(":", "").lower() != EXPECTED_CERT:
        raise ValueError("Keystore certificate is not the existing BITWIRE identity")
    # gh encrypts using the repository's public key. No key, token or base64 is printed or saved.
    subprocess.run(["gh", "secret", "set", "BITWIRE_DEBUG_KEYSTORE_BASE64", "--repo", repo],
                   input=base64.b64encode(path.read_bytes()), check=True)
    print("Existing BITWIRE key registered as an encrypted Actions Secret; source files unchanged.")


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--keystore", required=True, type=Path)
    parser.add_argument("--repo", default="BACKHYUN96/S20-PLUS")
    args = parser.parse_args()
    try:
        register(args.keystore, args.repo)
    except (ValueError, OSError, subprocess.CalledProcessError) as error:
        raise SystemExit(f"Signing Secret registration stopped: {error}") from None
