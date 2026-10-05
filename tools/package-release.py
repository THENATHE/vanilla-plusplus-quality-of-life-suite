#!/usr/bin/env python3
"""Publishable four-asset layout from an already committed, runtime-verified build."""
import hashlib
import importlib.util
import json
import re
import shutil
import subprocess
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
WORKSPACE = ROOT.parents[1]
SLUG = 'vanilla-plusplus-quality-of-life-suite'
PRODUCT = 'Vanilla++ Quality of Life Suite'
REPO = 'https://github.com/THENATHE/' + SLUG
spec = importlib.util.spec_from_file_location('installation_builder', ROOT / 'tools/package-installation.py')
builder = importlib.util.module_from_spec(spec)
spec.loader.exec_module(builder)
sha = lambda path: hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    report = json.loads((ROOT / 'docs/build-verification.json').read_text())
    version = report['version']
    artifact = ROOT / 'build/libs' / f'{SLUG}-{version}.jar'
    assert report['runtime_tested'], 'Record final runtime verification first'
    assert sha(artifact) == report['sha256'], 'Artifact changed after verification'
    revision = subprocess.check_output(['git', 'rev-parse', 'HEAD'], cwd=ROOT, text=True).strip()
    assert not subprocess.check_output(['git', 'status', '--porcelain'], cwd=ROOT, text=True).strip(), 'Commit final source/evidence before packaging'
    installation = json.loads((ROOT / 'docs/installation-pack-verification.json').read_text())
    assert installation['passed'] and installation['pack_version'] == version
    release = WORKSPACE / 'Builds/Minecraft' / PRODUCT / 'Main Plugin' / version
    release.mkdir(parents=True, exist_ok=True)
    shutil.copy2(artifact, release / artifact.name)
    kit = builder.package(release)
    assert installation['archives'][kit.name] == sha(kit), 'Installation ZIP changed after verification'
    readme = (ROOT / 'README.md').read_text()
    readme = re.sub(r'\]\((?!https?://|#)([^)]+)\)', lambda match: '](' + REPO + '/blob/' + revision + '/' + match.group(1) + ')', readme)
    readme += f'\n## Release record\n\nRelease: `{version}`. Source revision: `{revision}`. Exact installation requirements, artifact verification and historical test limits are linked above. The four uploaded assets are this README, the feature JAR, `docs.zip`, and the installation ZIP. Local checksum/publication records accompany the preserved release folder.\n'
    (release / 'README.md').write_text(readme)
    tracked = subprocess.check_output(['git', 'ls-files', '-z'], cwd=ROOT).decode().split('\0')
    documents = {}
    for name in filter(None, tracked):
        path = ROOT / name
        if (path.suffix.lower() in ('.md', '.rst', '.adoc') or name.startswith(('docs/', 'licenses/', 'locks/'))
                or ('/evidence/' in name and path.suffix.lower() in ('.json', '.txt', '.png', '.sha256'))):
            assert path.is_file() and not path.is_symlink(), name
            assert not name.endswith('.jar'), 'Development binaries must not enter docs.zip'
            documents[name] = path.read_bytes()
    documents['RELEASE_RECORD.json'] = (json.dumps({'version': version, 'revision': revision,
        'title': version, 'jar_sha256': sha(artifact), 'installation_sha256': sha(kit),
        'runtime_evidence': report['evidence'], 'documentation_files': len(documents)}, indent=2) + '\n').encode()
    documents['SHA256SUMS.sha256'] = ''.join(sha(path) + '  ' + path.name + '\n' for path in (release / artifact.name, kit, release / 'README.md')).encode()
    docs = release / 'docs.zip'
    builder.archive(docs, documents)
    public = [release / 'README.md', release / artifact.name, docs, kit]
    for path in public[1:]:
        with zipfile.ZipFile(path) as archive:
            assert archive.testzip() is None
    checksums = ''.join(sha(path) + '  ' + path.name + '\n' for path in public)
    (release / 'SHA256SUMS.sha256').write_text(checksums)
    record = {'release': str(release), 'version': version, 'title': version, 'tag': 'v' + version,
              'revision': revision, 'public_assets': [path.name for path in public],
              'docs_files': len(documents), 'checksums': checksums}
    (release / 'RELEASE_RECORD.json').write_text(json.dumps(record, indent=2) + '\n')
    print(json.dumps(record, indent=2))


if __name__ == '__main__':
    main()
