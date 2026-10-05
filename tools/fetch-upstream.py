#!/usr/bin/env python3
"""Fetch verified developer binaries, published sources and pinned repository snapshots.

Published sources are authoritative for the exact binary. A GitHub HEAD snapshot
is a build/history reference and may contain unpublished changes. No GitHub fork
or checkout is created; all source comes from pinned public archives.
"""
from pathlib import Path
import argparse
import hashlib
import io
import json
import shutil
import urllib.request
import zipfile

SUITE = Path(__file__).resolve().parents[1]
COMPONENTS = {
    'sso': ('simple-smithing-overhaul', '2.9.14+26.2', 'pajicadvance/simple-smithing-overhaul'),
    'mapstitch': ('mapstitch', '1.1.6+26.3', 'pajicadvance/mapstitch'),
    'toolpouch': ('tool-pouch', '1.1.10+26.3', 'pajicadvance/toolpouch'),
    'tiered-backpacks': ('tiered-backpacks', '1.0.20+26.3', 'pajicadvance/tiered_backpacks'),
    'misctweaks': ('misctweaks', '1.4.4+26.3', 'pajicadvance/misctweaks'),
    'simple-death-improvements': ('simple-death-improvements', '1.6.0+26.3', 'pajicadvance/simple-death-improvements'),
}

def read(url):
    req = urllib.request.Request(url, headers={'User-Agent': 'ThenatheModSuite-source-provenance/1.0'})
    with urllib.request.urlopen(req, timeout=60) as response:
        return response.read()

def json_get(url):
    return json.loads(read(url))

def save_json(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + '\n')

def extract(data, destination, prefix=''):
    with zipfile.ZipFile(io.BytesIO(data)) as archive:
        assert archive.testzip() is None
        for name in archive.namelist():
            if prefix and not name.startswith(prefix):
                continue
            relative = Path(name[len(prefix):])
            if not relative.parts or name.endswith('/'):
                continue
            if relative.is_absolute() or '..' in relative.parts:
                raise ValueError('Unsafe source archive entry: ' + name)
            target = destination / relative
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(archive.read(name))

def fetch(component):
    slug, version, repository = COMPONENTS[component]
    root = SUITE / 'components' / component
    upstream = root / 'upstream'
    artifacts = upstream / 'artifacts'
    metadata = upstream / 'metadata'
    artifacts.mkdir(parents=True, exist_ok=True)
    project = json_get('https://api.modrinth.com/v2/project/' + slug)
    versions = json_get('https://api.modrinth.com/v2/project/' + slug + '/version')
    upstream_minecraft = version.split('+', 1)[1]
    matching = [v for v in versions if v['version_number'] == version and upstream_minecraft in v['game_versions'] and 'fabric' in v['loaders']]
    if len(matching) != 1:
        raise RuntimeError(f'Expected one official Fabric{upstream_minecraft} {slug} {version} release, got {len(matching)}')
    release = matching[0]
    save_json(metadata / 'modrinth-project.json', project)
    save_json(metadata / 'modrinth-version.json', release)
    binary = None
    source = None
    for entry in release['files']:
        if not (entry['primary'] or entry['filename'].endswith('-sources.jar')):
            continue
        output = artifacts / entry['filename']
        content = output.read_bytes() if output.exists() else read(entry['url'])
        for algorithm, checksum in entry['hashes'].items():
            if hashlib.new(algorithm, content).hexdigest() != checksum:
                raise RuntimeError('Official checksum mismatch: ' + str(output))
        output.write_bytes(content)
        if entry['primary']:
            binary = output
        if entry['filename'].endswith('-sources.jar'):
            source = output
            if (upstream / 'source').exists():
                shutil.rmtree(upstream / 'source')
            extract(content, upstream / 'source')
    if binary is None:
        raise RuntimeError('No primary release file')
    with zipfile.ZipFile(binary) as archive:
        assert archive.testzip() is None
        fabric = json.loads(archive.read('fabric.mod.json'))
    repo = json_get('https://api.github.com/repos/' + repository)
    revision = json_get('https://api.github.com/repos/' + repository + '/commits/' + repo['default_branch'])['sha']
    git_ref = {'repository': 'https://github.com/' + repository, 'branch': repo['default_branch'], 'commit': revision}
    save_json(metadata / 'github-revision.json', git_ref)
    snapshot = artifacts / f'{component}-{revision}-source.zip'
    content = snapshot.read_bytes() if snapshot.exists() else read('https://api.github.com/repos/' + repository + '/zipball/' + revision)
    snapshot.write_bytes(content)
    with zipfile.ZipFile(io.BytesIO(content)) as archive:
        prefix = archive.namelist()[0]
    if (upstream / 'repository').exists():
        shutil.rmtree(upstream / 'repository')
    extract(content, upstream / 'repository', prefix)
    artifacts_lock = [dict(path=str(p.relative_to(SUITE)), sha256=hashlib.sha256(p.read_bytes()).hexdigest(), size=p.stat().st_size) for p in sorted(artifacts.iterdir()) if p.is_file()]
    configs = []
    source_tree = upstream / 'source'
    if source_tree.exists():
        import re
        for java in source_tree.rglob('*Config.java'):
            text = java.read_text(errors='replace')
            for config in re.findall(r'super\(\w+\.id\("([^"]+)"\)\)', text):
                configs.append(fabric['id'] + ':' + config)
    license_files = [p for p in (upstream / 'repository').glob('LICENSE*') if p.is_file()]
    record = dict(component=component, track='developer-release', mod_id=fabric['id'], artifact_version=fabric['version'], release_version=release['version_number'], minecraft=upstream_minecraft, suite_minecraft='26.3', upstream_minecraft=upstream_minecraft, advertised_game_versions=release['game_versions'], loader='fabric', modrinth_version_id=release['id'], modrinth_version_url=f"https://modrinth.com/mod/{slug}/version/{release['id']}", github=git_ref, binary=str(binary.relative_to(SUITE)), binary_sha256=hashlib.sha256(binary.read_bytes()).hexdigest(), published_sources=str(source.relative_to(SUITE)) if source else None, repository_snapshot=str((upstream / 'repository').relative_to(SUITE)), published_source_tree=str(source_tree.relative_to(SUITE)) if source else None, dependencies=fabric['depends'], suggests=fabric.get('suggests', {}), nested_jars=fabric.get('jars', []), config_ids=sorted(set(configs)), license=fabric['license'], copyright_file=str(license_files[0].relative_to(SUITE)) if license_files else None, artifacts=artifacts_lock, modifications=[], port_counterpart=None, port_counterpart_reason='Official developer release requires~26.2 and cannot be declared a26.3 runtime input. Existing local26.3 binary provenance must be independently resolved. Existing paused SSO-port is not updated or tested by this source-capture work.' if component == 'sso' else 'Official developer release supports26.3; no additional port created.')
    previous_lock = root / 'inputs.lock.json'
    if component == 'sso' and previous_lock.exists():
        previous = json.loads(previous_lock.read_text())
        if 'runtime_input' in previous:
            record['runtime_input'] = previous['runtime_input']
            record['suite_compatibility'] = previous.get('suite_compatibility')
            record['port_counterpart_reason'] = previous.get('port_counterpart_reason')
    save_json(root / 'inputs.lock.json', record)
    print(f"{component}: {version} {record['binary_sha256']}")
    return record

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('components', nargs='*', choices=list(COMPONENTS), default=[])
    args = parser.parse_args()
    selected = args.components or list(COMPONENTS)
    records = [fetch(name) for name in selected]
    inventory_path = SUITE / 'docs' / 'pajic-component-inputs.json'
    previous = json.loads(inventory_path.read_text()) if inventory_path.exists() else {'schema_version': 1, 'recorded_date': '2026-10-04', 'components': []}
    by_name = {r['component']: r for r in previous['components']}
    by_name.update({r['component']: r for r in records})
    previous['components'] = [by_name[n] for n in COMPONENTS if n in by_name]
    save_json(inventory_path, previous)
