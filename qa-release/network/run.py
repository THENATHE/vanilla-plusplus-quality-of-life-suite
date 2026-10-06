#!/usr/bin/env python3
"""Focused connected atlas command network compatibility QA."""
from pathlib import Path
import argparse, hashlib, json, os, shutil, socket, struct, subprocess, sys, time, zipfile

ROOT = Path(__file__).resolve().parents[2]
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
    for key, value in [('name', 'Atlas Maintenance QA'), ('ip', f'127.0.0.1:{port}')]: data += b'\x08' + string(key) + string(value)
    if policy != 'prompt': data += b'\x01' + string('acceptTextures') + (b'\x01' if policy == 'accept' else b'\x00')
    path.write_bytes(data + b'\x00\x00')
def main():
    parser = argparse.ArgumentParser(description='Focused real atlas command native/fallback network regression')
    parser.add_argument('--jar', type=Path, required=True)
    parser.add_argument('--label', required=True)
    parser.add_argument('--profiles', default='fabric,suite')
    parser.add_argument('--compile-only', action='store_true', help='Compile/package observation fixtures without launching any runtime')
    args = parser.parse_args()
    if set(args.profiles.split(',')) - {'fabric', 'suite'}: parser.error('Only fabric and suite profiles are supported')
    run = ROOT / 'qa-release/network/runs' / args.label; run.mkdir(parents=True, exist_ok=False)
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
    subprocess.run(['/usr/lib/jvm/java-27-openjdk/bin/javac', '--release', '25', '-proc:none', '-cp', os.pathsep.join(dict.fromkeys(paths)), '-d', str(classes), *map(str, (ROOT / 'qa-release/network/src').glob('*.java'))], check=True)
    for side in ('server', 'client'):
        entry = 'qa.atlasnetwork.AtlasNetwork' + side.title() + 'Qa'
        metadata = {'schemaVersion': 1, 'id': 'suite_atlas_network_qa_' + side, 'version': '1', 'environment': side,
                    'entrypoints': {'main' if side == 'server' else 'client': [entry]}, 'depends': {'fabric-api': '*'}, 'mixins': ['atlas-network.mixins.json'] if side == 'server' else []}
        with zipfile.ZipFile(fixtures / (side + '.jar'), 'w') as archive:
            archive.writestr('fabric.mod.json', json.dumps(metadata))
            if side == 'server': archive.writestr('atlas-network.mixins.json', json.dumps({'required': True, 'package': 'qa.atlasnetwork.mixin', 'compatibilityLevel': 'JAVA_25', 'mixins': ['AtlasPacketTraceMixin'], 'injectors': {'defaultRequire': 1}}))
            for file in classes.rglob('*.class'): archive.write(file, file.relative_to(classes))
    if args.compile_only:
        print('PASS observation fixtures compiled; no runtime launched',flush=True); return
    with zipfile.ZipFile(suite) as archive: version=json.loads(archive.read('fabric.mod.json'))['version']
    children=[]; observations=[]; env=os.environ.copy()
    env.update(DISPLAY=env.get('DISPLAY', ':1'), SDL_VIDEODRIVER='x11', SDL_VIDEO_X11_XINPUT2='0', LP_NUM_THREADS='2')
    def start(directory,command,mods):
        (directory/'mods').mkdir(parents=True,exist_ok=True)
        for jar in mods: shutil.copy2(jar,directory/'mods'/jar.name)
        (directory/'audit.json').write_text(json.dumps({'command':command,'mods':[{'file':p.name,'sha256':sha(p)} for p in mods]},indent=2))
        log=(directory/'console.log').open('w');process=subprocess.Popen(command,cwd=directory,env=env,stdin=subprocess.PIPE,stdout=log,stderr=subprocess.STDOUT,text=True)
        children.append((process,log));return process
    def read(name,side):
        try:return json.loads((control/(name+'-'+side+'.json')).read_text())
        except (FileNotFoundError,json.JSONDecodeError):return {}
    def wait(condition,process,label,timeout=140):
        deadline=time.time()+timeout
        while time.time()<deadline:
            if (control/'failure').exists():raise RuntimeError((control/'failure').read_text())
            if process.poll() is not None:raise RuntimeError(label+': process exited')
            if condition():return
            time.sleep(.5)
        raise TimeoutError(label)
    def stop(process,server=False):
        if process.poll() is None:
            if server:process.stdin.write('stop\n');process.stdin.flush()
            else:process.terminate()
            try:process.wait(timeout=25)
            except subprocess.TimeoutExpired:process.kill();process.wait()
    try:
        directory=run/'server';directory.mkdir();port=freeport();launch.copy_accepted_eula(directory)
        (directory/'server.properties').write_text(f'server-ip=127.0.0.1\nserver-port={port}\nonline-mode=false\nwhite-list=false\nenforce-secure-profile=false\nspawn-protection=0\nview-distance=2\nsimulation-distance=2\ndifficulty=peaceful\nlevel-type=minecraft:flat\ngenerator-settings={{"layers":[{{"block":"minecraft:bedrock","height":1}}],"biome":"minecraft:plains"}}\ngenerate-structures=false\n')
        command=launch.base_command('server',directory,port);command[0]='/usr/lib/jvm/java-25-openjdk/bin/java';command.insert(1,'-Datlas.qa.control='+str(control))
        server=start(directory,command,[suite,*dependencies,polymer,fixtures/'server.jar'])
        wait(lambda:'Done (' in (directory/'console.log').read_text(),server,'server startup')
        for profile in args.profiles.split(','):
            name='AtlasNet'+profile.title();clientdir=run/name;clientdir.mkdir()
            save_server(clientdir/'servers.dat',port,'decline')
            (clientdir/'options.txt').write_text('pauseOnLostFocus:false\nguiScale:2\ngraphicsMode:0\nrenderDistance:2\nmaxFps:25\nmaxFpsInactive:25\nsoundCategory_master:0.0\njoinedFirstServer:true\n')
            mods=[api,fixtures/'client.jar'] if profile=='fabric' else [suite,*dependencies,fixtures/'client.jar']
            command=launch.base_command('native',clientdir,port);command[0]='/usr/lib/jvm/java-25-openjdk/bin/java';command.insert(1,'-Datlas.qa.control='+str(control))
            command[command.index('--username')+1]=name
            client=start(clientdir,command,mods)
            wait(lambda:read(name,'client').get('world_ticks',0)>=40,client,name+' connected')
            before=read(name,'client')['world_ticks'];(control/(name+'-execute')).write_text('run actual commands')
            wait(lambda:read(name,'server').get('dedupe_result')==1,client,name+' commands executed')
            wait(lambda:read(name,'client').get('world_ticks',0)>=before+100 and read(name,'server').get('connected_age_ticks',0)>=read(name,'server').get('executed_at_age',999999)+100,client,name+' remains connected after commands')
            (control/(name+'-cartography-open')).write_text('open real vanilla cartography menu')
            wait(lambda:'cartography' in read(name,'client') and 'cartography' in read(name,'server'),client,name+' real cartography menu opened')
            click_observations=[]
            for stage,slot,quick in [(0,30,False),(1,0,False),(2,31,False),(3,1,False),(4,2,True)]:
                (control/(name+'-client-click.json')).write_text(json.dumps({'stage':stage,'slot':slot,'quick':quick}))
                def settled():
                    server_menu=read(name,'server').get('cartography',{});client_row=read(name,'client');client_menu=client_row.get('cartography',{})
                    if not any(c['stage']==stage for c in client_row.get('clicks',[])):return False
                    if stage==0:return server_menu.get('carried_atlas',False) and client_menu.get('carried_count')==1
                    if stage==1:return server_menu.get('top_atlas',False) and server_menu.get('carried_count')==0 and client_menu.get('top_count')==1 and client_menu.get('carried_count')==0
                    if stage==2:return server_menu.get('carried_book',False) and client_menu.get('carried_count')==1
                    if stage==3:return server_menu.get('output_atlas',False) and server_menu.get('book_count')==1 and client_menu.get('book_count')==1 and client_menu.get('output_count')==1 and client_menu.get('carried_count')==0
                    return server_menu.get('complete',False) and client_menu.get('top_count')==1 and client_menu.get('book_count')==0 and client_menu.get('output_count')==0 and client_menu.get('carried_count')==0
                wait(settled,client,name+' authoritative cartography click '+str(stage),60)
                click_observations.append({'stage':stage,'slot':slot,'quick_move':quick,'client':read(name,'client'),'server_cartography':read(name,'server')['cartography']})
            clicks=read(name,'client')['clicks'];assert all(c['local_book_may_place']==(profile=='suite') for c in clicks),clicks
            after_gui=read(name,'client')['world_ticks'];wait(lambda:read(name,'client').get('world_ticks',0)>=after_gui+60,client,name+' remains connected after GUI copy')
            row=read(name,'server');payloads=[e for e in row['events'][row['packet_event_start']:] if e['kind']=='custom-payload']
            refresh=[e for e in payloads if e['channel']=='mapstitch_mixed_scales:refresh_maps_v1']
            assert row['player_still_connected'] and read(name,'client')['connected'],row
            assert all(e['advertised'] for e in payloads if not e['channel'].startswith(('minecraft:','fabric:'))),payloads
            assert row['native_mapstitch']==(profile=='suite'),row
            for channel, module in [('simple_smithing_overhaul:repairables','sso'), ('sensible_stackables:stack_sizes','stackables')]:
                initial=[e for e in row['events'][:row['config_refresh_event_start']] if e['kind']=='custom-payload' and e['channel']==channel]
                hot=[e for e in row['events'][row['config_refresh_event_start']:row['config_refresh_event_end']] if e['kind']=='custom-payload' and e['channel']==channel]
                assert row['native_'+module]==(profile=='suite'),row
                assert len(initial)==(1 if profile=='suite' else 0),initial
                assert len(hot)==(1 if profile=='suite' else 0),hot
                assert all(e['advertised'] for e in initial+hot),initial+hot
            assert all(e['advertised'] for e in row['events'] if e['kind']=='custom-payload' and e['channel'] in ('simple_smithing_overhaul:repairables','sensible_stackables:stack_sizes')),row

            assert row['refresh_advertised']==(profile=='suite'),row
            assert len(refresh)==(3 if profile=='suite' else 0),payloads
            assert row['first_repair_full_snapshots']>=1 and row['repeat_repair_full_snapshots']>=1,row
            assert row['check_full_snapshots']==0 and row['read_only_check_unchanged'] and row['repeat_repair_unchanged'],row
            assert any(e['kind']=='vanilla-map-data' for e in row['events'][row['packet_event_start']:]),row
            observations.append({'profile':profile,'commands':['atlas fix','atlas fix check','atlas repair check','atlas repair','atlas dedupe','atlas makecopy'],'client':read(name,'client'),'server':row,'command_custom_payloads':payloads,'cartography_clicks':click_observations})
            stop(client);print('PASS '+profile+': initial/repeated repair full snapshots, read-only checks, dedupe, makecopy and real cartography clicks, connected, refresh='+str(len(refresh)),flush=True)
        stop(server,True)
        (run/'result.json').write_text(json.dumps({'passed':True,'version':version,'bundle_sha256':sha(suite),'run_label':args.label,'profiles':args.profiles.split(','),'observations':observations,'scope':'Native suite/Fabric API-only connections, new repairables/stack-size initial and config-refresh payload guards, atlas maintenance and cartography network safety. No broader gameplay or resource-pack matrix repeated.'},indent=2)+'\n')
        print('PASS focused network checks',flush=True)
    finally:
        for process,log in reversed(children):
            if process.poll() is None:
                process.terminate()
                try:process.wait(timeout=15)
                except subprocess.TimeoutExpired:process.kill();process.wait()
            log.close()

if __name__=='__main__':main()
