#!/usr/bin/env python3
"""Verify the installation ZIP and real disposable client/server installs."""
import argparse
import hashlib
import importlib.util
import json
import subprocess
import tempfile
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def module(name, file):
    spec = importlib.util.spec_from_file_location(name, ROOT / 'tools' / file)
    result = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(result)
    return result

installer = module('installer', 'install-pack.py')
builder = module('builder', 'package-installation.py')


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--release', type=Path, required=True)
    parser.add_argument('--client', type=Path, required=True, help='Client installed with --with-optional')
    parser.add_argument('--server', type=Path, required=True)
    args = parser.parse_args()
    lock = json.loads((ROOT / 'docs/installation-pack.lock.json').read_text())
    kit = args.release / ('vanilla-plusplus-installation-pack-' + lock['pack_version'] + '.zip')
    manifest, contents = installer.load_pack(kit)
    assert manifest['versionId'] == lock['pack_version']
    assert manifest['dependencies'] == lock['dependencies']
    assert len(manifest['files']) == 7
    cases = ['Installation ZIP integrity, safe paths and manifest identity verified']
    jars = {name: data for name, data in contents.items() if name.endswith('.jar')}
    assert len(jars) == 9
    for data in jars.values():
        builder.no_fzzy(data)
    for entry in lock['overrides']:
        installer.verify(contents['overrides/' + entry['path']], entry)
    for entry in lock['downloads']:
        if entry['distribution'] == 'embedded-cache':
            installer.verify(contents['downloads/' + entry['path']], entry)
        else:
            assert entry['slug'] == 'fzzy-config' and 'downloads/' + entry['path'] not in contents
    cases.append('Nine exact dependency/suite JARs embedded; Fzzy excluded recursively and remains official-manifest-only')
    with zipfile.ZipFile(kit) as archive:
        for line in archive.read('SHA256SUMS.sha256').decode().splitlines():
            digest, name = line.split('  ', 1)
            assert hashlib.sha256(archive.read(name)).hexdigest() == digest
        sources = json.loads((ROOT / 'docs/dependency-distribution.lock.json').read_text())
        for entry in sources['sources']:
            data = archive.read('overrides/suite-installation/dependency-sources/' + entry['file'])
            assert hashlib.sha256(data).hexdigest() == entry['sha256']
    cases.append('Embedded binary/source checksums and corresponding Cloth Config/Polymer source archives verified')
    profiles = {}
    for side, directory in [('client', args.client), ('server', args.server)]:
        selected = [entry for entry in manifest['files'] if entry['env'][side] in ('required', 'optional')]
        expected = selected + lock['overrides']
        assert {path.relative_to(directory).as_posix() for path in (directory / 'mods').glob('*.jar')} == {entry['path'] for entry in expected}
        for entry in expected:
            installer.verify((directory / entry['path']).read_bytes(), entry)
        profiles[side] = {entry['path']: entry['hashes']['sha256'] for entry in expected}
    cases.append('Real client/server installs match every hash: client includes optional Mod Menu, server Polymer; each excludes the other')
    for path in ('../escape', '/absolute', 'C:/absolute', 'mods/../escape', 'mods\\escape', ''):
        try:
            installer.safe_path(path)
            raise AssertionError('Accepted unsafe path: ' + path)
        except ValueError:
            pass
    cases.append('Traversal, absolute, Windows drive and backslash paths rejected')
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
        cases.append('Escaping destination symlink rejected')
        command = ['python3', str(ROOT / 'tools/install-pack.py'), '--pack', str(kit), '--side', 'client']
        dry = temp / 'dry'
        result = subprocess.run(command + ['--instance', str(dry), '--dry-run'], check=True, capture_output=True, text=True)
        assert 'modmenu-21.0.0.jar' not in result.stdout and 'polymer-bundled' not in result.stdout and not dry.exists()
        assert len([line for line in result.stdout.splitlines() if line.startswith('Download:')]) == 1
        cases.append('Default client dry-run excludes optional/server files, writes nothing and selects only Fzzy for download')
        broken = temp / 'broken'
        first = manifest['files'][0]
        target = broken / first['path'];target.parent.mkdir(parents=True)
        target.write_bytes(b'Existing instance data must survive')
        result = subprocess.run(command + ['--instance', str(broken)], capture_output=True, text=True)
        assert result.returncode != 0 and 'mismatch' in result.stderr
        assert target.read_bytes() == b'Existing instance data must survive' and len(list(broken.rglob('*'))) == 2
        cases.append('Mismatched existing mod aborts without changing any instance file')
        # Reject corrupt cached publisher files before dry-run or any destination write.
        cached = next(name for name in contents if name.startswith('downloads/') and name.endswith('.jar'))
        changed = bytearray(contents[cached]);changed[-1] ^= 1
        corrupted = temp / 'corrupted.zip'
        builder.archive(corrupted, {**contents, 'modrinth.index.json': json.dumps(manifest).encode(), cached: bytes(changed)})
        result = subprocess.run(['python3', str(ROOT / 'tools/install-pack.py'), '--pack', str(corrupted), '--side', 'client', '--instance', str(dry), '--dry-run'], capture_output=True, text=True)
        assert result.returncode != 0 and 'mismatch' in result.stderr and not dry.exists()
        cases.append('Same-size corrupted publisher cache rejected before installation')
        forbidden = temp / 'forbidden.zip'
        fzzy = next(entry for entry in lock['downloads'] if entry['slug'] == 'fzzy-config')
        builder.archive(forbidden, {**contents, 'modrinth.index.json': json.dumps(manifest).encode(), 'downloads/' + fzzy['path']: b'Forbidden cached data'})
        result = subprocess.run(['python3', str(ROOT / 'tools/install-pack.py'), '--pack', str(forbidden), '--side', 'client', '--instance', str(dry), '--dry-run'], capture_output=True, text=True)
        assert result.returncode != 0 and 'Unexpected cached dependency' in result.stderr and not dry.exists()
        cases.append('Manifest-only Fzzy cache injection rejected')
    sample = lock['overrides'][0]
    damaged = bytearray(contents['overrides/' + sample['path']]);damaged[-1] ^= 1
    try:
        installer.verify(damaged, sample)
        raise AssertionError('Corrupt override accepted')
    except ValueError:
        pass
    cases.append('Corrupted suite/local override rejected by checksum')
    # All existing downloads, including Fzzy, must be reused without network access.
    original_fetch = installer.fetch
    def forbidden_fetch(entry):
        raise AssertionError('Unexpected redownload of ' + entry['path'])
    installer.fetch = forbidden_fetch
    import sys
    original_argv = sys.argv
    try:
        sys.argv = ['install-pack.py', '--pack', str(kit), '--side', 'server', '--instance', str(args.server)]
        installer.main()
    finally:
        installer.fetch = original_fetch;sys.argv = original_argv
    cases.append('Exact server reinstall is idempotent and succeeds with all network fetches forbidden')
    before = hashlib.sha256(kit.read_bytes()).hexdigest()
    builder.package(args.release)
    assert hashlib.sha256(kit.read_bytes()).hexdigest() == before
    cases.append('Rebuilding installation ZIP produces byte-identical output')
    report = {'pack_version': lock['pack_version'], 'date': '2026-10-05', 'passed': True,
              'suite_sha256': lock['overrides'][0]['hashes']['sha256'],
              'archives': {kit.name: before}, 'cases': cases, 'installed_profiles': profiles,
              'launcher_gui_import_tested': False,
              'runtime_note': f"Focused installer/archive checks; exact suite also has independently recorded {lock['pack_version']} runtime checks."}
    text = json.dumps(report, indent=2) + '\n'
    (ROOT / 'docs/installation-pack-verification.json').write_text(text)
    print(text)


if __name__ == '__main__':
    main()
