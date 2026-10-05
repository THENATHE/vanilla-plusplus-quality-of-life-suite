#!/usr/bin/env python3
"""Compile the published Fabric 26.2 Java baseline independently, without altering its artifact."""
from pathlib import Path
import hashlib,json,os,shutil,subprocess,sys,zipfile
BASE=Path(__file__).resolve().parent;ROOT=BASE.parents[1];WORKSPACE=ROOT.parents[1]
sys.path.insert(0,str(ROOT/'tools'))
from workspace_paths import load_helper,project_path
run=BASE/'upstream/build/source-verification';run.mkdir(parents=True,exist_ok=True)
helper=load_helper(project_path(WORKSPACE,'chalk-polymer-shim')/'qa/run.py','baseline_cp')
server,client,info=helper.prepare_classpaths('26.2',run/'libraries')
cache=Path.home()/'.gradle/caches/modules-2/files-2.1'
patterns=['net.fabricmc.fabric-api/fabric-api/0.161.0+26.2/*/*.jar','me.fzzyhmstrs/fzzy_config/0.7.6+26.2-pre-3/*/*.jar','maven.modrinth/defaulted/1.3.8-26.1.2-Fabric/*/*.jar','maven.modrinth/mixson/2.2.0/*/*.jar']
mods=[next(cache.glob(p)) for p in patterns]
mods+=list((ROOT/'libs').glob('fabric-language-kotlin*.jar'))
paths=[Path.home()/'.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-merged-deobf/26.2/minecraft-merged-deobf-26.2.jar',*server,*client,*mods]
def nested(path):
 with zipfile.ZipFile(path) as z:
  for n in z.namelist():
   if n.endswith('.jar'):
    data=z.read(n);dest=run/(hashlib.sha256(data).hexdigest()[:12]+'-'+Path(n).name)
    if not dest.exists():dest.write_bytes(data)
    paths.append(dest);nested(dest)
for p in [*mods,*[p for p in server if 'fabric-loader' in p.name]]:nested(p)
paths += list(cache.glob('org.jetbrains/annotations/*/*/*.jar'))
paths += list(cache.glob('io.github.llamalad7/mixinextras-fabric/0.5.3/*/*.jar'))
sources=run/'src';classes=run/'classes';classes.mkdir(exist_ok=True)
for p in (BASE/'upstream/source/me').rglob('*.java'):
 if '/neoforge/' in str(p):continue
 target=sources/p.relative_to(BASE/'upstream/source');target.parent.mkdir(parents=True,exist_ok=True)
 text=p.read_text().replace('import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;\n','').replace('@Entrypoint("main")\n','').replace('@Entrypoint("client")\n','')
 target.write_text(text)
command=['/usr/lib/jvm/java-27-openjdk/bin/javac','--release','25','-proc:none','-cp',os.pathsep.join(map(str,dict.fromkeys(paths))),'-d',str(classes),*map(str,sources.rglob('*.java'))]
subprocess.run(command,check=True)
report={'passed':True,'scope':'Published Fabric26.2 source Java compilation; build-only Entrypoint annotations replaced by original binary metadata, NeoForge files excluded. This is not runtime acceptance.','minecraft':'26.2','sources':len(list(sources.rglob('*.java'))),'upstream_artifact_sha256':hashlib.sha256((BASE/'upstream/artifacts/sensible_stackables-fabric-3.0.3+26.2.jar').read_bytes()).hexdigest(),'dependencies':[{'file':p.name,'sha256':hashlib.sha256(p.read_bytes()).hexdigest()} for p in mods]}
(BASE/'upstream/metadata/source-compilation.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report,indent=2))
