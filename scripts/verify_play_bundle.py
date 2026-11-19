"""Verify a CourtQuest Play bundle's manifest, native libraries, integrity and signer."""
import argparse
import hashlib
import json
from pathlib import Path
import re
import subprocess
import tempfile
from xml.etree import ElementTree as ET
import zipfile

ANDROID = "{http://schemas.android.com/apk/res/android}"
PACKAGE = "com.courtquestpro"
PHONE_TEST_CERT = "25f8007f0a8503b01ded3f4856d02e830c64c823aa2c816da4d347ed5015f27b"


def output(*args):
    return subprocess.check_output(args, text=True, stderr=subprocess.STDOUT)


def check(condition, message):
    if not condition:
        raise SystemExit(message)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--bundle", type=Path, required=True)
    parser.add_argument("--bundletool", type=Path, required=True)
    parser.add_argument("--report", type=Path, required=True)
    parser.add_argument("--version-code", required=True)
    signing = parser.add_mutually_exclusive_group(required=True)
    signing.add_argument("--expected-upload-sha256")
    signing.add_argument("--unsigned", action="store_true")
    args = parser.parse_args()
    tool = ("java", "-jar", str(args.bundletool))
    path = f"--bundle={args.bundle}"
    output(*tool, "validate", path)
    manifest = ET.fromstring(output(*tool, "dump", "manifest", path, "--module=base"))
    application = manifest.find("application")
    check(application is not None, "Bundle has no application manifest")
    check(manifest.get("package") == PACKAGE, "Unexpected package ID")
    check(manifest.get(f"{ANDROID}versionCode") == args.version_code, "Unexpected version code")
    sdk = manifest.find("uses-sdk")
    check(sdk is not None and sdk.get(f"{ANDROID}minSdkVersion") == "26", "Unexpected minimum SDK")
    check(int(sdk.get(f"{ANDROID}targetSdkVersion", "0")) >= 36, "Target SDK below 36")
    check(application.get(f"{ANDROID}debuggable") != "true", "Bundle is debuggable")
    check(application.get(f"{ANDROID}allowBackup") == "false", "Device backup must be disabled")
    check(application.get(f"{ANDROID}usesCleartextTraffic") == "false", "Cleartext traffic must be disabled")
    permissions = sorted(tag.get(f"{ANDROID}name", "") for tag in manifest.findall("uses-permission"))
    check(set(permissions) <= {f"{PACKAGE}.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION"},
          f"Unexpected permissions: {permissions}")
    exported = [node.get(f"{ANDROID}name") for node in application if node.get(f"{ANDROID}exported") == "true"]
    check(exported == ["com.courtquestpro.MainActivity"], f"Unexpected exported components: {exported}")
    with zipfile.ZipFile(args.bundle) as archive, tempfile.TemporaryDirectory() as directory:
        check(archive.testzip() is None, "Bundle ZIP integrity failure")
        libraries = sorted(name for name in archive.namelist() if re.fullmatch(r"base/lib/[^/]+/[^/]+\.so", name))
        abis = {name.split("/")[2] for name in libraries}
        for compat, primary in (("armeabi-v7a", "arm64-v8a"), ("x86", "x86_64")):
            check(compat not in abis or primary in abis, f"Missing 64-bit counterpart for {compat}")
        if libraries:
            check("PAGE_ALIGNMENT_16K" in output(*tool, "dump", "config", path), "Bundle lacks 16 KB alignment")
        for name in libraries:
            elf = Path(directory) / "lib.so"
            elf.write_bytes(archive.read(name))
            loads = [line for line in output("readelf", "-lW", str(elf)).splitlines()
                     if line.strip().startswith("LOAD ")]
            check(bool(loads) and all(int(line.split()[-1], 16) >= 16384 for line in loads),
                  f"Native library has a LOAD segment under 16 KB: {name}")
        has_signature = any(re.fullmatch(r"META-INF/[^/]+\.(RSA|DSA|EC)", name, flags=re.IGNORECASE)
                            for name in archive.namelist())
    cert = None
    if args.unsigned:
        check(not has_signature, "Expected structural release candidate without a signer")
    else:
        check(has_signature, "Missing AAB upload signature")
        expected = args.expected_upload_sha256.replace(":", "").lower().strip()
        check(re.fullmatch("[0-9a-f]{64}", expected) is not None, "Invalid upload certificate fingerprint")
        check(expected != PHONE_TEST_CERT, "CourtQuest development signer is prohibited for Play")
        verification = output("jarsigner", "-verify", "-verbose", str(args.bundle))
        check("jar verified." in verification and "unsigned entries" not in verification, "Bundle signature failed")
        receipt = output("keytool", "-printcert", "-jarfile", str(args.bundle))
        check("CN=Android Debug" not in receipt, "Android debug certificates are prohibited for Play")
        matches = re.findall(r"SHA256:\s+([0-9A-F:]+)", receipt)
        cert = matches[0].replace(":", "").lower() if matches else ""
        check(receipt.count("Signer #") == 1 and cert == expected, "Play upload certificate fingerprint mismatch")
    result = dict(package=PACKAGE, versionCode=int(args.version_code), versionName=manifest.get(f"{ANDROID}versionName"),
                  minSdk=int(sdk.get(f"{ANDROID}minSdkVersion")), targetSdk=int(sdk.get(f"{ANDROID}targetSdkVersion")),
                  sizeBytes=args.bundle.stat().st_size, sha256=hashlib.sha256(args.bundle.read_bytes()).hexdigest(),
                  uploadCertificateSha256=cert, permissions=permissions, exportedComponents=exported,
                  abis=sorted(abis), nativeLibraries=libraries, bundletoolValidation="PASS",
                  signing="VERIFIED_UPLOAD_CERTIFICATE" if cert else "UNSIGNED_NOT_UPLOADABLE")
    args.report.write_text(json.dumps(result, indent=2) + "\n")
    print(json.dumps(result, indent=2))


if __name__ == "__main__":
    main()
