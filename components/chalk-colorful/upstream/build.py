#!/usr/bin/env python3
"""Build the metadata-only addon deterministically; no compiler or downloads needed."""
import hashlib
import json
from pathlib import Path
from zipfile import ZipFile, ZipInfo, ZIP_DEFLATED

ROOT = Path(__file__).resolve().parent
SOURCE = ROOT / 'ported/src/main/resources'
ORIGINAL = ROOT / 'upstream/chalk-colorful-addon-2.1.1.jar'
ORIGINAL_SHA256 = 'bf184e44ceb222e0831912acc2e26a17b7358e93162b60776dd646cf3780bbef'


def main():
    assert hashlib.sha256(ORIGINAL.read_bytes()).hexdigest() == ORIGINAL_SHA256
    metadata = json.loads((SOURCE / 'fabric.mod.json').read_text())
    assert metadata['id'] == 'chalk-colorful-addon'
    assert metadata['depends']['minecraft'] == '26.3'
    assert metadata['depends']['chalk'] == '=3.2.1+26.3'
    assert not metadata['entrypoints'] and not metadata['mixins']
    files = {str(p.relative_to(SOURCE)): p.read_bytes() for p in sorted(SOURCE.rglob('*')) if p.is_file()}
    with ZipFile(ORIGINAL) as original:
        assert not any(n.endswith('.class') for n in original.namelist())
        for name in ('LICENSE_chalk-colorful-addon', 'assets/chalk-colorful-addon/icon.png'):
            assert files[name] == original.read(name), f'Changed upstream asset: {name}'
    files['META-INF/MANIFEST.MF'] = b'Manifest-Version: 1.0\r\n\r\n'
    output = ROOT / 'ported/build/libs' / f"chalk-colorful-addon-{metadata['version']}.jar"
    output.parent.mkdir(parents=True, exist_ok=True)
    with ZipFile(output, 'w', compression=ZIP_DEFLATED, compresslevel=9) as jar:
        for name, data in sorted(files.items()):
            info = ZipInfo(name, date_time=(2026, 10, 3, 0, 0, 0))
            info.compress_type = ZIP_DEFLATED
            info.external_attr = 0o100644 << 16
            jar.writestr(info, data, compresslevel=9)
    with ZipFile(output) as jar:
        assert jar.testzip() is None
        assert json.loads(jar.read('fabric.mod.json')) == metadata
        assert not any(n.endswith('.class') for n in jar.namelist())
    print(f'{hashlib.sha256(output.read_bytes()).hexdigest()}  {output}')


if __name__ == '__main__':
    main()
