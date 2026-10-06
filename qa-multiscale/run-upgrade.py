#!/usr/bin/env python3
from pathlib import Path
import argparse,hashlib,json,shutil,subprocess
ROOT=Path(__file__).resolve().parents[1]
parser=argparse.ArgumentParser(description='Boot a copied preserved 1.1.4 atlas QA world against a completed 1.1.5 fixture installation, without booting the old JAR.');parser.add_argument('--label',required=True);parser.add_argument('--origin',type=Path,default=ROOT/'qa-multiscale/runs/atlas-copy-release-final3/maps-polymer');parser.add_argument('--current',type=Path,default=ROOT/'qa-multiscale/runs/1.1.5-sanity-server/maps-polymer');args=parser.parse_args();old=args.origin;current=args.current;run=ROOT/'qa-multiscale/runs'/args.label/'maps-polymer'
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
assert json.loads((old.parent/'result.json').read_text())['passed']
assert json.loads((old/'audit.json').read_text())['mods'][0]['sha256']=='2c1273947279693e5ec3c78b2c39793c3b5fdbc320216476e04d16f79b2a1754'
run.mkdir(parents=True,exist_ok=False)
origin=[{'file':str(p.relative_to(old)),'sha256':sha(p),'size':p.stat().st_size} for directory in ['world','config'] for p in sorted((old/directory).rglob('*')) if p.is_file()]
origin.append({'file':'mixedscale-atlas.dat','sha256':sha(old/'mixedscale-atlas.dat'),'size':(old/'mixedscale-atlas.dat').stat().st_size})
for directory in ['world','config']:shutil.copytree(old/directory,run/directory)
for name in ['mixedscale-atlas.dat','eula.txt','server.properties']:shutil.copy2(old/name,run/name)
shutil.copytree(current/'mods',run/'mods')
audit=json.loads((current/'audit.json').read_text());audit['origin']={'suite_version':'1.1.4+26.3','suite_sha256':'2c1273947279693e5ec3c78b2c39793c3b5fdbc320216476e04d16f79b2a1754','profile':str(old.relative_to(ROOT)),'files':origin}
assert not any(p['file'].startswith('defaulted-') for p in audit['mods'])
(run/'audit.json').write_text(json.dumps(audit,indent=2)+'\n')
with (run/'console-0.log').open('w') as log:
 proc=subprocess.Popen(audit['command'],cwd=run,stdout=log,stderr=subprocess.STDOUT)
 try:code=proc.wait(timeout=240)
 except subprocess.TimeoutExpired:proc.kill();proc.wait();raise
result=(run/'mixedscale-qa-result.txt').read_text();record={'bundle_sha256':audit['mods'][0]['sha256'],'passed':code==0 and result.startswith('PASS'),'origin_suite_version':'1.1.4+26.3','origin_suite_sha256':audit['origin']['suite_sha256'],'cases':[{'profile':'maps-polymer-upgrade-from-1.1.4','phase':0,'exit_code':code,'result':result.strip(),'passed':code==0 and result.startswith('PASS')}]}
(run.parent/'result.json').write_text(json.dumps(record,indent=2)+'\n');print(json.dumps(record,indent=2));assert record['passed']
