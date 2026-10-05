#!/usr/bin/env python3
"""Compare every root/nested Java class in tested and final bundle artifacts."""
import argparse, hashlib, io, json, zipfile
from pathlib import Path

def classes(data, prefix=""):
    result={}
    with zipfile.ZipFile(io.BytesIO(data)) as jar:
        for name in jar.namelist():
            if name.endswith(".class"):
                result[prefix+name]=hashlib.sha256(jar.read(name)).hexdigest()
            elif name.endswith(".jar"):
                result.update(classes(jar.read(name),prefix+name+"!/"))
    return result

parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument("tested",type=Path);parser.add_argument("final",type=Path)
args=parser.parse_args();old=classes(args.tested.read_bytes());new=classes(args.final.read_bytes())
changed=[key for key in sorted(old.keys() | new.keys()) if old.get(key)!=new.get(key)]
print(json.dumps({"tested_sha256":hashlib.sha256(args.tested.read_bytes()).hexdigest(),"final_sha256":hashlib.sha256(args.final.read_bytes()).hexdigest(),"tested_java_classes":len(old),"final_java_classes":len(new),"changed_java_classes":changed,"identical":not changed},indent=2))
raise SystemExit(bool(changed))
