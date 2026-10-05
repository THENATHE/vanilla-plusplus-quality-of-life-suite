#!/usr/bin/env python3
"""Stage pinned, redistributable dependency binaries and corresponding sources."""
import hashlib
import importlib.util
import json
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
spec = importlib.util.spec_from_file_location('installer', ROOT / 'tools/install-pack.py')
installer = importlib.util.module_from_spec(spec)
spec.loader.exec_module(installer)


def main():
    cache = ROOT / 'libs/installation'
    cache.mkdir(parents=True, exist_ok=True)
    lock = json.loads((ROOT / 'docs/installation-pack.lock.json').read_text())
    for entry in lock['downloads']:
        if entry['distribution'] != 'embedded-cache':
            print('Download-only, excluded:', entry['name'])
            continue
        target = cache / Path(entry['path']).name
        data = target.read_bytes() if target.exists() else installer.fetch(entry)
        installer.verify(data, entry)
        if not target.exists():
            target.write_bytes(data)
        print('Verified:', target.name)
    sources = json.loads((ROOT / 'docs/dependency-distribution.lock.json').read_text())
    for entry in sources['sources']:
        target = ROOT / entry['source']
        if target.exists():
            data = target.read_bytes()
        else:
            with urllib.request.urlopen(entry['url'], timeout=90) as response:
                data = response.read(entry['fileSize'] + 1)
        assert len(data) == entry['fileSize'] and hashlib.sha256(data).hexdigest() == entry['sha256'], entry['file']
        if not target.exists():
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(data)
        print('Verified source:', entry['file'])


if __name__ == '__main__':
    main()
