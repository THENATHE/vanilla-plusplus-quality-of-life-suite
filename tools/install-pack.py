#!/usr/bin/env python3
"""Install pinned mod files from this pack; never install Java, Minecraft, or accept an EULA."""
import argparse
import hashlib
import json
import tempfile
import urllib.parse
import urllib.request
import zipfile
from pathlib import Path, PurePosixPath

USER_AGENT = 'THENATHE-suite-install-pack/1.0.1 (https://github.com/THENATHE/vanilla-plusplus-quality-of-life-suite)'


def safe_path(value):
    path = PurePosixPath(value)
    if not value or '\\' in value or ':' in value or path.is_absolute() or '..' in path.parts or str(path) != value:
        raise ValueError('Unsafe pack path: ' + value)
    return path


def checked_target(instance, relative):
    path = instance.joinpath(*safe_path(relative).parts)
    if not path.resolve().is_relative_to(instance):
        raise ValueError('Destination escapes the instance through a symlink: ' + relative)
    return path


def verify(data, entry):
    if len(data) != entry['fileSize']:
        raise ValueError('File size mismatch: ' + entry['path'])
    for algorithm in ('sha1', 'sha512'):
        if hashlib.new(algorithm, data).hexdigest() != entry['hashes'][algorithm]:
            raise ValueError(algorithm + ' mismatch: ' + entry['path'])
    if 'sha256' in entry['hashes'] and hashlib.sha256(data).hexdigest() != entry['hashes']['sha256']:
        raise ValueError('sha256 mismatch: ' + entry['path'])


def load_pack(location):
    if location.is_dir():
        manifest = json.loads((location / 'modrinth.index.json').read_text(encoding='utf-8'))
        contents = {}
        for folder in ('overrides', 'client-overrides', 'server-overrides'):
            for path in (location / folder).rglob('*'):
                if path.is_symlink():
                    raise ValueError('Symlink in pack overrides: ' + str(path))
                if path.is_file():
                    contents[path.relative_to(location).as_posix()] = path.read_bytes()
    else:
        with zipfile.ZipFile(location) as archive:
            if archive.testzip() is not None or len(archive.namelist()) != len(set(archive.namelist())):
                raise ValueError('Corrupt archive or duplicate ZIP paths')
            for name in archive.namelist():
                if not name.endswith('/'):
                    safe_path(name)
            manifest = json.loads(archive.read('modrinth.index.json'))
            contents = {name: archive.read(name) for name in archive.namelist()
                        if not name.endswith('/') and name.startswith(('overrides/', 'client-overrides/', 'server-overrides/'))}
    if manifest['formatVersion'] != 1 or manifest['game'] != 'minecraft':
        raise ValueError('Unsupported modpack format')
    return manifest, contents


def fetch(entry):
    errors = []
    for url in entry['downloads']:
        parsed = urllib.parse.urlparse(url)
        if parsed.scheme != 'https' or parsed.hostname != 'cdn.modrinth.com':
            raise ValueError('This installation pack only downloads from official cdn.modrinth.com URLs')
        try:
            request = urllib.request.Request(url, headers={'User-Agent': USER_AGENT})
            with urllib.request.urlopen(request, timeout=90) as response:
                final = urllib.parse.urlparse(response.url)
                if final.scheme != 'https' or final.hostname != 'cdn.modrinth.com':
                    raise ValueError('Download redirected outside the official Modrinth CDN')
                data = response.read(entry['fileSize'] + 1)
            verify(data, entry)
            return data
        except (OSError, ValueError) as error:
            errors.append(str(error))
    raise ValueError('Could not download ' + entry['path'] + ': ' + '; '.join(errors))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--pack', type=Path, default=Path(__file__).resolve().parent,
                        help='A .mrpack file or extracted installation kit directory')
    parser.add_argument('--side', choices=('client', 'server'), required=True)
    parser.add_argument('--instance', type=Path, required=True, help='Prefer a new Minecraft/Fabric instance directory')
    parser.add_argument('--with-optional', action='store_true', help='Include optional files for the selected side (Mod Menu on client)')
    parser.add_argument('--dry-run', action='store_true', help='Show selected files without downloads or writes')
    args = parser.parse_args()
    manifest, contents = load_pack(args.pack.resolve())
    instance = args.instance.resolve()
    selected = []
    paths = set()
    for entry in manifest['files']:
        safe_path(entry['path'])
        if entry['path'] in paths:
            raise ValueError('Duplicate manifest path: ' + entry['path'])
        paths.add(entry['path'])
        status = entry.get('env', {}).get(args.side, 'required')
        if status not in ('required', 'optional', 'unsupported'):
            raise ValueError('Unknown environment status: ' + str(status))
        if status == 'required' or (status == 'optional' and args.with_optional):
            selected.append(entry)
    overrides = {}
    for folder in ('overrides/', args.side + '-overrides/'):
        for name, data in sorted(contents.items()):
            if name.startswith(folder):
                relative = str(safe_path(name[len(folder):]))
                if relative in paths:
                    raise ValueError('Override masks a manifest download: ' + relative)
                overrides[relative] = data
    # Verify the included local JARs against the pack's recorded byte hashes.
    lock = json.loads(overrides['suite-installation/installation-pack.lock.json'])
    allowed_jars = {entry['path'] for entry in lock['overrides']}
    if {name for name in overrides if name.endswith('.jar')} != allowed_jars:
        raise ValueError('Unexpected embedded JARs in pack overrides')
    for entry in lock['overrides']:
        verify(overrides[entry['path']], entry)
    for relative in [entry['path'] for entry in selected] + list(overrides):
        checked_target(instance, relative)
    print(manifest['name'], manifest['versionId'], '(' + args.side + ')')
    print('Minecraft ' + manifest['dependencies']['minecraft'] + '; Fabric Loader ' + manifest['dependencies']['fabric-loader'] + '; Java 25+')
    for entry in selected:
        print('Download:', entry['path'])
    for relative in sorted(allowed_jars):
        print('Included:', relative)
    if args.dry_run:
        return
    # Download and check everything before writing the instance. Never replace a
    # different existing file; users can deliberately remove old mod versions.
    with tempfile.TemporaryDirectory(prefix='suite-pack-') as temporary:
        staged = Path(temporary)
        entries = {entry['path']: entry for entry in selected}
        all_paths = list(entries) + list(overrides)
        for relative in all_paths:
            target = checked_target(instance, relative)
            if relative in overrides:
                data = overrides[relative]
            elif target.is_file():
                data = target.read_bytes()
                verify(data, entries[relative])
            else:
                data = fetch(entries[relative])
            if target.exists() and (not target.is_file() or target.read_bytes() != data):
                raise ValueError('Existing file differs; refusing to overwrite: ' + str(target))
            staging_file = staged.joinpath(*safe_path(relative).parts)
            staging_file.parent.mkdir(parents=True, exist_ok=True)
            staging_file.write_bytes(data)
        for relative in all_paths:
            target = checked_target(instance, relative)
            data = staged.joinpath(*safe_path(relative).parts).read_bytes()
            if target.exists():
                if not target.is_file() or target.read_bytes() != data:
                    raise ValueError('Destination changed during installation: ' + str(target))
                continue
            target.parent.mkdir(parents=True, exist_ok=True)
            # Exclusive creation prevents a file appearing during the download
            # from being silently replaced.
            with target.open('xb') as output:
                output.write(data)
    print('Installed and verified mod files. Install Minecraft/Fabric and Java separately; no EULA was accepted or server started.')


if __name__ == '__main__':
    try:
        main()
    except (OSError, ValueError, KeyError, zipfile.BadZipFile) as error:
        raise SystemExit(str(error))
