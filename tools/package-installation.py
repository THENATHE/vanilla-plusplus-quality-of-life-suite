#!/usr/bin/env python3
"""Package one verified client/server installation ZIP, with licensed dependency caches."""
import argparse
import hashlib
import io
import json
import posixpath
import re
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
            info = zipfile.ZipInfo(name, (2026, 10, 5, 0, 0, 0))
            info.compress_type = zipfile.ZIP_DEFLATED
            info.external_attr = 0o100644 << 16
            output.writestr(info, data)
    with zipfile.ZipFile(path) as result:
        assert result.testzip() is None


def verify(blob, entry):
    assert len(blob) == entry['fileSize'], entry['path']
    assert all(hashlib.new(key, blob).hexdigest() == value for key, value in entry['hashes'].items()), entry['path']


def publisher_notices(blob, prefix, output):
    """Retain licenses/notices from each unchanged dependency and nested library."""
    with zipfile.ZipFile(io.BytesIO(blob)) as jar:
        for name in jar.namelist():
            if name.endswith('/'):
                continue
            if any(word in Path(name).name.lower() for word in ('license', 'notice', 'copying')):
                output[prefix + '/' + name] = jar.read(name)
            elif name.endswith('.jar'):
                publisher_notices(jar.read(name), prefix + '/' + Path(name).stem, output)


def package(release):
    lock = json.loads(LOCK.read_text())
    version = lock['pack_version']
    release.mkdir(parents=True, exist_ok=True)
    manifest = {'formatVersion': 1, 'game': 'minecraft', 'versionId': version,
                'name': lock['name'], 'summary': 'Exact native Fabric client / Polymer server installation files.',
                'files': [{key: entry[key] for key in ('path', 'hashes', 'env', 'downloads', 'fileSize')} for entry in lock['downloads']],
                'dependencies': lock['dependencies']}
    assert len({entry['path'] for entry in manifest['files']}) == len(manifest['files'])
    for entry in manifest['files']:
        assert set(entry['hashes']) >= {'sha1', 'sha512'}
        assert all(url.startswith('https://cdn.modrinth.com/') for url in entry['downloads'])
        assert set(entry['env'].values()) <= {'required', 'optional', 'unsupported'}
    entries = {'modrinth.index.json': (json.dumps(manifest, indent=2) + '\n').encode(),
               'install.py': (ROOT / 'tools/install-pack.py').read_bytes(),
               'README.md': release_document('docs/INSTALLATION_PACK.md', version)}
    for entry in lock['overrides']:
        blob = (ROOT / entry['source']).read_bytes()
        verify(blob, entry)
        no_fzzy(blob)
        entries['overrides/' + entry['path']] = blob
    assert {entry['id'] for entry in lock['overrides']} == {'suite', 'defaulted', 'codecui'}
    suite = next(entry for entry in lock['overrides'] if entry['id'] == 'suite')
    report = json.loads((ROOT / 'docs/build-verification.json').read_text())
    assert version == report['version'] and suite['hashes']['sha256'] == report['sha256'] and report['runtime_tested']
    notices = 'overrides/suite-installation/licenses/'
    for path in (ROOT / 'licenses').glob('*.txt'):
        entries[notices + path.name] = path.read_bytes()
    for entry in lock['downloads']:
        if entry['slug'] == 'fzzy-config':
            assert entry['distribution'] == 'manifest-download'
            continue
        assert entry['distribution'] == 'embedded-cache'
        assert entry['license']['id'] in ('MIT', 'Apache-2.0', 'LGPL-3.0-only')
        blob = (ROOT / 'libs/installation' / Path(entry['path']).name).read_bytes()
        verify(blob, entry)
        no_fzzy(blob)
        entries['downloads/' + entry['path']] = blob
        publisher_notices(blob, notices + entry['slug'], entries)
    distribution = json.loads((ROOT / 'docs/dependency-distribution.lock.json').read_text())
    for entry in distribution['sources']:
        blob = (ROOT / entry['source']).read_bytes()
        assert sha(blob) == entry['sha256']
        with zipfile.ZipFile(io.BytesIO(blob)) as source:
            assert source.testzip() is None
        entries['overrides/suite-installation/dependency-sources/' + entry['file']] = blob
    for entry in distribution['notices']:
        blob = (ROOT / entry['source']).read_bytes()
        assert sha(blob) == entry['sha256']
        entries[notices + entry['file']] = blob
    for document in ('installation-pack.lock.json', 'dependency-distribution.lock.json', 'INSTALLATION_PACK.md', 'DEPENDENCIES.md'):
        entries['overrides/suite-installation/' + document] = (release_document('docs/' + document, version)
            if document.endswith('.md') else (ROOT / 'docs' / document).read_bytes())
    entries['overrides/suite-installation/THIRD_PARTY_NOTICES.md'] = release_document('THIRD_PARTY_NOTICES.md', version)
    entries['SHA256SUMS.sha256'] = ''.join(sha(blob) + '  ' + name + '\n' for name, blob in sorted(entries.items()) if name.endswith(('.jar', '.zip'))).encode()
    kit = release / (NAME + version + '.zip')
    archive(kit, entries)
    return kit


def release_document(relative, version):
    """Keep setup-directory and installed documentation links usable outside Git."""
    content = (ROOT / relative).read_text()
    base = 'https://github.com/THENATHE/vanilla-plusplus-quality-of-life-suite/blob/v' + version + '/'
    content = re.sub(r'\]\((?!https?://|#)([^)]+)\)',
        lambda match: '](' + base + posixpath.normpath(str(Path(relative).parent / match.group(1))) + ')', content)
    return content.encode()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output', type=Path)
    args = parser.parse_args()
    lock = json.loads(LOCK.read_text())
    release = args.output or ROOT.parents[1] / 'Builds/Minecraft/Vanilla++ Quality of Life Suite/Main Plugin' / lock['pack_version']
    kit = package(release)
    print(json.dumps({'archive': str(kit), 'sha256': sha(kit.read_bytes()), 'embedded_jars': 9,
                      'manifest_only_downloads': ['Fzzy Config'], 'suite_sha256': lock['overrides'][0]['hashes']['sha256']}, indent=2))


if __name__ == '__main__':
    main()
