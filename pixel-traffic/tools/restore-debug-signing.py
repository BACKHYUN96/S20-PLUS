#!/usr/bin/env python3
"""Restore only the verified checkpoint debug key, outside tracked project files."""
import argparse
import hashlib
from pathlib import Path
import zipfile

from importlib.util import module_from_spec, spec_from_file_location

spec = spec_from_file_location("cloud_build", Path(__file__).with_name("build-cloud.py"))
build = module_from_spec(spec)
spec.loader.exec_module(build)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("backup", type=Path, help="Separate signing ZIP or original debug.keystore")
    args = parser.parse_args()
    if args.backup.suffix.lower() == ".zip":
        with zipfile.ZipFile(args.backup) as archive:
            info = archive.getinfo("debug.keystore")
            if info.file_size > 32768:
                parser.error("Unexpected key size")
            content = archive.read(info)
    else:
        if args.backup.stat().st_size > 32768:
            parser.error("Unexpected key size")
        content = args.backup.read_bytes()
    if hashlib.sha256(content).hexdigest() != build.KEY_SHA256:
        parser.error("This is not the verified 0.29.0 checkpoint signing key")
    target = build.android_user_home() / "debug.keystore"
    if target.exists():
        if hashlib.sha256(target.read_bytes()).hexdigest() == build.KEY_SHA256:
            print("PASS: checkpoint signing key already restored")
            return
        parser.error("Target contains another key; use a new ANDROID_USER_HOME directory")
    target.parent.mkdir(parents=True, exist_ok=True)
    with target.open("xb") as output:
        target.chmod(0o600)
        output.write(content)
    print("PASS: verified checkpoint signing key restored")


if __name__ == "__main__":
    main()
