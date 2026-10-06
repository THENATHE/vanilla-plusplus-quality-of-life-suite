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
    parser.add_argument('--profiles', default='polymer,native,nosso')
    parser.add_argument('--observe', action='store_true')
    parser.add_argument('--addon',type=Path,help='Replace only nested addon in copied candidate; production/root artifact unchanged')
    parser.add_argument('--compile-only', action='store_true', help='Compile/package observation fixtures without launching any runtime')
    args = parser.parse_args()
    if set(args.profiles.split(',')) - {'polymer','native','nosso'}: parser.error('Only polymer/native/nosso profiles supported')
    run = ROOT / 'qa-release/pouch-mending/runs' / args.label; run.mkdir(parents=True, exist_ok=False)
    control = run / 'control'; control.mkdir()
    fixtures = run / 'fixtures'; fixtures.mkdir(); classes = fixtures / 'classes'; classes.mkdir()
    launch = mapstitch_launch(WORKSPACE); suite = run / args.jar.name; shutil.copy2(args.jar.resolve(), suite)
    original_sha=sha(suite)
    if args.addon:
        with zipfile.ZipFile(suite) as archive: members={n:archive.read(n) for n in archive.namelist()}
        metadata=json.loads(members['fabric.mod.json']);old=next(j['file'] for j in metadata['jars'] if 'toolpouch-atlas-elytra-compat-' in j['file'])
        target='META-INF/jars/'+args.addon.name
        members.pop(old);members[target]=args.addon.read_bytes()
        for j in metadata['jars']:
            if j['file']==old:j['file']=target
        members['fabric.mod.json']=json.dumps(metadata).encode()
        with zipfile.ZipFile(suite,'w',zipfile.ZIP_DEFLATED) as archive:
            for n,data in members.items():archive.writestr(n,data)
    dependencies = [ROOT / 'libs' / name for name in (
        'codecui-26.3-1.4.3-fabric.jar', 'defaulted-1.3.8+26.3.dropfix.1-fabric.jar',
        'fabric-language-kotlin-1.14.1+kotlin.2.4.20.jar', 'fzzy_config-0.7.7+fix2+26.3.jar', 'mixson-2.2.1-multiloader.jar')]
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
    subprocess.run(['/usr/lib/jvm/java-27-openjdk/bin/javac', '--release', '25', '-proc:none', '-cp', os.pathsep.join(dict.fromkeys(paths)), '-d', str(classes), *map(str, (ROOT / 'qa-release/pouch-mending/src').glob('*.java'))], check=True)
    fixture=fixtures/'qa-server.jar'
    with zipfile.ZipFile(fixture,'w') as archive:
        archive.writestr('fabric.mod.json',json.dumps({'schemaVersion':1,'id':'suite_pouch_mending_qa','version':'1','environment':'server','entrypoints':{'main':['qa.pouchmending.PouchMendingQa']},'depends':{'fabric-api':'*'}}))
        for file in classes.rglob('*.class'):archive.write(file,file.relative_to(classes))
    if args.compile_only:return
    fixture_hashes={str(p.relative_to(ROOT)):sha(p) for p in (ROOT/'qa-release/pouch-mending/src').glob('*.java')};fixture_hashes['qa-release/pouch-mending/run.py']=sha(Path(__file__))
    results={'fixture_inputs':fixture_hashes,'observe_only':args.observe,'passed':False,'original_bundle_sha256':original_sha,'tested_bundle_sha256':sha(suite),'addon_override_sha256':sha(args.addon) if args.addon else None,'profiles':[]}
    try:
        for profile in args.profiles.split(','):
            directory=run/profile;directory.mkdir();launch.copy_accepted_eula(directory)
            (directory/'mods').mkdir()
            selected=[suite,*dependencies,fixture]+([polymer] if profile=='polymer' else [])
            if profile=='nosso':
                originals=[]
                for path in fixtures.glob('*.jar'):
                    with zipfile.ZipFile(path) as archive:
                        if 'fabric.mod.json' not in archive.namelist():continue
                        metadata=json.loads(archive.read('fabric.mod.json'))
                        if metadata.get('id') in {'toolpouch','toolpouch_atlas_elytra_compat'}:originals.append(path)
                assert len(originals)==2,originals
                selected=[*originals,*dependencies,fixture]
            for jar in selected:shutil.copy2(jar,directory/'mods'/jar.name)
            (directory/'server.properties').write_text('server-ip=127.0.0.1\nserver-port=0\nonline-mode=false\nwhite-list=false\nenforce-secure-profile=false\nview-distance=2\nsimulation-distance=2\nlevel-type=minecraft:flat\ngenerator-settings={"layers":[{"block":"minecraft:bedrock","height":1}],"biome":"minecraft:plains"}\ngenerate-structures=false\n')
            command=launch.base_command('server',directory,0);command[0]='/usr/lib/jvm/java-25-openjdk/bin/java';command.insert(1,'-Dmixin.debug.export=true')
            if args.observe:command.insert(1,'-Dpouch.qa.observe=true')
            (directory/'audit.json').write_text(json.dumps({'mods':[{'file':p.name,'sha256':sha(p)} for p in selected],'command':command},indent=2))
            with (directory/'console.log').open('w') as log:
                proc=subprocess.Popen(command,cwd=directory,stdout=log,stderr=subprocess.STDOUT)
                try:code=proc.wait(timeout=120)
                except subprocess.TimeoutExpired:proc.kill();proc.wait();raise
            observation=json.loads((directory/'pouch-mending-result.json').read_text());results['profiles'].append({'profile':profile,'exit_code':code,'result':observation})
            print(json.dumps(results['profiles'][-1]),flush=True)
            if code!=0 or not observation['passed']:raise AssertionError(observation)
        results['passed']=True
    finally:(run/'result.json').write_text(json.dumps(results,indent=2)+'\n')
if __name__=='__main__':main()
