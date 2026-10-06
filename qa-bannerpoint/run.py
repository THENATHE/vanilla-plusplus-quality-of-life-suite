#!/usr/bin/env python3
"""Real Bannerpoint native/fallback negotiation and downloaded Polymer-pack lifecycle QA."""
from pathlib import Path
import argparse, hashlib, json, os, shutil, socket, struct, subprocess, sys, time, zipfile

ROOT = Path(__file__).resolve().parents[1]
WORKSPACE = ROOT.parents[1]
sys.path.insert(0, str(ROOT / 'tools'))
from workspace_paths import mapstitch_launch, project_path

def sha(path): return hashlib.sha256(path.read_bytes()).hexdigest()
def freeport():
    with socket.socket() as sock: sock.bind(('127.0.0.1', 0)); return sock.getsockname()[1]
def save_server(path, port, policy):
    # Vanilla's own servers.dat, no loader/agent needed to accept the real server pack.
    def string(value):
        encoded = value.encode(); return struct.pack('>H', len(encoded)) + encoded
    data = b'\x0a\x00\x00\x09' + string('servers') + b'\x0a' + struct.pack('>i', 1)
    for key, value in [('name', 'Bannerpoint QA'), ('ip', f'127.0.0.1:{port}')]: data += b'\x08' + string(key) + string(value)
    if policy != 'prompt': data += b'\x01' + string('acceptTextures') + (b'\x01' if policy == 'accept' else b'\x00')
    path.write_bytes(data + b'\x00\x00')
def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--jar', type=Path, required=True)
    parser.add_argument('--label', required=True)
    parser.add_argument('--profiles', default='suite,original,fabric,vanilla,nopolymer')
    parser.add_argument('--restart', action='store_true', help='Also restart the no-Polymer world, verify both saved banners/name/UUIDs and break the map-linked banner')
    parser.add_argument('--prepare-only', action='store_true', help='Compile the exact-current QA fixtures without launching a server or graphical client')
    args = parser.parse_args()
    if args.restart and 'nopolymer' not in args.profiles.split(','): parser.error('--restart requires the nopolymer profile')
    run = ROOT / 'qa-bannerpoint/runs' / args.label; run.mkdir(parents=True, exist_ok=False)
    control = run / 'control'; control.mkdir()
    fixtures = run / 'fixtures'; fixtures.mkdir(); classes = fixtures / 'classes'; classes.mkdir()
    launch = mapstitch_launch(WORKSPACE); suite = run / args.jar.name; shutil.copy2(args.jar.resolve(), suite)
    dependencies = [ROOT / 'libs' / name for name in (
        'codecui-26.3-1.4.3-fabric.jar',
        'fabric-language-kotlin-1.14.1+kotlin.2.4.20.jar', 'fzzy_config-0.7.7+fix3+26.3.jar', 'mixson-2.2.1-multiloader.jar')]
    api = launch.artifact('net.fabricmc.fabric-api', 'fabric-api', '0.161.0+26.3')
    dependencies += [api, launch.artifact('me.shedaniel.cloth', 'cloth-config-fabric', '26.3.159')]
    original = ROOT / 'libs/bannerpoint-fabric-1.1.2+26.3.jar'
    if not original.exists(): original = Path('/tmp/bannerpoint-research/bannerpoint-fabric-1.1.2+26.3.jar')
    polymer = WORKSPACE / 'Builds/Minecraft/Polymer/Main Plugin/0.18.2+26.3/polymer-bundled-0.18.2+26.3.jar'
    server_audit, client_audit = launch.audits()
    paths = launch.cp(server_audit['command']) + launch.cp(client_audit['command']) + list(map(str, [suite, original, *dependencies, polymer]))
    paths += [str(launch.artifact('io.github.llamalad7', 'mixinextras-fabric', '0.5.5'))]
    patched = next((project_path(WORKSPACE, 'SSO-backpack-toolpouch-mapstitch-shim') / '.gradle/loom-cache').rglob('minecraft-merged-*-26.3.jar')); paths.insert(0, str(patched))
    def nested(jar):
        with zipfile.ZipFile(jar) as archive:
            for name in archive.namelist():
                if name.endswith('.jar'):
                    content = archive.read(name); target = fixtures / (hashlib.sha256(content).hexdigest()[:12] + '-' + Path(name).name)
                    if not target.exists(): target.write_bytes(content); paths.append(str(target)); nested(target)
    for jar in [suite, original, *dependencies, polymer]: nested(jar)
    subprocess.run(['/usr/lib/jvm/java-27-openjdk/bin/javac', '--release', '25', '-proc:none', '-cp', os.pathsep.join(dict.fromkeys(paths)), '-d', str(classes), *map(str, (ROOT / 'qa-bannerpoint/src').glob('*.java'))], check=True)
    for side in ('server', 'client'):
        entry = 'qa.bannerpoint.Bannerpoint' + side.title() + 'Qa'
        metadata = {'schemaVersion': 1, 'id': 'suite_bannerpoint_qa_' + side, 'version': '1', 'environment': side,
                    'entrypoints': {'main' if side == 'server' else 'client': [entry]}, 'depends': {'fabric-api': '*'}, 'mixins': ['bannerpoint-qa-' + side + '.mixins.json']}
        with zipfile.ZipFile(fixtures / (side + '.jar'), 'w') as archive:
            archive.writestr('fabric.mod.json', json.dumps(metadata))
            archive.writestr('bannerpoint-qa-' + side + '.mixins.json', json.dumps({'required': True, 'package': 'qa.bannerpoint.mixin', 'compatibilityLevel': 'JAVA_25', side if side == 'client' else 'mixins': ['GuiSpriteTraceMixin' if side == 'client' else 'ServerPacketTraceMixin'], 'injectors': {'defaultRequire': 1}}))
            for file in classes.rglob('*.class'): archive.write(file, file.relative_to(classes))
    if args.prepare_only:
        (run / 'preparation.json').write_text(json.dumps({'bundle_sha256': sha(suite), 'original_sha256': sha(original),
            'dependencies': [{'file': jar.name, 'sha256': sha(jar)} for jar in dependencies],
            'fixtures': [{'file': (fixtures / (side + '.jar')).name, 'sha256': sha(fixtures / (side + '.jar'))} for side in ('server', 'client')]}, indent=2) + '\n')
        print('PASS compiled current Bannerpoint QA fixtures; no runtime launched', flush=True)
        return
    children = []; observations = []; env = os.environ.copy()
    env.update(DISPLAY=env.get('DISPLAY', ':1'), SDL_VIDEODRIVER='x11', SDL_VIDEO_X11_XINPUT2='0', LP_NUM_THREADS='2')
    def start(directory, command, mods=()):
        (directory / 'mods').mkdir(parents=True, exist_ok=True)
        for jar in mods: shutil.copy2(jar, directory / 'mods' / jar.name)
        (directory / 'audit.json').write_text(json.dumps({'command': command, 'mods': [{'file': p.name, 'sha256': sha(p)} for p in mods]}, indent=2))
        log = (directory / 'console.log').open('w'); process = subprocess.Popen(command, cwd=directory, env=env, stdin=subprocess.PIPE, stdout=log, stderr=subprocess.STDOUT, text=True)
        children.append((process, log)); return process
    def wait(condition, process, label, timeout=140):
        until = time.time() + timeout
        while time.time() < until:
            if (control / 'failure').exists(): raise RuntimeError((control / 'failure').read_text())
            if process.poll() is not None: raise RuntimeError(label + ': runtime exited')
            if condition(): return
            time.sleep(.5)
        raise TimeoutError(label)
    def stop(process, server=False):
        if process.poll() is None:
            if server: process.stdin.write('stop\n'); process.stdin.flush()
            else: process.terminate()
            try: process.wait(timeout=25)
            except subprocess.TimeoutExpired: process.kill(); process.wait()
    def read(name, side):
        file = control / (name + '-' + side + '.json')
        try: return json.loads(file.read_text())
        except (FileNotFoundError, json.JSONDecodeError): return {}
    def banner_count(row): return sum(w['style'] == 'bannerpoint:banner' for w in row.get('waypoints', []))
    def regular(row): return any('aca2b810-9353-4ae5-8c72-1eb6540f147f' in w['id'] for w in row.get('waypoints', []))
    def capture(name, directory, label):
        (control / (name + '-screenshot.txt')).write_text(label + '.png')
        wait(lambda: (directory / 'screenshots' / (label + '.png')).exists(), client, 'quiet HUD screenshot ' + name + '/' + label, 60)
        time.sleep(.5)
        shots = directory / 'screenshots'; destination = run / 'screenshots'; destination.mkdir(exist_ok=True)
        if shots.exists():
            for file in shots.glob('*.png'): shutil.copy2(file, destination / (name + '-' + file.name))
    try:
        for with_polymer in (True, False):
            profiles = [p for p in args.profiles.split(',') if (p != 'nopolymer') == with_polymer]
            if not profiles: continue
            directory = run / ('server-polymer' if with_polymer else 'server-native'); directory.mkdir()
            port = freeport(); http_port = freeport(); pack_url = f'http://127.0.0.1:{http_port}/resource_pack.zip'
            launch.copy_accepted_eula(directory)
            properties = f'server-ip=127.0.0.1\nserver-port={port}\nonline-mode=false\nwhite-list=false\nenforce-secure-profile=false\nspawn-protection=0\nview-distance=2\nsimulation-distance=2\ndifficulty=peaceful\nlevel-type=minecraft:flat\ngenerator-settings={{"layers":[{{"block":"minecraft:bedrock","height":1}}],"biome":"minecraft:plains"}}\ngenerate-structures=false\n'
            (directory / 'server.properties').write_text(properties)
            if with_polymer:
                config_directory = directory / 'config/polymer'; config_directory.mkdir(parents=True)
                (config_directory / 'resource-pack.json').write_text(json.dumps({'main_uuid': '80536d49-002a-4d9c-9bb8-391abd17ae69', 'markResourcePackAsRequiredByDefault': False, 'include_mod_assets': [], 'include_zips': [], 'resource_pack_location': 'polymer/resource_pack.zip', 'prevent_path_with': [], 'ignore_pack_version': False, 'log_errors': True}))
            command = launch.base_command('server', directory, port); command[0] = '/usr/lib/jvm/java-25-openjdk/bin/java'
            command[1:1] = ['-Dbanner.qa.control=' + str(control), '-Dbanner.qa.pack.url=' + pack_url]
            server_command = command[:]
            server_mods = [suite, *dependencies, fixtures / 'server.jar'] + ([polymer] if with_polymer else [])
            server = start(directory, command, server_mods)
            wait(lambda: 'Done (' in (directory / 'console.log').read_text(), server, 'server startup')
            if with_polymer:
                server.stdin.write('polymer generate-pack\n'); server.stdin.flush(); pack = directory / 'polymer/resource_pack.zip'
                wait(pack.exists, server, 'Polymer pack generation'); time.sleep(1)
                with zipfile.ZipFile(pack) as archive:
                    assert archive.testzip() is None
                    for name in ['assets/bannerpoint/waypoint_style/banner.json', *['assets/bannerpoint/textures/gui/sprites/hud/locator_bar_dot/banner_' + str(i) + '.png' for i in range(4)]]: assert name in archive.namelist(), name
                    with zipfile.ZipFile(directory / 'polymer/stale-pack.zip', 'w') as stale:
                        for member in archive.namelist():
                            if not member.startswith('assets/bannerpoint/'): stale.writestr(member, archive.read(member))
                http_directory = directory / 'polymer'
                http = start(http_directory, [sys.executable, '-m', 'http.server', str(http_port), '--bind', '127.0.0.1']); time.sleep(.5)
                (control / 'pack-installed.txt').unlink(missing_ok=True)
                (control / 'pack-ready.json').write_text(json.dumps({'uuid': '80536d49-002a-4d9c-9bb8-391abd17ae69', 'url': pack_url, 'sha1': hashlib.sha1(pack.read_bytes()).hexdigest()}))
                wait(lambda: (control / 'pack-installed.txt').exists(), server, 'configure actual generated pack hash')
            for profile in profiles:
                name = 'BannerQA' + ('NoPoly' if profile == 'nopolymer' else profile.title()); client_directory = run / name; client_directory.mkdir()
                mods = [] if profile == 'vanilla' else [api, fixtures / 'client.jar']
                if profile in ('suite', 'nopolymer'): mods = [suite, *dependencies, fixtures / 'client.jar']
                elif profile == 'original': mods += [original, ROOT / 'libs/fzzy_config-0.7.7+fix3+26.3.jar', ROOT / 'libs/fabric-language-kotlin-1.14.1+kotlin.2.4.20.jar']
                save_server(client_directory / 'servers.dat', port, 'decline' if profile == 'fabric' else 'accept')
                (client_directory / 'options.txt').write_text('pauseOnLostFocus:false\nguiScale:2\ngraphicsMode:0\nrenderDistance:2\nmaxFps:25\nmaxFpsInactive:25\nsoundCategory_master:0.0\njoinedFirstServer:true\n')
                command = launch.base_command('vanilla' if profile == 'vanilla' else 'native', client_directory, port)
                command[0] = '/usr/lib/jvm/java-25-openjdk/bin/java'; command[1:1] = ['-Dbanner.qa.control=' + str(control)]
                command[command.index('--username') + 1] = name
                command[command.index('--width') + 1] = '900'; command[command.index('--height') + 1] = '600'
                client = start(client_directory, command, mods)
                wait(lambda: bool(read(name, 'server')), client, name + ' joined')
                if profile == 'fabric':
                    wait(lambda: read(name, 'client').get('ticks', 0) > 40, client, 'fallback pending-pack world')
                    before = read(name, 'client'); assert banner_count(before) == 0 and regular(before), before
                    assert not read(name, 'server')['can_render']
                    (control / (name + '-action.txt')).write_text('states')
                    wait(lambda: len([e for e in read(name, 'server').get('events', []) if e['kind'] == 'strict-state']) >= 7, client, 'non-success pack states remain hidden')
                    observations.append({'profile': profile, 'phase': 'before acceptance', 'client': before, 'server': read(name, 'server')})
                    (control / (name + '-client-action.txt')).write_text('allow')
                    time.sleep(1); (control / (name + '-action.txt')).write_text('push')
                if with_polymer:
                    wait(lambda: any('SUCCESSFULLY_LOADED' in e['detail'] for e in read(name, 'server').get('events', []) if e['kind'] == 'pack-status'), client, name + ' real downloaded pack loaded')
                if profile != 'vanilla':
                    wait(lambda: banner_count(read(name, 'client')) == 2 and regular(read(name, 'client')) and bool(read(name, 'client').get('rendered')), client, name + ' banner sprites rendered')
                    row = read(name, 'client'); assert all(s['resolved'] == s['requested'] for s in row['sprites']), row
                    capture(name, client_directory, 'accepted')
                else:
                    time.sleep(6)
                    # Wayland focus cannot reliably be established here. Keep this
                    # client completely unmodified and avoid capturing other apps.
                    print('NOTE pure vanilla: genuine pack load/waypoint packets verified; no UI capture', flush=True)
                observed = read(name, 'server')
                assert observed['can_render'], observed
                native = profile in ('suite', 'original', 'nopolymer'); assert observed['native_banner_channel'] == native
                if native:
                    wait(lambda: read(name, 'client').get('native_banner_name') == 'QA Red Named Banner', client, name + ' native name payload decoded')
                if not native: assert not any(e['kind'] == 'banner-name' for e in observed['events']), observed
                observations.append({'profile': profile, 'phase': 'loaded' if with_polymer else 'native without Polymer', 'client': read(name, 'client') if profile != 'vanilla' else {'loader': None, 'mods': [], 'real_pack_loaded': True, 'ui_capture': 'Not performed: Wayland focus cannot be established reliably; actual sprite evidence uses Fabric API-only vanilla renderer.'}, 'server': observed})
                if profile == 'fabric':
                    def successes(): return sum('SUCCESSFULLY_LOADED' in e['detail'] for e in read(name, 'server').get('events', []) if e['kind'] == 'pack-status')
                    previous_successes = successes()
                    (control / (name + '-action.txt')).write_text('stale')
                    wait(lambda: successes() > previous_successes and not read(name, 'server').get('can_render', True) and banner_count(read(name, 'client')) == 0 and regular(read(name, 'client')) and not read(name, 'client').get('pack_style_resource', True), client, 'stale mainpack success stays unsupported')
                    capture(name, client_directory, 'stale-pack')
                    observations.append({'profile': profile, 'phase': 'stale mainpack loaded with no banner artwork', 'client': read(name, 'client'), 'server': read(name, 'server')})
                    previous_successes = successes(); (control / (name + '-action.txt')).write_text('push')
                    wait(lambda: successes() > previous_successes and read(name, 'server').get('can_render', False) and banner_count(read(name, 'client')) == 2 and all(s['requested'] == s['resolved'] for s in read(name, 'client').get('sprites', [])), client, 'fresh current pack restores banners')
                    observations.append({'profile': profile, 'phase': 'fresh pack restored after stale replacement', 'client': read(name, 'client'), 'server': read(name, 'server')})
                    (control / (name + '-action.txt')).write_text('pop')
                    wait(lambda: not read(name, 'server').get('can_render', True) and banner_count(read(name, 'client')) == 0 and regular(read(name, 'client')), client, 'main pack removal hides banners')
                    capture(name, client_directory, 'removed')
                    observations.append({'profile': profile, 'phase': 'removed', 'client': read(name, 'client'), 'server': read(name, 'server')})
                    (control / (name + '-client-action.txt')).write_text('reject'); time.sleep(1)
                    previous_declines = sum('DECLINED' in e['detail'] for e in read(name, 'server').get('events', []) if e['kind'] == 'pack-status')
                    (control / (name + '-action.txt')).write_text('push')
                    wait(lambda: sum('DECLINED' in e['detail'] for e in read(name, 'server').get('events', []) if e['kind'] == 'pack-status') > previous_declines, client, 'pack explicitly declined')
                    time.sleep(2); row = read(name, 'client'); assert banner_count(row) == 0 and regular(row), row
                    observations.append({'profile': profile, 'phase': 'declined', 'client': row, 'server': read(name, 'server')})
                stop(client)
                print('PASS ' + name, flush=True)
            stop(server, True)
            if with_polymer: stop(http)
            elif args.restart:
                shutil.copy2(directory / 'audit.json', directory / 'audit-before-restart.json')
                shutil.copy2(directory / 'console.log', directory / 'console-before-restart.log')
                restart_command = server_command[:]; restart_command.insert(1, '-Dbanner.qa.restart=true')
                server = start(directory, restart_command, server_mods)
                wait(lambda: 'Done (' in (directory / 'console.log').read_text(), server, 'restart original Bannerpoint saved world')
                name = 'BannerQARestart'; client_directory = run / name; client_directory.mkdir()
                (client_directory / 'options.txt').write_text('pauseOnLostFocus:false\nguiScale:2\ngraphicsMode:0\nrenderDistance:2\nmaxFps:25\nmaxFpsInactive:25\nsoundCategory_master:0.0\njoinedFirstServer:true\n')
                command = launch.base_command('native', client_directory, port); command[0] = '/usr/lib/jvm/java-25-openjdk/bin/java'
                command[1:1] = ['-Dbanner.qa.control=' + str(control)]
                command[command.index('--username') + 1] = name
                command[command.index('--width') + 1] = '900'; command[command.index('--height') + 1] = '600'
                client = start(client_directory, command, [suite, *dependencies, fixtures / 'client.jar'])
                wait(lambda: (control / 'restart-verified.json').exists() and banner_count(read(name, 'client')) == 2 and regular(read(name, 'client')) and read(name, 'client').get('native_banner_name') == 'QA Red Named Banner', client, 'native restart restores name/UUIDs/map-linked waypoint')
                observations.append({'profile': 'nopolymer', 'phase': 'real saved-world restart', 'persistence': json.loads((control / 'restart-verified.json').read_text()), 'client': read(name, 'client'), 'server': read(name, 'server')})
                (control / (name + '-action.txt')).write_text('break-blue')
                wait(lambda: banner_count(read(name, 'client')) == 1 and regular(read(name, 'client')) and any(e['kind'] == 'native-banner-removed' for e in read(name, 'server').get('events', [])), client, 'actual banner destruction removes only its own waypoint and saved transmitter')
                observations.append({'profile': 'nopolymer', 'phase': 'native banner block destroyed after restart', 'client': read(name, 'client'), 'server': read(name, 'server')})
                stop(client); stop(server, True)
                print('PASS saved-world restart, stable UUIDs/name/map link, native banner removal', flush=True)
        (run / 'result.json').write_text(json.dumps({'passed': True, 'bundle_sha256': sha(suite), 'original_sha256': sha(original), 'observations': observations}, indent=2) + '\n')
        print('PASS profiles: ' + args.profiles, flush=True)
    finally:
        for process, log in reversed(children):
            if process.poll() is None:
                process.terminate()
                try: process.wait(timeout=15)
                except subprocess.TimeoutExpired: process.kill(); process.wait()
            log.close()

if __name__ == '__main__': main()
