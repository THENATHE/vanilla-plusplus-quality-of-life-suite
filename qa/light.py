#!/usr/bin/env python3
"""Bounded suite connection smoke, cached libraries, disposable worlds, no downloads."""
from pathlib import Path
import argparse, hashlib, importlib.util, json, os, shutil, socket, subprocess, time, uuid, zipfile
ROOT = Path(__file__).resolve().parents[1]
WORKSPACE = ROOT.parents[1]
STAGE = WORKSPACE / 'Minecraft/polymer-shim-test-bundle/staging-2026-10-01/mods'
JAVA = '/usr/lib/jvm/java-25-openjdk/bin/java'
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--jar', type=Path, default=ROOT / 'build/libs/thenathe-mod-suite-1.0.0+26.3.jar')
parser.add_argument('--label', default='light-'+time.strftime('%Y%m%d-%H%M%S'))
args = parser.parse_args()
RUN = ROOT / 'qa/runs' / args.label
RUN.mkdir(parents=True, exist_ok=False)
spec=importlib.util.spec_from_file_location('cached_cp', WORKSPACE/'Minecraft/chalk-polymer-shim/qa/run.py')
helper=importlib.util.module_from_spec(spec);spec.loader.exec_module(helper)
servercp,clientcp,info=helper.prepare_classpaths('26.3',RUN/'cached-libraries')
sha=lambda path:hashlib.sha256(path.read_bytes()).hexdigest()
bundle=RUN/args.jar.name;shutil.copy2(args.jar,bundle)
with zipfile.ZipFile(bundle) as z:
    assert z.testzip() is None
    metadata=json.loads(z.read('fabric.mod.json'))
    featurefiles={Path(x['file']).name for x in metadata['jars']}
def fabric_mod(path):
    with zipfile.ZipFile(path) as z:return 'fabric.mod.json' in z.namelist()
external=[p for p in (ROOT/'libs').glob('*.jar') if p.name not in featurefiles and fabric_mod(p)]
external += [STAGE/'cloth-config-fabric-26.3.159.jar',STAGE/'fabric-api-0.161.0+26.3.jar']
polymer=STAGE/'polymer-bundled-0.18.2+26.3.jar'
patched_defaulted=next(p for p in external if p.name.startswith('defaulted-'))
assert sha(patched_defaulted)=='e339d6f0eb471a4ac41185fb9dbe0cfaa78a290c6ceedf92a49ba9110f732c61'
# Compile only the two bounded observer fixtures, never changing the production bundle.
fixture=RUN/'fixture';classes=fixture/'classes';classes.mkdir(parents=True)
paths=[*servercp,*clientcp,bundle,*external,polymer]
def nested(path):
    with zipfile.ZipFile(path) as z:
        for name in z.namelist():
            if name.endswith('.jar'):
                data=z.read(name);dest=fixture/(hashlib.sha256(data).hexdigest()[:12]+'-'+Path(name).name)
                dest.write_bytes(data);paths.append(dest);nested(dest)
for mod in [bundle,*external,polymer]:nested(mod)
# Fabric adds PacketContext/ConfigurationTask methods through Loom interface injection.
patched=next((WORKSPACE/'Minecraft/SSO-backpack-toolpouch-mapstitch-shim/.gradle/loom-cache').rglob('minecraft-merged-*-26.3.jar'))
paths.insert(0,patched)
subprocess.run(['/usr/lib/jvm/java-27-openjdk/bin/javac','--release','25','-proc:none','-implicit:none','-cp',os.pathsep.join(map(str,dict.fromkeys(paths))),'-d',str(classes),*map(str,(ROOT/'qa/src').glob('*.java'))],check=True)
fixtures={}
for side in ('server','client'):
    jar=fixture/(side+'.jar');fixtures[side]=jar
    with zipfile.ZipFile(jar,'w',zipfile.ZIP_DEFLATED) as z:
        z.writestr('fabric.mod.json',json.dumps({'schemaVersion':1,'id':'suite_light_qa_'+side,'version':'1','environment':side,'entrypoints':{'main' if side=='server' else 'client':['suite.qa.Suite'+side.title()+'Qa']},'depends':{'fabric-api':'*'}}))
        for p in classes.rglob('*.class'):
            if side.title() in p.name:z.write(p,p.relative_to(classes))
children=[]
result={'passed':False,'bundle_sha256':sha(bundle),'scope':'bounded connection/capability/resource-pack smoke only; no exhaustive gameplay or integrated-server test','defaulted_sha256':sha(patched_defaulted),'cases':[]}
def wait(predicate,process,reason,seconds=120):
    end=time.monotonic()+seconds
    while not predicate():
        if process.poll() is not None:raise RuntimeError(reason+': process exited '+str(process.returncode))
        if time.monotonic()>end:raise TimeoutError(reason)
        time.sleep(.25)
def start(directory,command,mods):
    directory.mkdir(parents=True,exist_ok=True)
    (directory/'audit.json').write_text(json.dumps({'command':command,'mods':[{'file':p.name,'sha256':sha(p)} for p in mods]},indent=2)+'\n')
    out=(directory/'console.log').open('w')
    env=os.environ.copy();env.update(DISPLAY=':1',SDL_VIDEODRIVER='x11',SDL_VIDEO_X11_XINPUT2='0',LP_NUM_THREADS='2')
    proc=subprocess.Popen(command,cwd=directory,env=env,stdin=subprocess.PIPE,stdout=out,stderr=subprocess.STDOUT,text=True)
    children.append((proc,out));return proc
def stop(proc,server=False):
    if proc.poll() is None:
        if server:proc.stdin.write('stop\n');proc.stdin.flush()
        else:proc.terminate()
    try:proc.wait(timeout=30)
    except subprocess.TimeoutExpired:proc.kill();proc.wait()
def server(label,with_polymer):
    directory=RUN/label;(directory/'mods').mkdir(parents=True)
    mods=[bundle,*external,fixtures['server']]+([polymer] if with_polymer else [])
    for mod in mods:shutil.copy2(mod,directory/'mods'/mod.name)
    (directory/'eula.txt').write_text('eula=true\n')
    with socket.socket() as s:s.bind(('127.0.0.1',0));port=s.getsockname()[1]
    (directory/'server.properties').write_text(f'server-ip=127.0.0.1\nserver-port={port}\nonline-mode=false\nenforce-secure-profile=false\nwhite-list=false\nspawn-protection=0\nview-distance=2\nsimulation-distance=2\npause-when-empty-seconds=0\nlevel-type=minecraft:flat\ngenerator-settings={{"layers":[{{"block":"minecraft:bedrock","height":1}}],"biome":"minecraft:plains"}}\ngenerate-structures=false\n')
    cmd=[JAVA,'-Xms256M','-Xmx2G','-XX:ActiveProcessorCount=2','-Dsuite.qa.control='+str(directory),'-cp',os.pathsep.join(map(str,servercp)),'net.fabricmc.loader.impl.launch.knot.KnotServer','nogui']
    proc=start(directory,cmd,mods)
    wait(lambda:'Done (' in (directory/'console.log').read_text(),proc,'server startup '+label,180)
    pack=None
    if with_polymer:
        proc.stdin.write('polymer generate-pack\n');proc.stdin.flush()
        pack=directory/'polymer/resource_pack.zip'
        wait(pack.exists,proc,'resource pack generation',90)
        time.sleep(2)
        with zipfile.ZipFile(pack) as z:assert z.testzip() is None
    return proc,directory,port,pack

def client(name,kind,sp,serverdir,port,pack):
    directory=RUN/name;(directory/'mods').mkdir(parents=True)
    mods=[] if kind=='vanilla' else [fixtures['client'],STAGE/'fabric-api-0.161.0+26.3.jar']
    if kind=='suite':mods=[bundle,*external,fixtures['client']]
    for mod in mods:shutil.copy2(mod,directory/'mods'/mod.name)
    packopt=''
    if pack and kind!='suite':
        (directory/'resourcepacks').mkdir();shutil.copy2(pack,directory/'resourcepacks/suite-qa.zip')
        packopt='resourcePacks:["vanilla","file/suite-qa.zip"]\n'
    (directory/'options.txt').write_text('graphicsMode:0\nrenderDistance:2\nsimulationDistance:2\nmaxFps:20\nmaxFpsInactive:20\nsoundCategory_master:0.0\njoinedFirstServer:true\npauseOnLostFocus:false\n'+packopt)
    cp=[p for p in clientcp if kind!='vanilla' or not any(t in str(p) for t in ['/net.fabricmc/','/org.ow2.asm/'])]
    cmd=[JAVA,'-Xmx2G','-XX:ActiveProcessorCount=2','--enable-native-access=ALL-UNNAMED','-XX:StackShadowPages=32','--add-exports','java.base/jdk.internal.misc=ALL-UNNAMED','-Dsuite.qa.control='+str(directory)]
    for prop,folder in [('java.library.path','java'),('jna.tmpdir','jna'),('org.lwjgl.system.SharedLibraryExtractPath','lwjgl'),('io.netty.native.workdir','netty')]:cmd+=['-D'+prop+'='+str(directory/'natives'/folder)]
    cmd+=['-cp',os.pathsep.join(map(str,cp)),'net.minecraft.client.main.Main' if kind=='vanilla' else 'net.fabricmc.loader.impl.launch.knot.KnotClient','--username',name,'--version','26.3','--gameDir',str(directory),'--assetsDir',str(Path.home()/'.local/share/ModrinthApp/meta/assets'),'--assetIndex',info['assetIndex']['id'],'--uuid',str(uuid.uuid3(uuid.NAMESPACE_DNS,name)),'--accessToken','0','--versionType','release','--width','900','--height','600','--quickPlayMultiplayer',f'127.0.0.1:{port}']
    proc=start(directory,cmd,mods)
    joined=serverdir/(name+'-join.json')
    wait(joined.exists,sp,'server observed '+name,160)
    if kind!='vanilla':wait(lambda:(directory/'client-joined.txt').exists(),proc,'in-world client '+name,45)
    else:time.sleep(5)
    observed=json.loads(joined.read_text());assert observed['passed'],observed
    assert proc.poll() is None,'client exited immediately'
    assert name+' lost connection' not in (serverdir/'console.log').read_text(),'client disconnected'
    if packopt:assert 'file/suite-qa.zip' in (directory/'console.log').read_text(),'fallback resource pack not loaded'
    result['cases'].append({'profile':name,'client_kind':kind,'client_polymer':False,'server_polymer':pack is not None,'passed':True,'observed':observed,'fallback_pack_loaded':bool(packopt)})
    (RUN/'result.json').write_text(json.dumps(result,indent=2)+'\n')
    print('PASS '+name,flush=True)
    stop(proc)
try:
    sp,directory,port,pack=server('server-polymer',True)
    client('SuiteNative','suite',sp,directory,port,pack)
    client('FabricOnly','fabric-api',sp,directory,port,pack)
    client('SuiteVanilla','vanilla',sp,directory,port,pack)
    stop(sp,server=True);assert sp.returncode==0
    sp,directory,port,pack=server('server-native',False)
    client('SuiteNativeNP','suite',sp,directory,port,pack)
    stop(sp,server=True);assert sp.returncode==0
    result['passed']=True
except Exception as error:
    result['failure']=str(error)
finally:
    for proc,out in reversed(children):
        if proc.poll() is None:stop(proc)
        out.close()
    (RUN/'result.json').write_text(json.dumps(result,indent=2)+'\n')
    print(json.dumps(result,indent=2))
raise SystemExit(0 if result['passed'] else 1)
