#!/usr/bin/env python3
"""Focused native suite settings validation using cached official runtime files."""
import argparse,hashlib,importlib.util,json,os,shutil,subprocess,time,zipfile,uuid
from pathlib import Path
HERE=Path(__file__).resolve().parent
SUITE=HERE.parent
ROOT=SUITE.parents[1]
spec=importlib.util.spec_from_file_location('launch',ROOT/'Minecraft/mapstitch-polymer-compat-26.3/qa/launch.py')
launch=importlib.util.module_from_spec(spec);spec.loader.exec_module(launch)
def sha(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument('--visual-only',action='store_true');p.add_argument('--guest',action='store_true');p.add_argument('--suite',type=Path,default=SUITE/'build/libs/vanilla-plusplus-quality-of-life-suite-1.0.0+26.3.jar')
    p.add_argument('--label',required=True);p.add_argument('--prepare-only',action='store_true');p.add_argument('--settings-only',action='store_true');p.add_argument('--port',type=int,default=25976)
    args=p.parse_args();suite=args.suite.resolve();assert suite.is_file();suiteHash=sha(suite)
    run=HERE/'runs'/args.label;run.mkdir(parents=True,exist_ok=False);control=run/'control';control.mkdir();fixtures=run/'fixtures';fixtures.mkdir();classes=fixtures/'classes';classes.mkdir()
    frozen=run/suite.name;shutil.copy2(suite,frozen);assert sha(frozen)==suiteHash;suite=frozen
    # Original runtime dependencies remain separate; component mods are nested in suite.
    deps=[SUITE/'libs'/n for n in ['codecui-26.3-1.4.3-fabric.jar','defaulted-1.3.8+26.3.dropfix.1-fabric.jar','fabric-language-kotlin-1.14.1+kotlin.2.4.20.jar','fzzy_config-0.7.7+fix2+26.3.jar','mixson-2.2.1-multiloader.jar']]
    deps+=[launch.artifact('net.fabricmc.fabric-api','fabric-api','0.161.0+26.3'),launch.artifact('me.shedaniel.cloth','cloth-config-fabric','26.3.159'),launch.artifact('com.terraformersmc','modmenu','21.0.0')]
    selected=[suite]+deps
    serverAudit,clientAudit=launch.audits();paths=launch.cp(serverAudit['command'])+launch.cp(clientAudit['command'])+[str(p) for p in selected]+[str(launch.artifact('io.github.llamalad7','mixinextras-fabric','0.5.5'))]
    def extract(jar):
        with zipfile.ZipFile(jar) as z:
            for n in z.namelist():
                if n.endswith('.jar'):
                    blob=z.read(n);dest=fixtures/(hashlib.sha256(blob).hexdigest()[:12]+'-'+Path(n).name)
                    if not dest.exists():dest.write_bytes(blob);paths.append(str(dest));extract(dest)
    for jar in selected:extract(jar)
    subprocess.run(['/usr/lib/jvm/java-27-openjdk/bin/javac','--release','25','-proc:none','-cp',os.pathsep.join(dict.fromkeys(paths)),'-d',str(classes),*map(str,(HERE/'src').glob('*.java'))],check=True)
    for side in ['client','server']:
        with zipfile.ZipFile(fixtures/f'{side}.jar','w') as z:
            meta={'schemaVersion':1,'id':'suite_settings_qa_'+side,'version':'1','environment':side,'entrypoints':{'client' if side=='client' else 'main':['suitesettingsqa.Settings'+side.title()+'Qa']},'depends':{'fabric-api':'*','thenathe_mod_suite':'*'}}
            z.writestr('fabric.mod.json',json.dumps(meta))
            for file in classes.rglob('*.class'):z.write(file,file.relative_to(classes))
    if args.prepare_only:
        (run/'prepare.json').write_text(json.dumps({'suite':str(suite),'suite_sha256':suiteHash,'fixtures_compiled':True,'runtime_launched':False},indent=2)+'\n')
        print('Fixtures compiled; no runtime launched');return
    children=[];env=os.environ.copy();env.update(SDL_VIDEODRIVER='x11',SDL_VIDEO_X11_XINPUT2='0',LP_NUM_THREADS='3',DISPLAY=env.get('DISPLAY',':1'))
    audits={}
    try:
        sides=['client'] if args.settings_only else ['server','client']
        for side in sides:
            directory=run/side;mods=directory/'mods';mods.mkdir(parents=True)
            chosen=selected+[fixtures/f'{side}.jar']
            if side=='server':chosen+=[ROOT/'Builds/Minecraft/Polymer/Main Plugin/0.18.2+26.3/polymer-bundled-0.18.2+26.3.jar']
            for jar in chosen:shutil.copy2(jar,mods/jar.name)
            if side=='server':
                launch.copy_accepted_eula(directory)
                profile=uuid.UUID(bytes=hashlib.md5(b'OfflinePlayer:SettingsQa').digest(),version=3)
                (directory/'ops.json').write_text(json.dumps([] if args.guest else [dict(uuid=str(profile),name='SettingsQa',level=4,bypassesPlayerLimit=True)]))
                (directory/'server.properties').write_text(f'server-ip=127.0.0.1\nserver-port={args.port}\nonline-mode=false\nwhite-list=false\nenforce-secure-profile=false\nspawn-protection=0\nview-distance=2\nsimulation-distance=2\ndifficulty=peaceful\nlevel-type=minecraft:flat\ngenerator-settings={{"biome":"minecraft:plains","layers":[{{"block":"minecraft:bedrock","height":1}},{{"block":"minecraft:dirt","height":2}},{{"block":"minecraft:grass_block","height":1}}]}}\ngenerate-structures=false\n')
            else:(directory/'options.txt').write_text('pauseOnLostFocus:false\nguiScale:2\ngraphicsMode:0\nrenderDistance:3\nsimulationDistance:3\nmaxFps:30\nmaxFpsInactive:30\nsoundCategory_master:0.0\njoinedFirstServer:true\n')
            command=launch.base_command('server' if side=='server' else 'native',directory,args.port)
            command[0]='/usr/lib/jvm/java-25-openjdk/bin/java'
            if side=='client':command[command.index('--username')+1]='SettingsQa'
            command.insert(1,'-Dsettings.qa.control='+str(control))
            command.insert(1,'-Djava.awt.headless=true')
            if args.guest:command.insert(1,'-Dsettings.qa.guest=true')
            if args.visual_only:command.insert(1,'-Dsettings.qa.visual=true')
            if args.settings_only:
                command.insert(1,'-Dsettings.qa.standalone=true');index=command.index('--quickPlayMultiplayer');del command[index:index+2]
            audits[side]={'command':command,'mods':[{'file':j.name,'sha256':sha(j)} for j in chosen]}
            (directory/'launch-audit.json').write_text(json.dumps(audits[side],indent=2))
            logpath=directory/'console.log';log=logpath.open('w');child=subprocess.Popen(command,cwd=directory,env=env,stdin=subprocess.PIPE,stdout=log,stderr=subprocess.STDOUT,text=True);children.append((child,log,side))
            if side=='server':
                for _ in range(120):
                    if child.poll() is not None:raise RuntimeError('server exited: '+str(logpath))
                    if 'Done (' in logpath.read_text():break
                    time.sleep(1)
                else:raise TimeoutError('server startup')
        for _ in range(240):
            if (control/'failure').exists():raise RuntimeError((control/'failure').read_text())
            if (control/'result.txt').exists():print((control/'result.txt').read_text());break
            if any(c.poll() is not None for c,_,_ in children):raise RuntimeError('runtime exited; inspect console.log')
            time.sleep(1)
        else:raise TimeoutError('settings validation')
    finally:
        for child,log,side in reversed(children):
            if child.poll() is None:
                if side=='server':
                    try:child.stdin.write('stop\n');child.stdin.flush()
                    except BrokenPipeError:pass
                else:child.terminate()
                try:child.wait(timeout=20)
                except subprocess.TimeoutExpired:child.kill();child.wait()
            log.close()
        evidence={'suite':str(suite),'suite_sha256':suiteHash,'settings_only':args.settings_only,'launches':audits}
        for name in ['result.txt','observations.json','failure']:
            if (control/name).exists():evidence[name]=(control/name).read_text()
        (run/'evidence.json').write_text(json.dumps(evidence,indent=2)+'\n')
if __name__=='__main__':main()
