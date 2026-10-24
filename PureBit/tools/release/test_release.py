import hashlib
import json
import os
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch
import base64
import subprocess
from register_signing_secret import register

from build_release import EXPECTED_CERT, EXPECTED_PACKAGE, certificate, source_version, should_publish, restore_key, test_counts
from publish_release import validate_bundle, stable_version


class ReleaseTests(unittest.TestCase):
    def test_source_matches_the_real_application_without_changing_signing(self):
        config = (Path(__file__).resolve().parents[2] / "app/build.gradle.kts").read_text()
        result = source_version(config)
        self.assertEqual(EXPECTED_PACKAGE, result["application_id"])
        self.assertEqual("bitwire-v" + result["version"], result["tag"])

    def test_only_explicit_manual_main_or_matching_release_tag_can_publish(self):
        tag = "bitwire-v1.10.0"
        self.assertFalse(should_publish("push", "refs/heads/main", "true", tag))
        self.assertFalse(should_publish("pull_request", "refs/pull/1/merge", "true", tag))
        self.assertFalse(should_publish("workflow_dispatch", "refs/heads/main", "false", tag))
        self.assertTrue(should_publish("workflow_dispatch", "refs/heads/main", "true", tag))
        self.assertTrue(should_publish("push", "refs/tags/" + tag, "false", tag))
        with self.assertRaises(ValueError):
            should_publish("push", "refs/tags/bitwire-v9.9.9", "false", tag)
        with self.assertRaises(ValueError):
            should_publish("workflow_dispatch", "refs/heads/unreviewed", "true", tag)

    def test_invalid_or_injected_version_cannot_become_a_release_tag(self):
        template = 'applicationId = "app.purebit"\nversionName = "%s"\nversionCode = 47'
        for version in ("1.10.0; echo hello", "$(id)", "../main", "1.2", "01.2.3"):
            with self.assertRaises(ValueError):
                source_version(template % version)
        self.assertTrue(source_version(template % "1.11.0-rc.1")["prerelease"])
        self.assertGreater(stable_version("1.10.0"), stable_version("1.9.0"))
        with self.assertRaises(ValueError):
            stable_version("1.11.0-rc.1")

    def test_wrong_signer_or_multiple_signers_cannot_be_published(self):
        self.assertEqual(EXPECTED_CERT, certificate("Signer #1 certificate SHA-256 digest: " + EXPECTED_CERT))
        for output in ("unsigned", "Signer #1 certificate SHA-256 digest: " + "0" * 64,
                       "Signer #1 certificate SHA-256 digest: " + EXPECTED_CERT + "\nSigner #2 certificate SHA-256 digest: " + EXPECTED_CERT):
            with self.assertRaises(ValueError):
                certificate(output)

    def test_missing_key_fails_without_generating_or_replacing_a_key(self):
        with tempfile.TemporaryDirectory() as temp:
            home = Path(temp)
            with patch.dict(os.environ, {"ANDROID_USER_HOME": str(home), "BITWIRE_DEBUG_KEYSTORE_BASE64": ""}):
                with self.assertRaises(ValueError):
                    restore_key()
            self.assertFalse((home / "debug.keystore").exists())

    def test_bundle_rejects_changed_apk_and_different_commit(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            data = b"APK test fixture - not a signing key"
            digest = hashlib.sha256(data).hexdigest()
            meta = {"application_id": EXPECTED_PACKAGE, "commit": "a" * 40, "build_success": True,
                    "signer_sha256": EXPECTED_CERT, "version": "1.10.0", "tag": "bitwire-v1.10.0", "prerelease": False, "sha256": digest}
            (root / "BITWIRE.apk").write_bytes(data)
            (root / "BITWIRE.apk.sha256").write_text(digest + "  BITWIRE.apk\n")
            (root / "build-info.json").write_text(json.dumps(meta))
            self.assertEqual(digest, validate_bundle(root, "a" * 40)["sha256"])
            with self.assertRaises(ValueError):
                validate_bundle(root, "b" * 40)
            (root / "BITWIRE.apk").write_bytes(data + b"changed")
            with self.assertRaises(ValueError):
                validate_bundle(root, "a" * 40)

    def test_secret_registration_checks_identity_and_sends_value_only_on_stdin(self):
        with tempfile.TemporaryDirectory() as temp:
            key = Path(temp) / "fixture.keystore"
            data = b"synthetic signing fixture, never a real private key"
            key.write_bytes(data)
            cert = ":".join(EXPECTED_CERT[i:i+2] for i in range(0,64,2))
            with patch("register_signing_secret.subprocess.run") as run, patch("builtins.print"):
                run.return_value = subprocess.CompletedProcess([],0,stdout="SHA256: " + cert)
                register(key,"BACKHYUN96/S20-PLUS")
                self.assertEqual(2,run.call_count)
                command = run.call_args.args[0]
                self.assertIn("BITWIRE_DEBUG_KEYSTORE_BASE64",command)
                self.assertNotIn("--body",command)
                self.assertEqual(base64.b64encode(data),run.call_args.kwargs["input"])
            self.assertEqual(data,key.read_bytes())

    def test_secret_registration_refuses_an_unrelated_key_before_github_write(self):
        with patch("register_signing_secret.subprocess.run") as run:
            run.return_value = subprocess.CompletedProcess([],0,stdout="SHA256: " + ":".join(["00"]*32))
            with self.assertRaises(ValueError):
                register("unused-fixture.keystore","BACKHYUN96/S20-PLUS")
            self.assertEqual(1,run.call_count)

    def test_absent_or_failed_jvm_results_cannot_claim_build_verification(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            with self.assertRaises(ValueError):
                test_counts(root)
            path = root / "TEST-fixture.xml"
            path.write_text('<testsuite tests="2" failures="1" errors="0" skipped="0"/>')
            with self.assertRaises(ValueError):
                test_counts(root)
            path.write_text('<testsuite tests="2" failures="0" errors="0" skipped="0"/>')
            self.assertEqual(2, test_counts(root)["tests"])


if __name__ == "__main__":
    unittest.main()
