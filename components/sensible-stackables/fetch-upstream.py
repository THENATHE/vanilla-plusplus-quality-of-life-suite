#!/usr/bin/env python3
"""Restore pinned, unmodified Modrinth artifacts for the independent 26.2 baseline."""
from pathlib import Path
import hashlib
import json
import urllib.parse
import urllib.request

root = Path(__file__).resolve().parent
provenance = json.loads((root / 'upstream/metadata/provenance.json').read_text())
output = root / 'upstream/artifacts'
output.mkdir(exist_ok=True)
for item in provenance['artifacts']:
    path = output / item['file']
    if path.exists():
        data = path.read_bytes()
    else:
        name = urllib.parse.quote(item['file'], safe='')
        url = f"https://cdn.modrinth.com/data/5lQcMjaC/versions/{provenance['modrinth_version_id']}/{name}"
        request = urllib.request.Request(url, headers={'User-Agent': 'THENATHE-suite-provenance/1.0'})
        with urllib.request.urlopen(request, timeout=60) as response:
            data = response.read()
    for algorithm in ('sha256', 'sha512'):
        actual = hashlib.new(algorithm, data).hexdigest()
        if actual != item[algorithm]:
            raise SystemExit(f"Refusing changed upstream artifact {item['file']}: {algorithm} mismatch")
    if not path.exists():
        path.write_bytes(data)
    print(f"Verified {item['file']} {item['sha256']}")
