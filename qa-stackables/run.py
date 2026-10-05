#!/usr/bin/env python3
"""Sensible Stackables menus, conservation, codec and restart regression in disposable dedicated worlds."""
from pathlib import Path
import argparse,hashlib,json,os,shutil,subprocess,sys,zipfile
ROOT=Path(__file__).resolve().parents[1];WORKSPACE=ROOT.parents[1]
sys.path.insert(0,str(ROOT/'tools'))
from workspace_paths import load_helper, project_path
parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--label',required=True);parser.add_argument('--jar',type=Path,default=ROOT/'build/libs/vanilla-plusplus-quality-of-life-suite-1.0.2-stackables.1+26.3.jar');parser.add_argument('--upstream-baseline',action='store_true');args=parser.parse_args()
RUN=ROOT/'qa-stackables/runs'/args.label;RUN.mkdir(parents=True,exist_ok=False)
helper=load_helper(project_path(WORKSPACE,'chalk-polymer-shim')/'qa/run.py','cached_cp')
target_mc='26.2' if args.upstream_baseline else '26.3'
servercp,clientcp,info=helper.prepare_classpaths(target_mc,RUN/'libraries')
if args.upstream_baseline:args.jar=ROOT/'components/sensible-stackables/upstream/artifacts/sensible_stackables-fabric-3.0.3+26.2.jar'
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
bundle=RUN/args.jar.name;shutil.copy2(args.jar,bundle)
with zipfile.ZipFile(bundle) as z:featurefiles={Path(x['file']).name for x in json.loads(z.read('fabric.mod.json')).get('jars',[])}
mods=[]
for p in (ROOT/'libs').glob('*.jar'):
 if p.name in featurefiles:continue
 with zipfile.ZipFile(p) as z:
  if 'fabric.mod.json' not in z.namelist():continue
 mods.append(p)
stage=project_path(WORKSPACE,'polymer-shim-test-bundle')/'staging-2026-10-01/mods';mods += [stage/'fabric-api-0.161.0+26.3.jar',stage/'cloth-config-fabric-26.3.159.jar'];polymer=stage/'polymer-bundled-0.18.2+26.3.jar'
if args.upstream_baseline:
 cache=Path.home()/'.gradle/caches/modules-2/files-2.1'
 patterns=['net.fabricmc.fabric-api/fabric-api/0.161.0+26.2/*/*.jar','me.fzzyhmstrs/fzzy_config/0.7.6+26.2-pre-3/*/*.jar','maven.modrinth/defaulted/1.3.8-26.1.2-Fabric/*/*.jar','maven.modrinth/mixson/2.2.0/*/*.jar']
 mods=[next(cache.glob(p)) for p in patterns]+list((ROOT/'libs').glob('fabric-language-kotlin*.jar'))
else:assert sha(next(p for p in mods if p.name.startswith('defaulted-')))=='e339d6f0eb471a4ac41185fb9dbe0cfaa78a290c6ceedf92a49ba9110f732c61'
classes=RUN/'classes';classes.mkdir();paths=[*servercp,*clientcp,bundle,*mods]+([] if args.upstream_baseline else [polymer])
def nested(p):
 with zipfile.ZipFile(p) as z:
  for n in z.namelist():
   if n.endswith('.jar'):
    data=z.read(n);dest=RUN/(hashlib.sha256(data).hexdigest()[:12]+'-'+Path(n).name)
    if not dest.exists():dest.write_bytes(data);paths.append(dest);nested(dest)
for p in [bundle,*mods]+([] if args.upstream_baseline else [polymer]):nested(p)
patched=(Path.home()/'.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-merged-deobf/26.2/minecraft-merged-deobf-26.2.jar') if args.upstream_baseline else next((project_path(WORKSPACE,'SSO-backpack-toolpouch-mapstitch-shim')/'.gradle/loom-cache').rglob('minecraft-merged-*-26.3.jar'));paths.insert(0,patched)
subprocess.run(['/usr/lib/jvm/java-27-openjdk/bin/javac','--release','25','-proc:none','-cp',os.pathsep.join(map(str,dict.fromkeys(paths))),'-d',str(classes),*map(str,(ROOT/'qa-stackables/src').rglob('*.java'))],check=True)
fixtures={}
for kind,entry in [('stackables','qa.StackablesQa')]:
 jar=RUN/(kind+'.jar');fixtures[kind]=jar
 with zipfile.ZipFile(jar,'w',zipfile.ZIP_DEFLATED) as z:
  z.writestr('fabric.mod.json',json.dumps({'schemaVersion':1,'id':'suite_'+kind+'_qa','version':'1','environment':'server','entrypoints':{'main':[entry]},'depends':{'sensible_stackables':'*','fabric-api':'*'}}))
  for p in classes.rglob('*.class'):z.write(p,p.relative_to(classes))
results={'bundle_sha256':sha(bundle),'minecraft':target_mc,'track':'developer-release' if args.upstream_baseline else 'ported-suite','passed':False,'cases':[]}
try:
 for with_polymer,uncap in ([(False,False),(False,True)] if args.upstream_baseline else [(True,False),(True,True),(False,True)]):
  kind='stackables'
  directory=RUN/(kind+('-polymer' if with_polymer else '-native')+('-uncapped' if uncap else '-defaults'));(directory/'mods').mkdir(parents=True)
  selected=[bundle,*mods,fixtures[kind]]+([polymer] if with_polymer else [])
  for p in selected:shutil.copy2(p,directory/'mods'/p.name)
  (directory/'eula.txt').write_text('eula=true\n')
  config=directory/'config/sensible_stackables';config.mkdir(parents=True)
  (config/'config.toml').write_text('uncapStackSize = '+str(uncap).lower()+'\ncommonStackSize = '+('2048' if uncap else '64')+'\n')
  assert 'eula=true' in (directory/'eula.txt').read_text()
  (directory/'server.properties').write_text('server-ip=127.0.0.1\nserver-port=0\nonline-mode=false\nwhite-list=false\nspawn-protection=0\nview-distance=2\nsimulation-distance=2\npause-when-empty-seconds=0\nlevel-type=minecraft:flat\ngenerator-settings={"layers":[{"block":"minecraft:bedrock","height":1}],"biome":"minecraft:plains"}\ngenerate-structures=false\n')
  command=['/usr/lib/jvm/java-25-openjdk/bin/java','-Xmx2G','-XX:ActiveProcessorCount=2','-Dstackables.qa.uncapped='+str(uncap).lower(),'-cp',os.pathsep.join(map(str,servercp)),'net.fabricmc.loader.impl.launch.knot.KnotServer','nogui']
  (directory/'audit.json').write_text(json.dumps({'command':command,'mods':[{'file':p.name,'sha256':sha(p)} for p in selected]},indent=2)+'\n')
  for phase in range(2):
   name='stackables-result.json';resultfile=directory/name
   resultfile.unlink(missing_ok=True)
   with (directory/('console-'+str(phase)+'.log')).open('w') as log:
    proc=subprocess.Popen(command,cwd=directory,stdout=log,stderr=subprocess.STDOUT)
    try:code=proc.wait(timeout=180)
    except subprocess.TimeoutExpired:proc.kill();proc.wait();raise
   result=resultfile.read_text() if resultfile.exists() else 'FAIL no result'
   record={'profile':directory.name,'phase':phase,'exit_code':code,'result':result.strip(),'passed':code==0 and json.loads(result).get('passed',False)};results['cases'].append(record)
   print(json.dumps(record),flush=True)
   if not record['passed']:raise AssertionError(record)
 results['passed']=True
finally:
 (RUN/'result.json').write_text(json.dumps(results,indent=2)+'\n');print(json.dumps(results,indent=2))
