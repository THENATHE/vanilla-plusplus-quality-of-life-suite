#!/usr/bin/env python3
"""Package an already verified commit; exclude public libraries and QA mods."""
import hashlib,json,posixpath,re,shutil,subprocess,zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
WORKSPACE=ROOT.parents[1]
SLUG='vanilla-plusplus-quality-of-life-suite'
PRODUCT='Vanilla++ Quality of Life Suite'
REPO='https://github.com/THENATHE/'+SLUG
report=json.loads((ROOT/'docs/build-verification.json').read_text())
version=report['version'];tag='v'+version
artifact=ROOT/'build/libs'/f'{SLUG}-{version}.jar'
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
assert report['runtime_tested'], 'Release runtime verification must be recorded first'
assert sha(artifact)==report['sha256'], 'Artifact changed after verification'
revision=subprocess.check_output(['git','rev-parse','HEAD'],cwd=ROOT,text=True).strip()
assert not subprocess.check_output(['git','status','--porcelain'],cwd=ROOT,text=True).strip(),'Commit final source/evidence before packaging'
release=WORKSPACE/'Builds/Minecraft'/PRODUCT/'Main Plugin'/version
release.mkdir(parents=True,exist_ok=True)
shutil.copy2(artifact,release/artifact.name)
locks=json.loads((ROOT/'locks/artifacts.json').read_text())
libraries=[next(x for x in locks if x.get('id')==key) for key in ['defaulted','codecui']]
kit=release/f'vanilla-plusplus-local-libraries-{version}.zip'
with zipfile.ZipFile(kit,'w',zipfile.ZIP_DEFLATED) as archive:
 for entry in libraries:
  library=ROOT/entry['file'];assert sha(library)==entry['sha256'];archive.write(library,'mods/'+library.name)
 for name in ['defaulted-LICENSE.txt','codecui-LICENSE.txt']:
  archive.write(ROOT/'licenses'/name,'LICENSES/'+name)
 archive.writestr('SHA256SUMS.sha256',''.join(x['sha256']+'  mods/'+Path(x['file']).name+'\n' for x in libraries))
 archive.writestr('README.md',f'''# Optional local libraries for {PRODUCT} {version}

Minecraft 26.3 / Fabric. Copy the two JARs under mods/ into your instance's mods directory. Remove other external Defaulted/CodecUI copies first. Keep the other libraries listed in the suite README installed separately.

Defaulted 1.3.8+26.3.dropfix.1 is the exact requested fix, not the unpatched original. CodecUI 26.3-1.4.3 is the retained private fork-family build; its exact developer build commit is unrecorded. Both are MIT licensed; full terms and attribution are included under LICENSES/. Fzzy Config and all other public libraries are excluded from this archive.

Source/provenance: {REPO}/tree/{revision}/components/defaulted-dropfix and components/codecui-reference. Checksums: SHA256SUMS.sha256.
''')
source=release/f'{SLUG}-{version}-source.zip'
subprocess.run(['git','archive','--format=zip','--prefix='+SLUG+'/',str(revision),'-o',str(source)],cwd=ROOT,check=True)
for path in [release/artifact.name,kit,source]:
 with zipfile.ZipFile(path) as z:assert z.testzip() is None
with zipfile.ZipFile(source) as z:
 assert not any('/runs/' in n or '/.gradle/' in n or '/__pycache__/' in n for n in z.namelist())
 assert not any(n.endswith('.jar') and '/gradle/wrapper/' not in n for n in z.namelist())
readme=(ROOT/'README.md').read_text()
readme=re.sub(r'\]\((?!https?://|#)([^)]+)\)',lambda m:']('+REPO+'/blob/'+revision+'/'+m.group(1)+')',readme)
readme+=f'\n## Release record\n\nVersion: {version}. Release target: Minecraft 26.3 / Fabric, unofficial combined distribution using the documented developer/local-port inputs. Source revision: `{revision}`. Source checkout: `Minecraft/thenathe-mod-suite/`; public source: {REPO}/tree/{revision}. Historical developer/ported tracks remain separate, including the paused SSO port.\n\nInstallable feature JAR, optional local-library archive and complete tracked-source ZIP are beside this README with `SHA256SUMS.sha256`. QA fixtures are excluded from installable artifacts.\n\nValidation results for this release, historical checks of unchanged components, and remaining manual checks: [validation]('+REPO+'/blob/'+revision+'/docs/VALIDATION.md).\n'
readme+='\nLocal reference files: [dependency and compatible-mod downloads](DEPENDENCIES.md), [changes](CHANGELOG.md), and [validation](VALIDATION.md).\n'
(release/'README.md').write_text(readme)
shutil.copy2(ROOT/'docs/build-verification.json',release/'VERIFICATION.json')
def copy_document(relative):
 original=ROOT/relative
 content=original.read_text()
 content=re.sub(r'\]\((?!https?://|#)([^)]+)\)',lambda m:']('+REPO+'/blob/'+revision+'/'+posixpath.normpath(str(Path(relative).parent/m.group(1)))+')',content)
 (release/original.name).write_text(content)
for document in ['docs/VALIDATION.md','THIRD_PARTY_NOTICES.md','docs/DEPENDENCIES.md','CHANGELOG.md']:
 copy_document(document)
shutil.copy2(ROOT/'docs/modrinth-links.json',release/'modrinth-links.json')
checksums=''.join(sha(p)+'  '+p.name+'\n' for p in [release/artifact.name,kit,source])
(release/'SHA256SUMS.sha256').write_text(checksums)
print(json.dumps({'release':str(release),'revision':revision,'checksums':checksums},indent=2))
