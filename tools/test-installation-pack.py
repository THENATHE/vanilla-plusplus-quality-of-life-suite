#!/usr/bin/env python3
"""Validate installation archives and already installed disposable client/server profiles."""
import argparse
import hashlib
import importlib.util
import json
import subprocess
import tempfile
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
spec = importlib.util.spec_from_file_location('installer', ROOT / 'tools/install-pack.py')
installer = importlib.util.module_from_spec(spec)
spec.loader.exec_module(installer)
builder_spec = importlib.util.spec_from_file_location('builder', ROOT / 'tools/package-installation.py')
builder = importlib.util.module_from_spec(builder_spec)
builder_spec.loader.exec_module(builder)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--release', type=Path, required=True)
    parser.add_argument('--client', type=Path, required=True, help='Client installed with --with-optional')
    parser.add_argument('--server', type=Path, required=True)
    args = parser.parse_args()
    historical_checksums = (args.release / 'SHA256SUMS.sha256').read_bytes()
    publication = json.loads((args.release / 'PUBLICATION.json').read_text())
    original = next(entry for entry in publication['assets'] if entry['file'] == 'SHA256SUMS.sha256')
    assert len(historical_checksums) == original['bytes'] and hashlib.sha256(historical_checksums).hexdigest() == original['sha256']
    lock = json.loads((ROOT / 'docs/installation-pack.lock.json').read_text())
    stem = 'vanilla-plusplus-installation-pack-' + lock['pack_version']
    pack = args.release / (stem + '.mrpack')
    kit = args.release / (stem + '-manual.zip')
    manifest, contents = installer.load_pack(pack)
    manual_manifest, manual_contents = installer.load_pack(kit)
    assert manifest == manual_manifest and contents == manual_contents
    assert manifest['dependencies'] == {'minecraft': '26.3', 'fabric-loader': '0.19.5'}
    assert len(manifest['files']) == 7
    cases = ['Both ZIP archives pass integrity/path/duplicate validation', 'Client/server manifests and overrides are identical',
             'Minecraft 26.3 and Fabric Loader 0.19.5 selected']
    for entry in lock['overrides']:
        data = contents['overrides/' + entry['path']]
        installer.verify(data, entry)
        builder.no_fzzy(data)
    assert len([name for name in contents if name.endswith('.jar')]) == 3
    cases.append('Only exact suite, Defaulted dropfix and CodecUI JARs embedded; no nested Fzzy binary')
    profiles = {}
    for side, directory in [('client', args.client), ('server', args.server)]:
        selected = [entry for entry in manifest['files'] if entry['env'][side] in ('required', 'optional')]
        expected = selected + lock['overrides']
        assert {str(path.relative_to(directory)) for path in (directory / 'mods').glob('*.jar')} == {entry['path'] for entry in expected}
        for entry in expected:
            installer.verify((directory / entry['path']).read_bytes(), entry)
        profiles[side] = {entry['path']: entry['hashes']['sha256'] for entry in expected}
    cases.append('Real official-download installations: client includes optional Mod Menu but excludes Polymer; server includes Polymer but excludes Mod Menu; every installed hash/size matches')
    bad_paths = ('../escape', '/absolute', 'C:/absolute', 'mods/../escape', 'mods\\escape', '')
    for path in bad_paths:
        try:
            installer.safe_path(path)
            raise AssertionError('Accepted unsafe path: ' + path)
        except ValueError:
            pass
    cases.append('Traversal, absolute paths, Windows drive paths and backslash paths rejected')
    with tempfile.TemporaryDirectory(prefix='suite-pack-test-') as temporary:
        temp = Path(temporary)
        (temp / 'outside').mkdir()
        (temp / 'instance').mkdir()
        (temp / 'instance/mods').symlink_to(temp / 'outside', target_is_directory=True)
        try:
            installer.checked_target((temp / 'instance').resolve(), 'mods/escape.jar')
            raise AssertionError('Accepted escaping destination symlink')
        except ValueError:
            pass
        command = ['python3', str(ROOT / 'tools/install-pack.py'), '--pack', str(pack), '--side', 'client']
        dry = temp / 'dry'
        result = subprocess.run(command + ['--instance', str(dry), '--dry-run'], check=True, capture_output=True, text=True)
        assert 'modmenu-21.0.0.jar' not in result.stdout and 'polymer-bundled' not in result.stdout and not dry.exists()
        broken = temp / 'broken'
        first = manifest['files'][0]
        target = broken / first['path']
        target.parent.mkdir(parents=True)
        target.write_bytes(b'Existing instance data must survive')
        result = subprocess.run(command + ['--instance', str(broken)], capture_output=True, text=True)
        assert result.returncode != 0 and 'mismatch' in result.stderr
        assert target.read_bytes() == b'Existing instance data must survive'
        assert len(list(broken.rglob('*'))) == 2
    cases.extend(['Escaping destination symlink rejected', 'Client default dry-run excludes both optional Mod Menu and server Polymer without writing files',
                  'Mismatched existing mod aborts installation without changing any instance file'])
    sample = lock['overrides'][0]
    damaged = bytearray(contents['overrides/' + sample['path']])
    damaged[-1] ^= 1
    try:
        installer.verify(damaged, sample)
        raise AssertionError('Corrupt override accepted')
    except ValueError:
        pass
    cases.append('Corrupt same-size override fails checksum validation')
    subprocess.run(['python3', str(ROOT / 'tools/install-pack.py'), '--pack', str(pack), '--side', 'server', '--instance', str(args.server)],
                   check=True, capture_output=True, text=True)
    cases.append('Exact server reinstallation is idempotent and reuses verified existing downloads')
    before = {path.name: hashlib.sha256(path.read_bytes()).hexdigest() for path in (pack, kit)}
    subprocess.run(['python3', str(ROOT / 'tools/package-installation.py'), '--output', str(args.release)],
                   check=True, capture_output=True, text=True)
    assert before == {path.name: hashlib.sha256(path.read_bytes()).hexdigest() for path in (pack, kit)}
    cases.append('Rebuilding both archives from the lock produces byte-identical output')
    assert (args.release / 'SHA256SUMS.sha256').read_bytes() == historical_checksums
    cases.append('Historical SHA256SUMS.sha256 remains byte-identical to its original publication record')
    report = {'pack_version': lock['pack_version'], 'date': '2026-10-04', 'passed': True,
              'suite_sha256': lock['overrides'][0]['hashes']['sha256'],
              'archives': {path.name: hashlib.sha256(path.read_bytes()).hexdigest() for path in (pack, kit)},
              'cases': cases, 'installed_profiles': profiles, 'launcher_gui_import_tested': False,
              'historical_release_checksums_sha256': original['sha256'],
              'runtime_note': 'Mod JARs are byte-identical to the existing suite runtime-tested inputs; this task tests archive/import structure and actual helper downloads/installation, not a new gameplay run.'}
    text = json.dumps(report, indent=2) + '\n'
    (ROOT / 'docs/installation-pack-verification.json').write_text(text)
    (args.release / 'INSTALLATION_PACK_VERIFICATION.json').write_text(text)
    builder.installation_checksums(args.release, lock['pack_version'])
    checksums = (args.release / 'INSTALLATION_PACK_SHA256SUMS.sha256').read_text().splitlines()
    assert len(checksums) == 4
    for line in checksums:
        digest, filename = line.split('  ', 1)
        assert hashlib.sha256((args.release / filename).read_bytes()).hexdigest() == digest
    print(text)


if __name__ == '__main__':
    main()
