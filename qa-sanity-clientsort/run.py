#!/usr/bin/env python3
"""Real native ClientSort button and operation compatibility regression."""
import argparse, hashlib, importlib.util, json, os, shutil, subprocess, time, zipfile
from pathlib import Path
HERE = Path(__file__).resolve().parent
SUITE = HERE.parent
ROOT = SUITE.parents[1]
import sys
sys.path.insert(0,str(SUITE/'tools'))
from workspace_paths import mapstitch_launch
launch = mapstitch_launch(ROOT)

def sha(path): return hashlib.sha256(path.read_bytes()).hexdigest()
def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--track', choices=['developer'], default='developer')
    parser.add_argument('--server-clientsort', action='store_true')
    parser.add_argument('--refill-buttons', action='store_true')
    parser.add_argument('--label', required=True)
    parser.add_argument('--port', type=int)
    parser.add_argument('--candidate', action='store_true', help='Expect native ClientSort support on bag menus')
    parser.add_argument('--prepare-only',action='store_true')
    parser.add_argument('--suite',type=Path,default=SUITE/'build/libs/vanilla-plusplus-quality-of-life-suite-1.1.5+26.3.jar')
    args=parser.parse_args(); port=args.port or 26084
    run=HERE/'runs'/args.label
    run.mkdir(parents=True,exist_ok=False)
    control=run/'control';control.mkdir();build=run/'fixtures';build.mkdir();classes=build/'classes';classes.mkdir()
    suite=run/args.suite.name;shutil.copy2(args.suite,suite)
    dependencies=[SUITE/'libs'/n for n in ['codecui-26.3-1.4.3-fabric.jar','fabric-language-kotlin-1.14.1+kotlin.2.4.20.jar','fzzy_config-0.7.7+fix3+26.3.jar','mixson-2.2.1-multiloader.jar']]
    dependencies += [launch.artifact('net.fabricmc.fabric-api','fabric-api','0.161.0+26.3'),launch.artifact('me.shedaniel.cloth','cloth-config-fabric','26.3.159')]
    clientsort=ROOT/'Backups/General Utilities (Non-WP)/Minecraft/toolpouch-atlas-elytra-compat-26.3/Snapshots/2026-10-04-suite-consolidation/qa/clientsort/fixtures/clientsort-fabric-3.104.1+26.3.jar'
    native=[suite,*dependencies,clientsort]
    server=[suite,*dependencies,ROOT/'Builds/Minecraft/Polymer/Main Plugin/0.18.2+26.3/polymer-bundled-0.18.2+26.3.jar']
    if args.server_clientsort:server.append(clientsort)
    mods=list(dict.fromkeys(server+native)); server_audit,client_audit=launch.audits()
    paths=launch.cp(server_audit['command'])+launch.cp(client_audit['command'])+list(map(str,mods))
    for jar in mods:
        with zipfile.ZipFile(jar) as archive:
            for member in archive.namelist():
                if member.endswith('.jar'):
                    target=build/Path(member).name;target.write_bytes(archive.read(member));paths.append(str(target))
    sources=list((HERE/'src').glob('*.java'))
    if args.candidate and args.server_clientsort:sources+=list((HERE/'boundaries').glob('*.java'))
    compile_result=subprocess.run(['/usr/lib/jvm/java-27-openjdk/bin/javac','--release','25','-proc:none','-cp',os.pathsep.join(dict.fromkeys(paths)),'-d',str(classes),*map(str,sources)])
    if compile_result.returncode:raise RuntimeError('QA fixture compilation failed; see compiler diagnostics above')
    for side in ('server','client'):
        with zipfile.ZipFile(build/f'{side}.jar','w') as archive:
            metadata={'schemaVersion':1,'id':'clientsort_native_qa_'+side,'version':'1','environment':side,'entrypoints':{'main' if side=='server' else 'client':['clientsortqa.Sort'+side.title()+'Qa']},'depends':{'toolpouch':'*','fabric-api':'*'}}
            if side=='client':
                metadata['mixins']=['clientsort-qa.mixins.json']
                archive.writestr('clientsort-qa.mixins.json',json.dumps({'required':True,'package':'clientsortqa.mixin','compatibilityLevel':'JAVA_25','client':['SortClientOperatorTraceMixin'],'injectors':{'defaultRequire':1}}))
            archive.writestr('fabric.mod.json',json.dumps(metadata))
            for path in classes.rglob('*'+side.title()+'*.class'):archive.write(path,path.relative_to(classes))
    if args.prepare_only:
        print('Fixtures compiled; no runtime launched');return
    before={str(p):sha(p) for p in mods};children=[];audits={}
    env=os.environ.copy();env.update(DISPLAY=env.get('DISPLAY',':1'),SDL_VIDEODRIVER='x11',SDL_VIDEO_X11_XINPUT2='0',LP_NUM_THREADS='3')
    try:
        for side,mode,selected in [('server','server',server),('client','native',native)]:
            directory=run/side;moddir=directory/'mods';moddir.mkdir(parents=True,exist_ok=False)
            selected=selected+[build/f'{side}.jar']
            for path in selected:shutil.copy2(path,moddir/path.name)
            if side=='server':
                launch.copy_accepted_eula(directory)
                (directory/'server.properties').write_text(f'server-ip=127.0.0.1\nserver-port={port}\nonline-mode=false\nwhite-list=false\nenforce-secure-profile=false\nview-distance=2\nsimulation-distance=2\nlevel-name=qa-world\nlevel-seed=927436\nlevel-type=minecraft:flat\ngenerator-settings={{"layers":[{{"block":"minecraft:bedrock","height":1}},{{"block":"minecraft:dirt","height":2}},{{"block":"minecraft:grass_block","height":1}}],"biome":"minecraft:plains"}}\ngenerate-structures=false\ndifficulty=peaceful\n')
            else:(directory/'options.txt').write_text('pauseOnLostFocus:false\ngraphicsMode:0\nrenderDistance:3\nsimulationDistance:5\nmaxFps:30\nmaxFpsInactive:30\nsoundCategory_master:0.0\njoinedFirstServer:true\ntutorialStep:none\n')
            command=launch.base_command(mode,directory,port);command[0]='/usr/lib/jvm/java-25-openjdk/bin/java';command.insert(1,'-Dclientsort.qa.control='+str(control))
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
        evidence={'track':args.track,'candidate_expectations':args.candidate,'server_clientsort':args.server_clientsort,'refill_buttons':args.refill_buttons,'suite_sha256':sha(suite),'artifacts':audits,'original_jars_unchanged':before==after}
        for filename in ('result.txt','observations.txt','client-policy.txt'):
            if (control/filename).exists():evidence[filename]=(control/filename).read_text()
        (run/'evidence.json').write_text(json.dumps(evidence,indent=2)+'\n')
        assert before==after
if __name__=='__main__':main()
