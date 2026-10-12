from pathlib import Path
import hashlib
import json
import os
import shutil
import subprocess
import zipfile

root = Path('/home/thenathe/SSDC/Plugins/WP Plugin Dev/Minecraft/thenathe-mod-suite')
work = Path('/tmp/vpp-swift-anvil-probe')
previous = root / 'qa-features/runs/native-features-final4-20261012'
native_audit = json.loads((previous / 'native/audit.json').read_text())
classpath = native_audit['command'][4].split(os.pathsep)
patched = Path('/home/thenathe/SSDC/Plugins/WP Plugin Dev/Backups/General Utilities (Non-WP)/Minecraft/SSO-backpack-toolpouch-mapstitch-shim/Snapshots/2026-10-04-suite-consolidation/.gradle/loom-cache/minecraftMaven/net/minecraft/minecraft-merged-d53d6e7c3c/26.3/minecraft-merged-d53d6e7c3c-26.3.jar')
compile_paths = [str(patched), *classpath, *map(str, (previous / 'fixture').glob('*.jar')), *map(str, (previous / 'native/mods').glob('*.jar'))]
classes = work / 'classes'
classes.mkdir(exist_ok=True)
subprocess.run(['/usr/lib/jvm/java-27-openjdk/bin/javac', '--release', '25', '-proc:none', '-implicit:none', '-cp', os.pathsep.join(compile_paths), '-d', str(classes), str(work / 'src/SwiftAnvilProbe.java')], check=True)
probe = work / 'swift-anvil-probe.jar'
with zipfile.ZipFile(probe, 'w', zipfile.ZIP_DEFLATED) as archive:
    archive.writestr('fabric.mod.json', json.dumps({'schemaVersion': 1, 'id': 'swift_anvil_probe', 'version': '1', 'environment': 'server', 'entrypoints': {'main': ['SwiftAnvilProbe']}, 'depends': {'fabric-api': '*', 'vpp_mod_suite': '*'}}))
    for source in classes.rglob('*.class'):
        archive.write(source, source.relative_to(classes))
results = {}
for profile in ('native', 'polymer'):
    target = work / profile
    (target / 'mods').mkdir(parents=True, exist_ok=True)
    for source in (previous / profile / 'mods').glob('*.jar'):
        if source.name != 'native-features-qa.jar':
            shutil.copy2(source, target / 'mods' / source.name)
    shutil.copy2(probe, target / 'mods' / probe.name)
    shutil.copy2(previous / profile / 'eula.txt', target / 'eula.txt')
    shutil.copy2(previous / profile / 'server.properties', target / 'server.properties')
    suite = next((target / 'mods').glob('vanilla-plusplus*.jar'))
    digest = hashlib.sha256(suite.read_bytes()).hexdigest()
    assert digest == 'e57862d94d4b4331ddd5245e916793a16fa9258d3cceca0d2b3ac891b77aea72', digest
    audit = json.loads((previous / profile / 'audit.json').read_text())
    with (target / 'console.log').open('w') as log:
        process = subprocess.run(audit['command'], cwd=target, stdout=log, stderr=subprocess.STDOUT, timeout=150)
    result = json.loads((target / 'swift-anvil-result.json').read_text())
    result['bundle_sha256'] = digest
    result['exit_code'] = process.returncode
    results[profile] = result
    print(profile, json.dumps(result), flush=True)
    assert result['passed'] and process.returncode == 0
(work / 'result.json').write_text(json.dumps(results, indent=2) + '\n')
