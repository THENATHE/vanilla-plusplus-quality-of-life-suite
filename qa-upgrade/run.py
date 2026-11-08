#!/usr/bin/env python3
"""Write suite data with the previous release, then load the same world and configuration with the candidate."""
from pathlib import Path
import argparse, hashlib, json, os, re, shutil, subprocess, sys, zipfile

HERE = Path(__file__).resolve().parent
ROOT = HERE.parent
WORKSPACE = ROOT.parents[1]
sys.path.insert(0, str(ROOT / 'tools'))
from workspace_paths import load_helper, project_path

JAVA = '/usr/lib/jvm/java-25-openjdk/bin/java'
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--previous', type=Path, required=True, help='Previous release installation ZIP')
parser.add_argument('--candidate', type=Path, required=True, help='Candidate suite JAR')
parser.add_argument('--label', required=True)
args = parser.parse_args()
RUN = HERE / 'runs' / args.label
RUN.mkdir(parents=True, exist_ok=False)
sha = lambda path: hashlib.sha256(path.read_bytes()).hexdigest()

helper = load_helper(project_path(WORKSPACE, 'chalk-polymer-shim') / 'qa/run.py', 'cached_cp')
servercp, _, _ = helper.prepare_classpaths('26.3', RUN / 'cached-libraries')

# Previous release: the exact files from its installation ZIP, plus the unchanged download-only Fzzy input.
previous = RUN / 'previous-inputs'
with zipfile.ZipFile(args.previous) as archive:
    for name in archive.namelist():
        if name.startswith(('overrides/mods/', 'downloads/mods/')) and name.endswith('.jar') and 'modmenu' not in name:
            target = previous / Path(name).name
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(archive.read(name))
previous_mods = sorted(previous.glob('*.jar')) + [ROOT / 'libs/fzzy_config-0.7.7+fix3+26.3.jar']
install = ROOT / 'libs/installation'
candidate_mods = [args.candidate, ROOT / 'libs/codecui-26.3-1.4.3-fabric.jar', ROOT / 'libs/fzzy_config-0.7.7+fix3+26.3.jar',
                  ROOT / 'libs/mixson-2.2.1-multiloader.jar', install / 'cloth-config-fabric-26.3.159.jar',
                  install / 'fabric-api-0.165.2+26.3.jar', install / 'fabric-language-kotlin-1.14.2+kotlin.2.4.21.jar',
                  install / 'polymer-bundled-0.18.7+26.3.jar']

# Compile the fixture against the candidate; it only uses vanilla, Fabric API and the stable PetIndex API.
fixture = RUN / 'fixture'
classes = fixture / 'classes'
classes.mkdir(parents=True)
paths = [*servercp, *candidate_mods]
def nested(path):
    with zipfile.ZipFile(path) as archive:
        for name in archive.namelist():
            if name.endswith('.jar'):
                data = archive.read(name)
                dest = fixture / (hashlib.sha256(data).hexdigest()[:12] + '-' + Path(name).name)
                dest.write_bytes(data)
                paths.append(dest)
                nested(dest)
for mod in candidate_mods:
    nested(mod)
patched = next((project_path(WORKSPACE, 'SSO-backpack-toolpouch-mapstitch-shim') / '.gradle/loom-cache').rglob('minecraft-merged-*-26.3.jar'))
javac = Path(os.environ.get('JAVA_HOME', '/usr/lib/jvm/java-25-openjdk')) / 'bin/javac'
subprocess.run([str(javac), '--release', '25', '-proc:none', '-implicit:none', '-cp', os.pathsep.join(map(str, dict.fromkeys([patched, *paths]))),
                '-d', str(classes), *map(str, (HERE / 'src').rglob('*.java'))], check=True)
fixture_jar = fixture / 'upgrade-qa.jar'
with zipfile.ZipFile(fixture_jar, 'w', zipfile.ZIP_DEFLATED) as archive:
    archive.writestr('fabric.mod.json', json.dumps({'schemaVersion': 1, 'id': 'suite_upgrade_qa', 'version': '1', 'environment': 'server',
                                                    'entrypoints': {'main': ['suite.upgrade.UpgradeQa']}, 'depends': {'fabric-api': '*', 'vpp_mod_suite': '*'}}))
    for path in classes.rglob('*.class'):
        archive.write(path, path.relative_to(classes))

def run_server(directory, mods, phase):
    (directory / 'mods').mkdir(parents=True, exist_ok=True)
    for old in (directory / 'mods').glob('*.jar'):
        old.unlink()
    for mod in [*mods, fixture_jar]:
        shutil.copy2(mod, directory / 'mods' / mod.name)
    (directory / 'eula.txt').write_text('eula=true\n')
    (directory / 'server.properties').write_text('server-ip=127.0.0.1\nserver-port=0\nonline-mode=false\nlevel-type=minecraft:flat\n'
                                                 'generate-structures=false\nspawn-protection=0\nview-distance=2\nsimulation-distance=2\n')
    command = [JAVA, '-Xms256M', '-Xmx2G', '-XX:ActiveProcessorCount=2', '-Dsuite.upgrade.control=' + str(directory),
               '-Dsuite.upgrade.phase=' + phase, '-cp', os.pathsep.join(map(str, servercp)),
               'net.fabricmc.loader.impl.launch.knot.KnotServer', 'nogui']
    (directory / f'audit-{phase}.json').write_text(json.dumps({'command': command, 'mods': [{'file': p.name, 'sha256': sha(p)} for p in mods]}, indent=2) + '\n')
    with (directory / f'console-{phase}.log').open('w') as log:
        process = subprocess.Popen(command, cwd=directory, stdout=log, stderr=subprocess.STDOUT)
        try:
            code = process.wait(timeout=300)
        except subprocess.TimeoutExpired:
            process.kill()
            process.wait()
            raise
    return code, (directory / 'result.txt').read_text().strip() if (directory / 'result.txt').exists() else 'missing result'

def config_values(directory):
    values = {}
    for path in sorted((directory / 'config').rglob('*')):
        if path.is_file() and path.suffix in ('.toml', '.json', '.json5', '.properties'):
            for line in path.read_text(errors='replace').splitlines():
                match = re.match(r'\s*"?([A-Za-z0-9_.\-]+)"?\s*[=:]\s*(.+?),?\s*$', line)
                if match:
                    values[f'{path.relative_to(directory)}::{match.group(1)}'] = match.group(2)
    return values

old_dir, new_dir = RUN / 'previous', RUN / 'candidate'
old_dir.mkdir()
(old_dir / 'items.json').write_text((HERE / 'items.json').read_text())
code, result = run_server(old_dir, previous_mods, 'write')
record = {'previous_installation_zip': str(args.previous), 'previous_zip_sha256': sha(args.previous),
          'candidate': str(args.candidate), 'candidate_sha256': sha(args.candidate),
          'previous_mods': [{'file': p.name, 'sha256': sha(p)} for p in previous_mods],
          'candidate_mods': [{'file': p.name, 'sha256': sha(p)} for p in candidate_mods], 'write': {'exit': code, 'result': result}}
assert result.startswith('PASS'), result

# Non-default settings written by the previous release must survive the candidate's first load.
edits = []
for path in sorted((old_dir / 'config').rglob('*')):
    if not path.is_file():
        continue
    text = path.read_text(errors='replace')
    changed = re.sub(r'(?m)^(\s*"?[A-Za-z0-9_]+"?\s*[=:]\s*)true(,?)\s*$', lambda m: m.group(1) + 'false' + m.group(2), text, count=1)
    if changed != text:
        path.write_text(changed)
        edits.append(str(path.relative_to(old_dir)))
before = config_values(old_dir)
shutil.copytree(old_dir, new_dir, ignore=shutil.ignore_patterns('mods', 'logs', 'console-*.log', 'audit-*.json', 'result.txt'))
code, result = run_server(new_dir, candidate_mods, 'verify')
after = config_values(new_dir)
lost = {key: {'before': value, 'after': after.get(key)} for key, value in before.items() if after.get(key) != value}
# Changes made on purpose by an upstream release when it loads an older file (accepted by the user).
ACCEPTED_UPSTREAM = {
    'config/mapstitch/config.toml::version': 'MapStitch 1.2 config v1 -> v2 migration',
    'config/mapstitch/config.toml::compass': 'MapStitch 1.2 clears compass requirements',
    'config/mapstitch/config.toml::clock': 'MapStitch 1.2 clears clock requirements',
    'config/mapstitch/config.toml::allowGameTime': 'MapStitch 1.2 allows minimap time',
    'config/mapstitch/config.toml::allowCoordinates': 'MapStitch 1.2 allows minimap coordinates',
    'config/mapstitch/config.toml::allowBiome': 'MapStitch 1.2 allows minimap biome',
    'config/mapstitch/config.toml::allowWeather': 'MapStitch 1.2 allows minimap weather',
    'config/mapstitch/config.toml::minecraft': 'line-based reader artifact: MapStitch 1.2 adds a multi-line loot-table list',
    'config/polymer/server.json::_c1': 'Polymer 0.18.7 removed the PolyMc options',
    'config/polymer/server.json::polyMcSyncModdedEntries': 'Polymer 0.18.7 removed the PolyMc options',
    'config/polymer/server.json::_c13': 'Polymer 0.18.7 removed the PolyMc options',
    'config/polymer/server.json::override_polymc_mining_check': 'Polymer 0.18.7 removed the PolyMc options',
}
explained = {key: ACCEPTED_UPSTREAM[key] for key in lost if key in ACCEPTED_UPSTREAM}
unexplained = {key: change for key, change in lost.items() if key not in ACCEPTED_UPSTREAM}
record.update(verify={'exit': code, 'result': result}, config_files_edited=edits, config_values_checked=len(before),
              config_changes=lost, accepted_upstream_changes=explained, unexplained_config_changes=unexplained)
verify = new_dir / 'verify.json'
record['world'] = json.loads(verify.read_text()) if verify.exists() else None
record['passed'] = result.startswith('PASS') and not unexplained
(RUN / 'result.json').write_text(json.dumps(record, indent=2) + '\n')
print(json.dumps({k: record[k] for k in ('write', 'verify', 'config_values_checked', 'accepted_upstream_changes', 'unexplained_config_changes', 'passed')}, indent=2))
