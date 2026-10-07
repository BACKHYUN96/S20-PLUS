#!/usr/bin/env python3
"""Encrypt a ZIP to a user-supplied public key; never print signing key contents."""
import argparse
import base64
import hashlib
import json
from pathlib import Path
import xml.etree.ElementTree as ET
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.asymmetric import padding, rsa


def encrypt(public_xml, data):
    root = ET.fromstring(public_xml)
    if root.tag != "RSAKeyValue" or sorted(x.tag for x in root) != ["Exponent", "Modulus"]:
        raise ValueError("Expected only public RSA Modulus and Exponent; no private fields")
    decode = lambda name: int.from_bytes(base64.b64decode(root.findtext(name), validate=True), "big")
    key = rsa.RSAPublicNumbers(decode("Exponent"), decode("Modulus")).public_key()
    if key.key_size < 2048:
        raise ValueError("RSA key must have at least 2048 bits")
    block = (key.key_size + 7) // 8
    chunk = block - 2 * 20 - 2
    oaep = padding.OAEP(mgf=padding.MGF1(hashes.SHA1()), algorithm=hashes.SHA1(), label=None)
    ciphertext = b"".join(key.encrypt(data[i:i + chunk], oaep) for i in range(0, len(data), chunk))
    return {"format": "rsa-oaep-sha1-blocks-v1", "block_bytes": block,
            "plaintext_bytes": len(data), "plaintext_sha256": hashlib.sha256(data).hexdigest(),
            "ciphertext_base64": base64.b64encode(ciphertext).decode("ascii")}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("public_xml", type=Path)
    parser.add_argument("backup_zip", type=Path)
    parser.add_argument("output_json", type=Path)
    args = parser.parse_args()
    data = args.backup_zip.read_bytes()
    if len(data) > 32768:
        parser.error("This text-transfer helper is only for a small signing backup")
    packet = encrypt(args.public_xml.read_text(encoding="utf-8-sig"), data)
    with args.output_json.open("x", encoding="utf-8") as stream:
        stream.write(json.dumps(packet, indent=2) + "\n")
    print("Encrypted packet saved; no private key or plaintext printed.")


if __name__ == "__main__":
    main()
