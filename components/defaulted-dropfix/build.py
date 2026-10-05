#!/usr/bin/env python3
"""Reproducible, hash-pinned patch of the retained Defaulted 26.3 artifact."""
import hashlib
import json
import os
from pathlib import Path
import subprocess
import zipfile

ROOT = Path(__file__).resolve().parent
VERSION = '1.3.8+26.3.dropfix.1'
EXPECTED = '0f6efc6423902b83da78ce85a119c69536517b1a9af52ed7046858859ff582a0'
TARGET = 'net/atlas/defaulted/utils/ReferentialDataComponentMap.class'
source = ROOT / 'inputs/defaulted-1.3.8.release-26.3-fabric.jar'
assert hashlib.sha256(source.read_bytes()).hexdigest() == EXPECTED, 'Wrong input artifact'
build = ROOT / 'build'
build.mkdir(exist_ok=True)
asm_root = Path(os.environ.get('GRADLE_USER_HOME', Path.home() / '.gradle')) / 'caches/modules-2/files-2.1/org.ow2.asm'
asm = [next((asm_root / artifact / '9.10.1').glob('*/*.jar')) for artifact in ['asm', 'asm-tree']]
expected_asm = ['ed825d10ab1399c8c0cb669e688cf0c8c82629b4c8399b58352b68e92ca10fcb',
                '3dfb0d5b6a106cd40b5b250e39935fbf2f927f4477546a5369a3ac609cf0506b']
for dependency, digest in zip(asm, expected_asm):
    assert hashlib.sha256(dependency.read_bytes()).hexdigest() == digest, 'Wrong build dependency'
classpath = os.pathsep.join(map(str, asm))
subprocess.run(['javac', '--release', '25', '-cp', classpath, '-d', str(build),
                str(ROOT / 'tools/PatchDefaulted.java')], check=True)
with zipfile.ZipFile(source) as archive:
    (build / 'original.class').write_bytes(archive.read(TARGET))
subprocess.run(['java', '-cp', str(build) + os.pathsep + classpath, 'PatchDefaulted',
                str(build / 'original.class'), str(build / 'patched.class')], check=True)
output = build / f'defaulted-{VERSION}-fabric.jar'
with zipfile.ZipFile(source) as original, zipfile.ZipFile(output, 'w') as patched:
    for info in original.infolist():
        data = original.read(info.filename)
        if info.filename == TARGET:
            data = (build / 'patched.class').read_bytes()
        elif info.filename == 'fabric.mod.json':
            metadata = json.loads(data)
            metadata['version'] = VERSION
            data = json.dumps(metadata, ensure_ascii=False, separators=(',', ':')).encode()
        patched.writestr(info, data)
with zipfile.ZipFile(source) as original, zipfile.ZipFile(output) as patched:
    assert patched.testzip() is None
    assert original.namelist() == patched.namelist()
    changes = [n for n in original.namelist() if original.read(n) != patched.read(n)]
    assert set(changes) == {TARGET, 'fabric.mod.json'}, changes
    before, after = json.loads(original.read('fabric.mod.json')), json.loads(patched.read('fabric.mod.json'))
    after['version'] = before['version']
    assert before == after, 'Unexpected metadata change'
    unchanged_count = len(original.namelist()) - len(changes)
report = {'version': VERSION, 'input_sha256': EXPECTED,
          'output_sha256': hashlib.sha256(output.read_bytes()).hexdigest(),
          'changed_entries': changes, 'unchanged_entry_count': unchanged_count,
          'build_dependencies': {str(p): hashlib.sha256(p.read_bytes()).hexdigest() for p in asm}}
(build / 'build-report.json').write_text(json.dumps(report, indent=2) + '\n')
print(output)
print(json.dumps(report, indent=2))
