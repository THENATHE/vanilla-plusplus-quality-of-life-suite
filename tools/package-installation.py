#!/usr/bin/env python3
"""Package the unchanged verified suite as a manifest-based client/server installation kit."""
import argparse
import hashlib
import io
import json
import shutil
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
LOCK = ROOT / 'docs/installation-pack.lock.json'
NAME = 'vanilla-plusplus-installation-pack-'


def sha(data):
    return hashlib.sha256(data).hexdigest()


def no_fzzy(data):
    with zipfile.ZipFile(io.BytesIO(data)) as jar:
        if 'fabric.mod.json' in jar.namelist():
            assert json.loads(jar.read('fabric.mod.json')).get('id') != 'fzzy_config', 'Fzzy must remain a manifest download'
        for name in jar.namelist():
            if name.endswith('.jar'):
                no_fzzy(jar.read(name))


def archive(path, entries):
    with zipfile.ZipFile(path, 'w', zipfile.ZIP_DEFLATED, compresslevel=9) as output:
        for name, data in sorted(entries.items()):
            info = zipfile.ZipInfo(name, (2026, 10, 4, 0, 0, 0))
            info.compress_type = zipfile.ZIP_DEFLATED
            info.external_attr = 0o100644 << 16
            output.writestr(info, data)
    with zipfile.ZipFile(path) as result:
        assert result.testzip() is None


def installation_checksums(release, version):
    names = (NAME + version + '.mrpack', NAME + version + '-manual.zip',
             'INSTALLATION_PACK.md', 'INSTALLATION_PACK_VERIFICATION.json')
    files = [release / name for name in names if (release / name).is_file()]
    (release / 'INSTALLATION_PACK_SHA256SUMS.sha256').write_text(
        ''.join(sha(path.read_bytes()) + '  ' + path.name + '\n' for path in files))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output', type=Path)
    args = parser.parse_args()
    lock = json.loads(LOCK.read_text())
    version = lock['pack_version']
    release = args.output or ROOT.parents[1] / 'Builds/Minecraft/Vanilla++ Quality of Life Suite/Main Plugin - Merged Experiments' / version
    release.mkdir(parents=True, exist_ok=True)
    manifest = {'formatVersion': 1, 'game': 'minecraft', 'versionId': version,
                'name': lock['name'], 'summary': 'Native Fabric client and Polymer-enabled dedicated server installation; exact merged testing suite files.',
                'files': [{key: entry[key] for key in ('path', 'hashes', 'env', 'downloads', 'fileSize')} for entry in lock['downloads']],
                'dependencies': lock['dependencies']}
    assert len({entry['path'] for entry in manifest['files']}) == len(manifest['files'])
    for entry in manifest['files']:
        assert set(entry['hashes']) >= {'sha1', 'sha512'}
        assert all(url.startswith('https://cdn.modrinth.com/') for url in entry['downloads'])
        assert set(entry['env'].values()) <= {'required', 'optional', 'unsupported'}
    fzzy = next(entry for entry in lock['downloads'] if entry['slug'] == 'fzzy-config')
    assert fzzy['version_id'] == 'thw1Z19c'
    entries = {'modrinth.index.json': (json.dumps(manifest, indent=2) + '\n').encode()}
    for entry in lock['overrides']:
        blob = (ROOT / entry['source']).read_bytes()
        assert len(blob) == entry['fileSize'] and all(hashlib.new(key, blob).hexdigest() == value for key, value in entry['hashes'].items())
        no_fzzy(blob)
        entries['overrides/' + entry['path']] = blob
    assert {entry['id'] for entry in lock['overrides']} == {'suite', 'defaulted', 'codecui'}
    suite = next(entry for entry in lock['overrides'] if entry['id'] == 'suite')
    report = json.loads((ROOT / 'docs/build-verification.json').read_text())
    assert suite['hashes']['sha256'] == report['sha256'] and report['runtime_tested']
    for path in (ROOT / 'licenses').glob('*.txt'):
        entries['overrides/suite-installation/licenses/' + path.name] = path.read_bytes()
    entries['overrides/suite-installation/installation-pack.lock.json'] = LOCK.read_bytes()
    entries['overrides/suite-installation/INSTALLATION_PACK.md'] = (ROOT / 'docs/INSTALLATION_PACK.md').read_bytes()
    entries['overrides/suite-installation/THIRD_PARTY_NOTICES.md'] = (ROOT / 'THIRD_PARTY_NOTICES.md').read_bytes()
    pack = release / (NAME + version + '.mrpack')
    archive(pack, entries)
    kit = release / (NAME + version + '-manual.zip')
    archive(kit, {**entries, 'install.py': (ROOT / 'tools/install-pack.py').read_bytes(),
                  'README.md': (ROOT / 'docs/INSTALLATION_PACK.md').read_bytes()})
    shutil.copy2(ROOT / 'docs/INSTALLATION_PACK.md', release / 'INSTALLATION_PACK.md')
    # SHA256SUMS.sha256 belongs to the historical mod publication. Keep those
    # bytes unchanged; installation assets have an independent checksum record.
    installation_checksums(release, version)
    print(json.dumps({'suite_sha256': suite['hashes']['sha256'], 'embedded_jars': [entry['path'] for entry in lock['overrides']],
                      'manifest_downloads': len(manifest['files']), 'artifacts': {path.name: sha(path.read_bytes()) for path in (pack, kit)}}, indent=2))


if __name__ == '__main__':
    main()
