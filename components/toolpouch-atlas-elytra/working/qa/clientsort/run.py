#!/usr/bin/env python3
"""Real native ClientSort button and operation compatibility regression."""
import argparse, hashlib, importlib.util, json, os, shutil, subprocess, time, zipfile
from pathlib import Path
HERE = Path(__file__).resolve().parent
ROOT = next(path for path in HERE.parents if (path/'Minecraft/polymer-shim-test-bundle').is_dir())
MC = ROOT/'Minecraft'
BUNDLE = MC/'polymer-shim-test-bundle/staging-2026-10-01'
spec = importlib.util.spec_from_file_location('launch', MC/'mapstitch-polymer-compat-26.3/qa/launch.py')
launch = importlib.util.module_from_spec(spec); spec.loader.exec_module(launch)

def sha(path): return hashlib.sha256(path.read_bytes()).hexdigest()
def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--track', choices=['developer'], default='developer')
    parser.add_argument('--server-clientsort', action='store_true')
    parser.add_argument('--refill-buttons', action='store_true')
    parser.add_argument('--label', required=True)
    parser.add_argument('--port', type=int)
    parser.add_argument('--candidate', action='store_true', help='Expect native ClientSort support on bag menus')
    parser.add_argument('--shim', type=Path, default=ROOT/'Builds/Minecraft/Multi-Shim/Combined Compatibility Shim - Developer Release Targets/1.0.3+26.3/SSO-backpack-toolpouch-mapstitch-shim-1.0.3+26.3.jar')
    parser.add_argument('--addon', type=Path, default=ROOT/'Builds/Minecraft/Tool Pouch/Feature Addon - Atlas and Elytra/1.0.3+26.3/toolpouch-atlas-elytra-compat-1.0.3+26.3.jar')
    args=parser.parse_args(); port=args.port or (25946 if args.track=='developer' else 25947)
    run=HERE/'runs'/args.label
    run.mkdir(parents=True,exist_ok=False)
    control=run/'control';control.mkdir();build=run/'fixtures';build.mkdir();classes=build/'classes';classes.mkdir()
    native=list((BUNDLE/'native-client/mods').glob('*.jar'))
    server=[p for p in (BUNDLE/'mods').glob('*.jar') if not p.name.startswith(('simple-smithing-polymer-compat-','tiered-backpacks-polymer-compat-','toolpouch-polymer-compat-','mapstitch-polymer-compat-'))]+[args.shim.resolve(),args.addon.resolve()]
    native+=[args.addon.resolve()]
    clientsort=HERE/'fixtures/clientsort-fabric-3.104.1+26.3.jar'
    native.append(clientsort)
    if args.server_clientsort:server.append(clientsort)
    tiered_override = Path(os.environ['TIERED_QA_JAR']).resolve() if os.environ.get('TIERED_QA_JAR') else None
    if tiered_override and args.track != 'isolated':
        with zipfile.ZipFile(tiered_override) as archive:
            assert json.loads(archive.read('fabric.mod.json'))['id'] == 'tiered_backpacks'
        server = [p for p in server if not p.name.startswith('tiered_backpacks-')] + [tiered_override]
        native = [p for p in native if not p.name.startswith('tiered_backpacks-')] + [tiered_override]
    tiered=MC/'SSO-backpack-toolpouch-mapstitch-shim/qa/tiered-1.0.20/fixtures/tiered_backpacks-fabric-1.0.20+26.3.jar'
    if args.track!='isolated':
        server=[p for p in server if not p.name.startswith('tiered_backpacks-')]+[tiered]
        native=[p for p in native if not p.name.startswith('tiered_backpacks-')]+[tiered]
    mods=list(dict.fromkeys(server+native)); server_audit,client_audit=launch.audits()
    paths=launch.cp(server_audit['command'])+launch.cp(client_audit['command'])+list(map(str,mods))
    for jar in mods:
        with zipfile.ZipFile(jar) as archive:
            for member in archive.namelist():
                if member.endswith('.jar'):
                    target=build/Path(member).name;target.write_bytes(archive.read(member));paths.append(str(target))
    sources=list((HERE/'src').glob('*.java'))
    if args.candidate and args.server_clientsort:sources+=list((HERE/'boundaries').glob('*.java'))
    compile_result=subprocess.run(['javac','--release','25','-proc:none','-cp',os.pathsep.join(dict.fromkeys(paths)),'-d',str(classes),*map(str,sources)])
    if compile_result.returncode:raise RuntimeError('QA fixture compilation failed; see compiler diagnostics above')
    for side in ('server','client'):
        with zipfile.ZipFile(build/f'{side}.jar','w') as archive:
            metadata={'schemaVersion':1,'id':'clientsort_native_qa_'+side,'version':'1','environment':side,'entrypoints':{'main' if side=='server' else 'client':['clientsortqa.Sort'+side.title()+'Qa']},'depends':{'toolpouch':'*','fabric-api':'*'}}
            if side=='client':
                metadata['mixins']=['clientsort-qa.mixins.json']
                archive.writestr('clientsort-qa.mixins.json',json.dumps({'required':True,'package':'clientsortqa.mixin','compatibilityLevel':'JAVA_25','client':['SortClientOperatorTraceMixin'],'injectors':{'defaultRequire':1}}))
            archive.writestr('fabric.mod.json',json.dumps(metadata))
            for path in classes.rglob('*'+side.title()+'*.class'):archive.write(path,path.relative_to(classes))
    before={str(p):sha(p) for p in mods};children=[];audits={}
    env=os.environ.copy();env.update(SDL_VIDEODRIVER='x11',SDL_VIDEO_X11_XINPUT2='0',LP_NUM_THREADS='3')
    try:
        for side,mode,selected in [('server','server',server),('client','native',native)]:
            directory=run/side;moddir=directory/'mods';moddir.mkdir(parents=True,exist_ok=False)
            selected=selected+[build/f'{side}.jar']
            for path in selected:shutil.copy2(path,moddir/path.name)
            if side=='server':
                eula=MC/'toolpouch-atlas-elytra-compat-26.3/qa/elytra-client/runs/server/eula.txt';assert 'eula=true' in eula.read_text().splitlines();shutil.copy2(eula,directory/'eula.txt')
                (directory/'server.properties').write_text(f'server-ip=127.0.0.1\nserver-port={port}\nonline-mode=false\nwhite-list=false\nenforce-secure-profile=false\nview-distance=2\nsimulation-distance=2\nlevel-name=qa-world\nlevel-seed=927436\nlevel-type=minecraft:flat\ngenerator-settings={{"layers":[{{"block":"minecraft:bedrock","height":1}},{{"block":"minecraft:dirt","height":2}},{{"block":"minecraft:grass_block","height":1}}],"biome":"minecraft:plains"}}\ngenerate-structures=false\ndifficulty=peaceful\n')
            else:(directory/'options.txt').write_text('pauseOnLostFocus:false\ngraphicsMode:0\nrenderDistance:3\nsimulationDistance:5\nmaxFps:30\nmaxFpsInactive:30\nsoundCategory_master:0.0\njoinedFirstServer:true\ntutorialStep:none\n')
            command=launch.base_command(mode,directory,port);command.insert(1,'-Dclientsort.qa.control='+str(control))
            if args.candidate:command.insert(1,'-Dclientsort.qa.candidate=true')
            if args.server_clientsort:command.insert(1,'-Dclientsort.qa.serverInstalled=true')
            if args.refill_buttons:command.insert(1,'-Dclientsort.qa.refillButtons=true')
            audits[side]={'mods':[{'filename':p.name,'sha256':sha(p)} for p in selected],'qa_fixture':True}
            suffix=''
            (directory/f'launch-audit{suffix}.json').write_text(json.dumps({'command':command,**audits[side]},indent=2))
            logpath=directory/f'console{suffix}.log';log=logpath.open('w');child=subprocess.Popen(command,cwd=directory,env=env,stdin=subprocess.PIPE,stdout=log,stderr=subprocess.STDOUT,text=True);children.append((child,log,side))
            if side=='server':
                for _ in range(150):
                    if child.poll() is not None:raise RuntimeError('Server exited: '+str(logpath))
                    if 'Done (' in logpath.read_text():break
                    time.sleep(1)
                else:raise TimeoutError('server startup')
        for _ in range(1200):
            result=control/'result.txt'
            if result.exists():
                print(result.read_text(),flush=True)
                if not result.read_text().startswith('COMPLETE'):raise RuntimeError('Fixture failed')
                break
            if any(child.poll() is not None for child,_,_ in children):raise RuntimeError('Process exited')
            time.sleep(1)
        else:raise TimeoutError('client suite')
    finally:
        for child,log,side in reversed(children):
            if child.poll() is None:
                if side=='server':
                    try:child.stdin.write('stop\n');child.stdin.flush()
                    except BrokenPipeError:pass
                else:child.terminate()
                try:child.wait(timeout=25)
                except subprocess.TimeoutExpired:child.kill();child.wait()
            log.close()
        after={str(p):sha(p) for p in mods}
        evidence={'track':args.track,'candidate_expectations':args.candidate,'server_clientsort':args.server_clientsort,'refill_buttons':args.refill_buttons,'artifacts':audits,'original_jars_unchanged':before==after}
        for filename in ('result.txt','observations.txt','client-policy.txt'):
            if (control/filename).exists():evidence[filename]=(control/filename).read_text()
        (run/'evidence.json').write_text(json.dumps(evidence,indent=2)+'\n')
        assert before==after
if __name__=='__main__':main()
