#!/usr/bin/env python3
"""Spend atlas pins from small and large containers on a dedicated server, with and without Polymer."""
from pathlib import Path
import argparse, hashlib, json, os, shutil, subprocess, sys, zipfile

HERE = Path(__file__).resolve().parent
ROOT = HERE.parent
WORKSPACE = ROOT.parents[1]
sys.path.insert(0, str(ROOT / 'tools'))
from workspace_paths import load_helper, project_path

JAVA = '/usr/lib/jvm/java-25-openjdk/bin/java'
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--jar', type=Path, required=True)
parser.add_argument('--label', required=True)
parser.add_argument('--baseline', action='store_true', help='Record the result without requiring it to pass (for a build without the guard)')
args = parser.parse_args()
RUN = HERE / 'runs' / args.label
RUN.mkdir(parents=True, exist_ok=False)
sha = lambda path: hashlib.sha256(path.read_bytes()).hexdigest()

helper = load_helper(project_path(WORKSPACE, 'chalk-polymer-shim') / 'qa/run.py', 'cached_cp')
servercp, _, _ = helper.prepare_classpaths('26.3', RUN / 'cached-libraries')
install = ROOT / 'libs/installation'
mods = [args.jar, ROOT / 'libs/codecui-26.3-1.4.3-fabric.jar', ROOT / 'libs/fzzy_config-0.7.7+fix3+26.3.jar',
        ROOT / 'libs/mixson-2.2.1-multiloader.jar', install / 'cloth-config-fabric-26.3.159.jar',
        install / 'fabric-api-0.165.2+26.3.jar', install / 'fabric-language-kotlin-1.14.2+kotlin.2.4.21.jar']
polymer = install / 'polymer-bundled-0.18.7+26.3.jar'

fixture = RUN / 'fixture'
classes = fixture / 'classes'
classes.mkdir(parents=True)
paths = [*servercp, *mods]
def nested(path):
    with zipfile.ZipFile(path) as archive:
        for name in archive.namelist():
            if name.endswith('.jar'):
                data = archive.read(name)
                dest = fixture / (hashlib.sha256(data).hexdigest()[:12] + '-' + Path(name).name)
                dest.write_bytes(data)
                paths.append(dest)
                nested(dest)
for mod in mods:
    nested(mod)
patched = next((project_path(WORKSPACE, 'SSO-backpack-toolpouch-mapstitch-shim') / '.gradle/loom-cache').rglob('minecraft-merged-*-26.3.jar'))
javac = Path(os.environ.get('JAVA_HOME', '/usr/lib/jvm/java-25-openjdk')) / 'bin/javac'
subprocess.run([str(javac), '--release', '25', '-proc:none', '-implicit:none', '-cp', os.pathsep.join(map(str, dict.fromkeys([patched, *paths]))),
                '-d', str(classes), *map(str, (HERE / 'src').rglob('*.java'))], check=True)
fixture_jar = fixture / 'pin-qa.jar'
with zipfile.ZipFile(fixture_jar, 'w', zipfile.ZIP_DEFLATED) as archive:
    archive.writestr('fabric.mod.json', json.dumps({'schemaVersion': 1, 'id': 'suite_pin_qa', 'version': '1', 'environment': 'server',
                                                    'entrypoints': {'main': ['suite.pins.PinQa']}, 'depends': {'fabric-api': '*', 'mapstitch': '*'}}))
    for path in classes.rglob('*.class'):
        archive.write(path, path.relative_to(classes))

record = {'jar': str(args.jar), 'jar_sha256': sha(args.jar), 'baseline': args.baseline, 'profiles': []}
for profile, extra in (('native', []), ('polymer', [polymer])):
    directory = RUN / profile
    (directory / 'mods').mkdir(parents=True)
    selected = [*mods, *extra, fixture_jar]
    for mod in selected:
        shutil.copy2(mod, directory / 'mods' / mod.name)
    (directory / 'eula.txt').write_text('eula=true\n')
    (directory / 'server.properties').write_text('server-ip=127.0.0.1\nserver-port=0\nonline-mode=false\nlevel-type=minecraft:flat\n'
                                                 'generate-structures=false\nview-distance=2\nsimulation-distance=2\n')
    command = [JAVA, '-Xms256M', '-Xmx2G', '-XX:ActiveProcessorCount=2', '-cp', os.pathsep.join(map(str, servercp)),
               'net.fabricmc.loader.impl.launch.knot.KnotServer', 'nogui']
    with (directory / 'console.log').open('w') as log:
        process = subprocess.Popen(command, cwd=directory, stdout=log, stderr=subprocess.STDOUT)
        try:
            code = process.wait(timeout=300)
        except subprocess.TimeoutExpired:
            process.kill()
            process.wait()
            raise
    output = directory / 'pins-result.json'
    result = json.loads(output.read_text()) if output.exists() else {'passed': False, 'error': 'missing result'}
    record['profiles'].append({'profile': profile, 'exit': code, 'mods': [{'file': p.name, 'sha256': sha(p)} for p in selected], 'result': result})
record['passed'] = all(p['result'].get('passed') for p in record['profiles'])
(RUN / 'result.json').write_text(json.dumps(record, indent=2) + '\n')
for p in record['profiles']:
    print(p['profile'], p['result'].get('passed'), [(c['scenario'], c['path'], c['passed']) for c in p['result'].get('cases', [])], p['result'].get('error'))
if not args.baseline and not record['passed']:
    sys.exit(1)
