#!/usr/bin/env python3
"""Run pouch utilities mechanics and persistence on exact suite binaries."""
from pathlib import Path
import argparse
import hashlib
import json
import os
import shutil
import subprocess
import sys
import zipfile

ROOT = Path(__file__).resolve().parents[1]
WORKSPACE = ROOT.parents[1]
sys.path.insert(0, str(ROOT / 'tools'))
from workspace_paths import load_helper, project_path, mapstitch_launch


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--jar', type=Path, required=True)
    parser.add_argument('--label', required=True)
    parser.add_argument('--compile-only', action='store_true')
    args = parser.parse_args()
    run = ROOT / 'qa-pickup/runs' / args.label
    run.mkdir(parents=True, exist_ok=False)
    fixture = run / 'fixture'
    classes = fixture / 'classes'
    classes.mkdir(parents=True)
    helper = load_helper(project_path(WORKSPACE, 'chalk-polymer-shim') / 'qa/run.py', 'cached_cp')
    servercp, clientcp, _ = helper.prepare_classpaths('26.3', run / 'libraries')
    suite = run / args.jar.name
    shutil.copy2(args.jar.resolve(), suite)
    with zipfile.ZipFile(suite) as archive:
        feature_names = {Path(entry['file']).name for entry in json.loads(archive.read('fabric.mod.json'))['jars']}
    dependencies = []
    for path in (ROOT / 'libs').glob('*.jar'):
        with zipfile.ZipFile(path) as archive:
            if path.name not in feature_names and 'fabric.mod.json' in archive.namelist():
                dependencies.append(path)
    stage = project_path(WORKSPACE, 'polymer-shim-test-bundle') / 'staging-2026-10-01/mods'
    dependencies += [stage / 'fabric-api-0.161.0+26.3.jar', stage / 'cloth-config-fabric-26.3.159.jar']
    polymer = stage / 'polymer-bundled-0.18.2+26.3.jar'
    assert not any(path.name.startswith('defaulted-') for path in dependencies), 'Defaulted remains compile-only'
    paths = [*servercp, *clientcp, suite, *dependencies, polymer]

    def unpack(path):
        with zipfile.ZipFile(path) as archive:
            for name in archive.namelist():
                if not name.endswith('.jar'):
                    continue
                content = archive.read(name)
                target = fixture / (hashlib.sha256(content).hexdigest()[:12] + '-' + Path(name).name)
                if not target.exists():
                    target.write_bytes(content)
                    paths.append(target)
                    unpack(target)

    for path in [suite, *dependencies, polymer]:
        unpack(path)
    patched = next((project_path(WORKSPACE, 'SSO-backpack-toolpouch-mapstitch-shim') / '.gradle/loom-cache').rglob('minecraft-merged-*-26.3.jar'))
    paths.insert(0, patched)
    sources = [*sorted((ROOT / 'qa-pickup/src').glob('*.java')), *sorted((ROOT / 'qa-pouch-utilities/src').glob('*.java'))]
    subprocess.run(['/usr/lib/jvm/java-27-openjdk/bin/javac', '--release', '25', '-proc:none', '-implicit:none', '-cp', os.pathsep.join(map(str, dict.fromkeys(paths))), '-d', str(classes), *map(str, sources)], check=True)
    observer = fixture / 'pouch-utilities-qa.jar'
    with zipfile.ZipFile(observer, 'w', zipfile.ZIP_DEFLATED) as archive:
        archive.writestr('fabric.mod.json', json.dumps({'schemaVersion': 1, 'id': 'suite_pouch_utilities_qa', 'version': '1', 'environment': 'server', 'entrypoints': {'main': ['qa.pickup.PickupQa']}, 'depends': {'fabric-api': '*', 'thenathe_mod_suite': '*'}}))
        for path in classes.rglob('*.class'):
            archive.write(path, path.relative_to(classes))
    if args.compile_only:
        print('PASS fixtures compiled, no runtime launched')
        return
    result = {'passed': False, 'bundle_sha256': sha(suite), 'fixture_inputs': {str(path.relative_to(ROOT)): sha(path) for path in [Path(__file__), *sources]}, 'cases': []}
    launch = mapstitch_launch(WORKSPACE)
    try:
        for profile in ('native', 'polymer'):
            directory = run / profile
            (directory / 'mods').mkdir(parents=True)
            launch.copy_accepted_eula(directory)
            selected = [suite, *dependencies, observer] + ([polymer] if profile == 'polymer' else [])
            for path in selected:
                shutil.copy2(path, directory / 'mods' / path.name)
            (directory / 'server.properties').write_text('server-ip=127.0.0.1\nserver-port=0\nonline-mode=false\nwhite-list=false\nspawn-protection=16\nview-distance=2\nsimulation-distance=2\npause-when-empty-seconds=0\nlevel-type=minecraft:flat\ngenerator-settings={"layers":[{"block":"minecraft:bedrock","height":1}],"biome":"minecraft:plains"}\ngenerate-structures=false\n')
            command = ['/usr/lib/jvm/java-25-openjdk/bin/java', '-Xmx2G', '-XX:ActiveProcessorCount=2', '-cp', os.pathsep.join(map(str, servercp)), 'net.fabricmc.loader.impl.launch.knot.KnotServer', 'nogui']
            (directory / 'audit.json').write_text(json.dumps({'command': command, 'mods': [{'file': path.name, 'sha256': sha(path)} for path in selected]}, indent=2) + '\n')
            for phase in ('initial', 'restart'):
                output = directory / 'pickup-result.json'
                output.unlink(missing_ok=True)
                with (directory / ('console-' + phase + '.log')).open('w') as log:
                    process = subprocess.Popen(command, cwd=directory, stdout=log, stderr=subprocess.STDOUT)
                    try:
                        code = process.wait(timeout=180)
                    except subprocess.TimeoutExpired:
                        process.kill()
                        process.wait()
                        raise
                observation = json.loads(output.read_text())
                record = {'profile': profile, 'phase': phase, 'exit_code': code, 'result': observation}
                result['cases'].append(record)
                print(json.dumps(record), flush=True)
                if code != 0 or not observation['passed']:
                    raise AssertionError(record)
        result['passed'] = True
    finally:
        (run / 'result.json').write_text(json.dumps(result, indent=2) + '\n')


if __name__ == '__main__':
    main()
