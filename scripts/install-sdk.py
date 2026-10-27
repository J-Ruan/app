"""Install official Linux SDK archives, checking the repository's published SHA-1.
Only development toolchain files outside the checkout are changed. Requires Python/curl/unzip.
"""
import hashlib
import os
from pathlib import Path
import shutil
import subprocess
import tempfile
import xml.etree.ElementTree as ET

sdk = Path(os.environ.get("ANDROID_HOME", "/workspace/toolchains/android-sdk"))
sdk.mkdir(parents=True, exist_ok=True)
targets = {"platforms;android-35": "platforms/android-35", "build-tools;35.0.0": "build-tools/35.0.0", "platform-tools": "platform-tools"}
if all((sdk / t / "source.properties").exists() for t in targets.values()):
    print("SDK prerequisites already installed")
    raise SystemExit(0)
downloads = Path(os.environ.get("WARM_CYCLE_DOWNLOADS", "/workspace/downloads"))
downloads.mkdir(parents=True, exist_ok=True)
with tempfile.TemporaryDirectory(dir=downloads) as work:
    work = Path(work)
    subprocess.run(["curl", "-fsSL", "https://dl.google.com/android/repository/repository2-1.xml", "-o", str(work / "repository.xml")], check=True)
    root = ET.parse(work / "repository.xml").getroot()
    for package, target in targets.items():
        if (sdk / target / "source.properties").exists():
            continue
        matches = [p for p in root.findall("remotePackage") if p.get("path") == package]
        # For API 35 use the base platform rather than extension previews.
        p = next((p for p in matches if p.findtext("type-details/extension-level") is None), matches[-1])
        archive = next(a for a in p.findall("archives/archive") if a.findtext("host-os") in (None, "linux"))
        url = archive.findtext("complete/url")
        destination = work / "sdk.zip"
        subprocess.run(["curl", "-fsSL", "https://dl.google.com/android/repository/" + url, "-o", str(destination)], check=True)
        checksum = hashlib.sha1()
        with destination.open("rb") as f:
            for chunk in iter(lambda: f.read(1024 * 1024), b""):
                checksum.update(chunk)
        if checksum.hexdigest() != archive.findtext("complete/checksum"):
            raise RuntimeError("SDK checksum mismatch: " + package)
        extracted = work / "extracted"
        extracted.mkdir()
        subprocess.run(["unzip", "-q", str(destination), "-d", str(extracted)], check=True)
        folder = next(extracted.iterdir())
        (sdk / target).parent.mkdir(parents=True, exist_ok=True)
        shutil.move(str(folder), str(sdk / target))
        extracted.rmdir()
        print("Installed", package)
