"""Run backpack crafting menu regressions against an exact supplied suite JAR."""
from pathlib import Path
import argparse
import hashlib
import json
import os
import shutil
import subprocess
import sys
import zipfile

parser = argparse.ArgumentParser()
parser.add_argument('suite', type=Path)
parser.add_argument('--work', type=Path, required=True)
parser.add_argument('--clientsort', action='store_true', help='Include real ClientSort server schema and permutation checks')
args = parser.parse_args()
root = Path(__file__).resolve().parents[1]
work = args.work.resolve()
work.mkdir(parents=True, exist_ok=False)
suite = args.suite.resolve()
digest = hashlib.sha256(suite.read_bytes()).hexdigest()
workspace = root.parents[1]
sys.path.insert(0, str(root / 'tools'))
from workspace_paths import load_helper, project_path, mapstitch_launch
helper = load_helper(project_path(workspace, 'chalk-polymer-shim') / 'qa/run.py', 'cached_cp')
servercp, clientcp, _ = helper.prepare_classpaths('26.3', work / 'libraries')
with zipfile.ZipFile(suite) as archive:
    bundled = {Path(entry['file']).name for entry in json.loads(archive.read('fabric.mod.json'))['jars']}
dependencies = []
for dependency in (root / 'libs').glob('*.jar'):
    with zipfile.ZipFile(dependency) as archive:
        if dependency.name not in bundled and 'fabric.mod.json' in archive.namelist():
            dependencies.append(dependency)
stage = project_path(workspace, 'polymer-shim-test-bundle') / 'staging-2026-10-01/mods'
dependencies += [stage / 'fabric-api-0.161.0+26.3.jar', stage / 'cloth-config-fabric-26.3.159.jar']
polymer = stage / 'polymer-bundled-0.18.2+26.3.jar'
assert not any(path.name.startswith('defaulted-') for path in dependencies)
compile_paths = [*servercp, *clientcp, suite, *dependencies, polymer]
clientsort = root / 'libs/compile-only/clientsort-fabric-3.104.1+26.3.jar'
compile_paths.append(clientsort)
if args.clientsort:
    dependencies.append(clientsort)
fixture = work / 'fixture'
fixture.mkdir()

def unpack(path):
    with zipfile.ZipFile(path) as archive:
        for name in archive.namelist():
            if not name.endswith('.jar'):
                continue
            content = archive.read(name)
            target = fixture / (hashlib.sha256(content).hexdigest()[:12] + '-' + Path(name).name)
            if not target.exists():
                target.write_bytes(content)
                compile_paths.append(target)
                unpack(target)

for dependency in [suite, *dependencies, polymer]:
    unpack(dependency)
patched = next((project_path(workspace, 'SSO-backpack-toolpouch-mapstitch-shim') / '.gradle/loom-cache').rglob('minecraft-merged-*-26.3.jar'))
compile_paths.insert(0, patched)
classes = work / 'classes'
classes.mkdir()
source = Path(__file__).with_name('BackpackCraftingProbe.java')
sort_source = Path(__file__).with_name('BackpackClientSortChecks.java')
shutil.copy2(source, work / source.name)
shutil.copy2(sort_source, work / sort_source.name)
shutil.copy2(Path(__file__), work / 'executed-run.py')
subprocess.run(['/usr/lib/jvm/java-27-openjdk/bin/javac', '--release', '25', '-proc:none', '-implicit:none',
                '-cp', os.pathsep.join(map(str, compile_paths)), '-d', str(classes), str(source), str(sort_source)], check=True)
probe = work / 'backpack-crafting-probe.jar'
with zipfile.ZipFile(probe, 'w', zipfile.ZIP_DEFLATED) as archive:
    archive.writestr('fabric.mod.json', json.dumps({'schemaVersion': 1, 'id': 'backpack_crafting_probe', 'version': '2',
        'environment': 'server', 'entrypoints': {'main': ['BackpackCraftingProbe']},
        'depends': {'fabric-api': '*', 'vpp_mod_suite': '*'}}))
    for compiled in classes.rglob('*.class'):
        archive.write(compiled, compiled.relative_to(classes))
results = {}
for profile in ('native', 'polymer'):
    target = work / profile
    (target / 'mods').mkdir(parents=True)
    selected = [suite, *dependencies, probe] + ([polymer] if profile == 'polymer' else [])
    for dependency in selected:
        shutil.copy2(dependency, target / 'mods' / dependency.name)
    mapstitch_launch(workspace).copy_accepted_eula(target)
    (target / 'server.properties').write_text('server-ip=127.0.0.1\nserver-port=0\nonline-mode=false\nwhite-list=false\nspawn-protection=16\nview-distance=2\nsimulation-distance=2\npause-when-empty-seconds=0\nlevel-type=minecraft:flat\ngenerator-settings={"layers":[{"block":"minecraft:bedrock","height":1}],"biome":"minecraft:plains"}\ngenerate-structures=false\n')
    command = ['/usr/lib/jvm/java-25-openjdk/bin/java', '-Xmx2G', '-XX:ActiveProcessorCount=2', '-cp',
               os.pathsep.join(map(str, servercp)), 'net.fabricmc.loader.impl.launch.knot.KnotServer', 'nogui']
    (target / 'audit.json').write_text(json.dumps({'command': command, 'mods': [
        {'file': path.name, 'sha256': hashlib.sha256(path.read_bytes()).hexdigest()} for path in selected]}, indent=2) + '\n')
    with (target / 'console.log').open('w') as log:
        process = subprocess.run(command, cwd=target, stdout=log, stderr=subprocess.STDOUT, timeout=150)
    result = json.loads((target / 'backpack-crafting-result.json').read_text())
    result['bundle_sha256'] = digest
    result['exit_code'] = process.returncode
    results[profile] = result
    (work / 'result.json').write_text(json.dumps(results, indent=2) + '\n')
    print(profile, 'passed=' + str(result['passed']), 'checks=' + str(len(result.get('checks', []))), result.get('error', ''), flush=True)
    assert result['passed'] and process.returncode == 0
