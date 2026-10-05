#!/usr/bin/env python3
"""Verify nested metadata, original input bytes, licenses and Defaulted build pin."""
import hashlib,io,json,sys,zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
path=Path(sys.argv[1]) if len(sys.argv)>1 else ROOT/'build/libs/vanilla-plusplus-quality-of-life-suite-1.0.0+26.3.jar'
locks=json.loads((ROOT/'locks/artifacts.json').read_text())
with zipfile.ZipFile(path) as archive:
    assert archive.testzip() is None
    meta=json.loads(archive.read('fabric.mod.json'))
    assert meta['id']=='thenathe_mod_suite' and meta['version']=='1.0.0+26.3'
    assert meta['depends']['defaulted']=='=1.3.8+26.3.dropfix.1'
    assert not any(name.endswith('.class') and '/suite/network/' in name for name in archive.namelist()),'Duplicated coordinator in root'
    modules=[]
    for nested in meta['jars']:
        data=archive.read(nested['file'])
        with zipfile.ZipFile(io.BytesIO(data)) as jar:
            assert jar.testzip() is None
            info=json.loads(jar.read('fabric.mod.json'))
            assert '${' not in info['version']
            modules.append({'id':info['id'],'version':info['version'],'sha256':hashlib.sha256(data).hexdigest(),'file':nested['file']})
        pinned=next((x for x in locks if Path(x['file']).name==Path(nested['file']).name),None)
        if pinned:assert hashlib.sha256(data).hexdigest()==pinned['sha256'],'Changed original input '+pinned['id']
    assert len({x['id'] for x in modules})==len(modules)==13
    expected={'simple_smithing_overhaul','mapstitch','toolpouch','tiered_backpacks','misctweaks','simple_death_improvements','shared_region_maps','chalk','chalk-colorful-addon','toolpouch_atlas_elytra_compat','amethyst_curse_cleanser','chalk_polymer_compat','sso_backpack_toolpouch_mapstitch_shim'}
    assert {x['id'] for x in modules}==expected
    for licensefile in (ROOT/'licenses').glob('*.txt'):
        assert archive.read('META-INF/licenses/'+licensefile.name)==licensefile.read_bytes()
    report={'archive_integrity':True,'root_id':meta['id'],'version':meta['version'],'sha256':hashlib.sha256(path.read_bytes()).hexdigest(),'modules':modules,'original_inputs_unchanged':True,'defaulted_exact_dropfix_requirement':True,'licenses_retained':True,'runtime_tested':False}
(ROOT/'build/bundle-verification.json').write_text(json.dumps(report,indent=2)+'\n')
print(json.dumps(report,indent=2))
