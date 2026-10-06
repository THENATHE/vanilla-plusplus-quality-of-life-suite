#!/usr/bin/env python3
"""Native atlas dimension/scale renderer regression with real map metadata packets and dimension travel."""
from pathlib import Path
import argparse, hashlib, json, os, shutil, socket, subprocess, sys, time, zipfile
ROOT = Path(__file__).resolve().parents[1]
WORKSPACE = ROOT.parents[1]
sys.path.insert(0, str(ROOT / 'tools'))
from workspace_paths import mapstitch_launch, project_path

def sha(path): return hashlib.sha256(path.read_bytes()).hexdigest()
def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--label', required=True)
    parser.add_argument('--jar', type=Path, default=ROOT / 'build/libs/vanilla-plusplus-quality-of-life-suite-1.1.1+26.3.jar')
    parser.add_argument('--baseline-1.1.3', dest='baseline_1_1_3', action='store_true', help='Use the historical repair command for the expected full-screen regression on stable1.1.3.')
    parser.add_argument('--atlas-smoke', action='store_true', help='Run two negative-grid Ctrl+Q cases and all source/pouch cases; omit the historical21-view matrix.')
    args = parser.parse_args()
    run = ROOT / 'qa-multiscale/runs' / args.label
    run.mkdir(parents=True, exist_ok=False)
    control = run / 'control'; control.mkdir()
    fixtures = run / 'fixtures'; fixtures.mkdir()
    classes = fixtures / 'classes'; classes.mkdir()
    launch = mapstitch_launch(WORKSPACE)
    suite = args.jar.resolve()
    dependencies = [ROOT / 'libs' / name for name in (
        'codecui-26.3-1.4.3-fabric.jar', 'defaulted-1.3.8+26.3.dropfix.1-fabric.jar',
        'fabric-language-kotlin-1.14.1+kotlin.2.4.20.jar', 'fzzy_config-0.7.7+fix2+26.3.jar', 'mixson-2.2.1-multiloader.jar')]
    dependencies += [launch.artifact('net.fabricmc.fabric-api', 'fabric-api', '0.161.0+26.3'),
                     launch.artifact('me.shedaniel.cloth', 'cloth-config-fabric', '26.3.159')]
    polymer = WORKSPACE / 'Builds/Minecraft/Polymer/Main Plugin/0.18.2+26.3/polymer-bundled-0.18.2+26.3.jar'
    server_audit, client_audit = launch.audits()
    paths = launch.cp(server_audit['command']) + launch.cp(client_audit['command']) + list(map(str, [suite, *dependencies, polymer]))
    paths += [str(launch.artifact('io.github.llamalad7', 'mixinextras-fabric', '0.5.5'))]
    patched = next((project_path(WORKSPACE, 'SSO-backpack-toolpouch-mapstitch-shim') / '.gradle/loom-cache').rglob('minecraft-merged-*-26.3.jar'))
    paths.insert(0, str(patched))
    def nested(jar):
        with zipfile.ZipFile(jar) as archive:
            for name in archive.namelist():
                if name.endswith('.jar'):
                    content = archive.read(name)
                    target = fixtures / (hashlib.sha256(content).hexdigest()[:12] + '-' + Path(name).name)
                    if not target.exists(): target.write_bytes(content); paths.append(str(target)); nested(target)
    for jar in [suite, *dependencies, polymer]: nested(jar)
    sources = [ROOT / 'qa-multiscale/src' / name for name in (
        'MixedScaleClientServerQa.java', 'MixedScaleClientQa.java', 'MapRenderTraceMixin.java', 'MapEjectTraceMixin.java', 'WorldMapTextTraceMixin.java')]
    subprocess.run(['/usr/lib/jvm/java-27-openjdk/bin/javac', '--release', '25', '-proc:none',
                    '-cp', os.pathsep.join(dict.fromkeys(paths)), '-d', str(classes), *map(str, sources)], check=True)
    for side in ('server', 'client'):
        entry = 'qa.MixedScaleClientServerQa' if side == 'server' else 'qa.MixedScaleClientQa'
        metadata = {'schemaVersion': 1, 'id': 'suite_maps_client_qa_' + side, 'version': '1', 'environment': side,
                    'entrypoints': {'main' if side == 'server' else 'client': [entry]},
                    'depends': {'fabric-api': '*', 'thenathe_mod_suite': '*'}}
        with zipfile.ZipFile(fixtures / (side + '.jar'), 'w') as archive:
            if side == 'client':
                metadata['mixins'] = ['suite-map-render-qa.mixins.json']
                archive.writestr('suite-map-render-qa.mixins.json', json.dumps({'required': True, 'package': 'qa.mixin',
                    'compatibilityLevel': 'JAVA_25', 'client': ['MapRenderTraceMixin', 'MapEjectTraceMixin', 'WorldMapTextTraceMixin'], 'injectors': {'defaultRequire': 1}}))
            archive.writestr('fabric.mod.json', json.dumps(metadata))
            for file in classes.rglob('*.class'): archive.write(file, file.relative_to(classes))
    with socket.socket() as sock: sock.bind(('127.0.0.1', 0)); port = sock.getsockname()[1]
    children = []; audits = {}
    env = os.environ.copy(); env.update(DISPLAY=env.get('DISPLAY', ':1'), SDL_VIDEODRIVER='x11', SDL_VIDEO_X11_XINPUT2='0', LP_NUM_THREADS='2')
    try:
        for side in ('server', 'client'):
            directory = run / side; mods = directory / 'mods'; mods.mkdir(parents=True)
            selected = [suite, *dependencies, fixtures / (side + '.jar')] + ([polymer] if side == 'server' else [])
            for jar in selected: shutil.copy2(jar, mods / jar.name)
            if side == 'server':
                launch.copy_accepted_eula(directory)
                (directory / 'server.properties').write_text(f'server-ip=127.0.0.1\nserver-port={port}\nonline-mode=false\nwhite-list=false\nenforce-secure-profile=false\nspawn-protection=0\nview-distance=2\nsimulation-distance=2\ndifficulty=peaceful\nlevel-type=minecraft:flat\ngenerator-settings={{"layers":[{{"block":"minecraft:bedrock","height":1}}],"biome":"minecraft:plains"}}\ngenerate-structures=false\n')
            else:
                (directory / 'options.txt').write_text('pauseOnLostFocus:false\nguiScale:2\ngraphicsMode:0\nrenderDistance:2\nmaxFps:30\nmaxFpsInactive:30\nsoundCategory_master:0.0\njoinedFirstServer:true\n')
            command = launch.base_command('server' if side == 'server' else 'native', directory, port)
            command[0] = '/usr/lib/jvm/java-25-openjdk/bin/java'
            command.insert(1, '-Dmaps.qa.control=' + str(control))
            if args.baseline_1_1_3: command.insert(1, '-Dmaps.qa.baseline114=true')
            if args.atlas_smoke: command.insert(1, '-Dmaps.qa.atlasSmoke=true')
            audits[side] = {'command': command, 'mods': [{'file': p.name, 'sha256': sha(p)} for p in selected]}
            (directory / 'audit.json').write_text(json.dumps(audits[side], indent=2) + '\n')
            log = (directory / 'console.log').open('w')
            process = subprocess.Popen(command, cwd=directory, env=env, stdin=subprocess.PIPE, stdout=log, stderr=subprocess.STDOUT, text=True)
            children.append((process, log, side))
            if side == 'server':
                for _ in range(150):
                    if process.poll() is not None: raise RuntimeError('Server exited; inspect console.log')
                    if 'Done (' in (directory / 'console.log').read_text(): break
                    time.sleep(1)
                else: raise TimeoutError('Server startup')
        for _ in range(300):
            if (control / 'failure').exists(): raise RuntimeError((control / 'failure').read_text())
            if (control / 'result.txt').exists(): print((control / 'result.txt').read_text()); break
            if any(p.poll() is not None for p, _, _ in children): raise RuntimeError('Runtime exited; inspect console.log')
            time.sleep(1)
        else: raise TimeoutError('Map renderer regression')
    finally:
        for process, log, side in reversed(children):
            if process.poll() is None:
                if side == 'server':
                    try: process.stdin.write('stop\n'); process.stdin.flush()
                    except BrokenPipeError: pass
                else: process.terminate()
                try: process.wait(timeout=20)
                except subprocess.TimeoutExpired: process.kill(); process.wait()
            log.close()
        result = {'bundle_sha256': sha(suite), 'passed': (control / 'result.txt').exists() and not (control / 'failure').exists(), 'launches': audits}
        for name in ('result.txt', 'observations.json', 'failure'):
            if (control / name).exists(): result[name] = (control / name).read_text()
        (run / 'result.json').write_text(json.dumps(result, indent=2) + '\n')
if __name__ == '__main__': main()
