#!/usr/bin/env python3
"""Actual nested inventory packet codec regression, recording all failures rather than treating reproduction as acceptance."""
from pathlib import Path
import argparse, hashlib, json, os, shutil, subprocess, sys, zipfile
ROOT=Path(__file__).resolve().parents[1]; WORKSPACE=ROOT.parents[1]
sys.path.insert(0,str(ROOT/'tools'))
from workspace_paths import load_helper, project_path
p=argparse.ArgumentParser(description=__doc__)
p.add_argument('--label',required=True)
p.add_argument('--jar',type=Path,default=ROOT/'build/libs/vanilla-plusplus-quality-of-life-suite-1.1.5+26.3.jar')
p.add_argument('--uncapped',action='store_true')
args=p.parse_args()
RUN=ROOT/'qa-packet-templates/runs'/args.label; RUN.mkdir(parents=True,exist_ok=False)
helper=load_helper(project_path(WORKSPACE,'chalk-polymer-shim')/'qa/run.py','cached_cp')
servercp,clientcp,info=helper.prepare_classpaths('26.3',RUN/'libraries')
sha=lambda x:hashlib.sha256(x.read_bytes()).hexdigest()
bundle=RUN/args.jar.name; shutil.copy2(args.jar,bundle)
with zipfile.ZipFile(bundle) as z: features={Path(x['file']).name for x in json.loads(z.read('fabric.mod.json'))['jars']}
mods=[]
for file in (ROOT/'libs').glob('*.jar'):
 if file.name in features:continue
 with zipfile.ZipFile(file) as z:
  if 'fabric.mod.json' in z.namelist():mods.append(file)
stage=project_path(WORKSPACE,'polymer-shim-test-bundle')/'staging-2026-10-01/mods'
mods += [stage/'fabric-api-0.161.0+26.3.jar',stage/'cloth-config-fabric-26.3.159.jar']
polymer=stage/'polymer-bundled-0.18.2+26.3.jar'
assert not any(x.name.startswith('defaulted-') for x in mods)
classes=RUN/'classes';classes.mkdir();paths=[*servercp,*clientcp,bundle,*mods,polymer]
sourcefiles=[]
for file in (ROOT/'qa-packet-templates/src').rglob('*.java'):
 dest=RUN/'source'/file.relative_to(ROOT/'qa-packet-templates/src');dest.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(file,dest);sourcefiles.append(dest)
shutil.copy2(Path(__file__),RUN/'runner-snapshot.py')
def nested(file):
 with zipfile.ZipFile(file) as z:
  for name in z.namelist():
   if name.endswith('.jar'):
    data=z.read(name);dest=RUN/(hashlib.sha256(data).hexdigest()[:12]+'-'+Path(name).name)
    if not dest.exists():dest.write_bytes(data);paths.append(dest);nested(dest)
for file in [bundle,*mods,polymer]:nested(file)
patched=next((project_path(WORKSPACE,'SSO-backpack-toolpouch-mapstitch-shim')/'.gradle/loom-cache').rglob('minecraft-merged-*-26.3.jar'));paths.insert(0,patched)
subprocess.run(['/usr/lib/jvm/java-27-openjdk/bin/javac','--release','25','-proc:none','-cp',os.pathsep.join(map(str,dict.fromkeys(paths))),'-d',str(classes),*map(str,sourcefiles)],check=True)
fixture=RUN/'packet-templates-qa.jar'
with zipfile.ZipFile(fixture,'w',zipfile.ZIP_DEFLATED) as z:
 z.writestr('fabric.mod.json',json.dumps({'schemaVersion':1,'id':'suite_packet_templates_qa','version':'1','environment':'server','entrypoints':{'main':['qa.PacketTemplatesQa']},'depends':{'thenathe_mod_suite':'*','fabric-api':'*'}}))
 for file in classes.rglob('*.class'):z.write(file,file.relative_to(classes))
results={'bundle_sha256':sha(bundle),'fixture_sha256':sha(fixture),'qa_sources':{str(x.relative_to(RUN)):sha(x) for x in sourcefiles},'runner_sha256':sha(RUN/'runner-snapshot.py'),'passed':False,'cases':[]}
try:
 for with_polymer in [True,False]:
  name='polymer' if with_polymer else 'no-polymer';directory=RUN/name;(directory/'mods').mkdir(parents=True)
  selected=[bundle,*mods,fixture]+([polymer] if with_polymer else [])
  for file in selected:shutil.copy2(file,directory/'mods'/file.name)
  shutil.copy2(WORKSPACE/'Minecraft/map-atlases-26.3/port26/build/qa-server/eula.txt',directory/'eula.txt')
  assert 'eula=true' in (directory/'eula.txt').read_text()
  (directory/'server.properties').write_text('server-ip=127.0.0.1\nserver-port=0\nonline-mode=false\nview-distance=2\nsimulation-distance=2\npause-when-empty-seconds=0\nlevel-type=minecraft:flat\ngenerator-settings={"layers":[{"block":"minecraft:bedrock","height":1}],"biome":"minecraft:plains"}\ngenerate-structures=false\n')
  if args.uncapped:
   config=directory/'config/sensible_stackables';config.mkdir(parents=True)
   (config/'config.toml').write_text('uncapStackSize = true\ncommonStackSize = 2048\n')
  command=['/usr/lib/jvm/java-25-openjdk/bin/java','-Xmx2G','-XX:ActiveProcessorCount=2','-Dtemplates.qa.uncapped='+str(args.uncapped).lower(),'-cp',os.pathsep.join(map(str,servercp)),'net.fabricmc.loader.impl.launch.knot.KnotServer','nogui']
  (directory/'audit.json').write_text(json.dumps({'command':command,'mods':[{'file':x.name,'sha256':sha(x)} for x in selected]},indent=2)+'\n')
  with (directory/'console.log').open('w') as log:
   proc=subprocess.Popen(command,cwd=directory,stdout=log,stderr=subprocess.STDOUT)
   try:code=proc.wait(timeout=180)
   except subprocess.TimeoutExpired:proc.kill();proc.wait();raise
  file=directory/'packet-templates-result.json'
  record={'profile':name,'exit_code':code,'result':json.loads(file.read_text()) if file.exists() else {'passed':False,'fatal':'no result'}}
  results['cases'].append(record);print(json.dumps({'profile':name,'exit_code':code,'passed':record['result']['passed'],'failures':record['result'].get('failures'),'assertions':record['result'].get('assertions')}),flush=True)
 results['passed']=all(x['exit_code']==0 and x['result']['passed'] for x in results['cases'])
finally:
 (RUN/'result.json').write_text(json.dumps(results,indent=2)+'\n');print('RESULT '+str(RUN/'result.json'),flush=True)
sys.exit(0 if results['passed'] else 1)
