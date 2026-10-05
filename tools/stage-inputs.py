#!/usr/bin/env python3
"""Stage only hash-matching existing inputs; never substitute a similarly named release."""
import argparse, hashlib, json, shutil, sys, zipfile
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--workspace', type=Path, help='Existing Minecraft development workspace')
parser.add_argument('--inputs', type=Path, help='Directory containing the exact pinned input JARs')
parser.add_argument('--bundle', type=Path, help='Verified released suite JAR containing original pinned feature inputs')
args = parser.parse_args()
records = json.loads((ROOT / 'locks/artifacts.json').read_text())
if args.bundle:
    release = json.loads((ROOT / 'docs/build-verification.json').read_text())
    assert hashlib.sha256(args.bundle.read_bytes()).hexdigest() == release['sha256'], 'Wrong released suite bundle'
    expected = {record['sha256']: record for record in records}
    with zipfile.ZipFile(args.bundle) as archive:
        assert archive.testzip() is None
        for entry in json.loads(archive.read('fabric.mod.json'))['jars']:
            data = archive.read(entry['file'])
            record = expected.get(hashlib.sha256(data).hexdigest())
            if record:
                target = ROOT / record['file']
                target.parent.mkdir(parents=True, exist_ok=True)
                target.write_bytes(data)
search = [ROOT / 'libs']
search += [ROOT / 'components' / name / 'upstream/artifacts' for name in ['sso','mapstitch','toolpouch','tiered-backpacks','misctweaks','simple-death-improvements']]
if args.inputs: search.append(args.inputs)
if args.workspace:
    workspace = args.workspace.resolve()
    search += [workspace / 'Minecraft/SSO-backpack-toolpouch-mapstitch-shim/libs',
               workspace / 'Minecraft/defaulted-drop-fix/build',
               workspace / 'Minecraft/polymer-shim-test-bundle/staging-2026-10-01/mods']
    search += [p for p in (workspace / 'Builds/Minecraft').glob('*/*/*') if p.is_dir()]
candidates = list(dict.fromkeys(p.resolve() for directory in search if directory.is_dir() for p in directory.rglob('*.jar')))
hashes = {hashlib.sha256(p.read_bytes()).hexdigest(): p for p in candidates}
missing = []
for record in records:
    target = ROOT / record['file']
    source = hashes.get(record['sha256'])
    if source:
        target.parent.mkdir(parents=True, exist_ok=True)
        if source != target.resolve(): shutil.copy2(source, target)
        print('Verified ' + target.name)
    elif record['id'] is not None:
        missing.append(record)
# Compile-only stdlib is already inside the pinned language-Kotlin mod.
stdlib = next(record for record in records if record['id'] is None)
kotlin = next(record for record in records if record['id'] == 'fabric-language-kotlin')
if (ROOT / kotlin['file']).is_file():
    with zipfile.ZipFile(ROOT / kotlin['file']) as archive:
        data = archive.read('META-INF/jars/kotlin-stdlib-2.4.20.jar')
    assert hashlib.sha256(data).hexdigest() == stdlib['sha256']
    (ROOT / stdlib['file']).write_bytes(data)
else:
    missing.append(stdlib)
if missing:
    print('Missing exact inputs; supply them with --inputs or --workspace:', file=sys.stderr)
    for record in missing: print(record['file'] + ' SHA256 ' + record['sha256'], file=sys.stderr)
    sys.exit(1)
print('All pinned inputs staged. Unpatched Defaulted and official SSO26.2 cannot satisfy different hashes.')
