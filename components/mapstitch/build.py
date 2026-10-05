#!/usr/bin/env python3
"""Verify and stage the pinned unchanged official Fabric component."""
import hashlib, json, shutil, zipfile
from pathlib import Path
root = Path(__file__).resolve().parent
suite = root.parents[1]
lock = json.loads((root / "inputs.lock.json").read_text())
source = suite / lock["binary"]
assert hashlib.sha256(source.read_bytes()).hexdigest() == lock["binary_sha256"], "Official binary hash mismatch"
with zipfile.ZipFile(source) as archive:
    assert archive.testzip() is None
    metadata = json.loads(archive.read("fabric.mod.json"))
    assert metadata["id"] == lock["mod_id"]
    assert metadata["version"] == lock["artifact_version"]
    for entry in metadata.get("jars", []):
        assert entry["file"] in archive.namelist()
destination = root / "build" / "developer" / source.name
destination.parent.mkdir(parents=True, exist_ok=True)
shutil.copyfile(source, destination)
print(destination)
