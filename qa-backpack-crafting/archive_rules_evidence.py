#!/usr/bin/env python3
"""Archive selected successful QA runs without worlds or dependency binaries.

Example: archive_rules_evidence.py --candidate-sha256 HASH --destination evidence/1.4.3
    --run server=runs/1.4.2-server-final --run rules/native=runs/SUCCESSFUL_LABEL
    --attachment independent-build.json=/tmp/vanilla-plusplus-1.4.3-source-compare.json
    --attachment standard-build.txt=/tmp/EXPLICIT_BUILD_LOG
"""
import argparse
import hashlib
import json
import re
import shutil
import tempfile
import zipfile
from pathlib import Path, PurePosixPath


SUITE = Path(__file__).resolve().parents[1]
DEFAULT_VERSION = "1.4.3+26.3"


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def assignment(value):
    label, separator, location = value.partition("=")
    relative = PurePosixPath(label)
    if not separator or relative.is_absolute() or any(part in (".", "..") for part in label.split("/")):
        raise argparse.ArgumentTypeError("Use a portable relative label=existing-path")
    if not all(re.fullmatch(r"[A-Za-z0-9][A-Za-z0-9_.-]*", part) for part in label.split("/")):
        raise argparse.ArgumentTypeError("Labels may contain letters, numbers, periods, underscores, hyphens and slashes")
    return label, Path(location).resolve()


def sanitize(value):
    if isinstance(value, dict):
        return {sanitize(key): sanitize(item) for key, item in value.items()}
    if isinstance(value, list):
        return [sanitize(item) for item in value]
    if not isinstance(value, str):
        return value
    for path, token in ((SUITE, "<suite-workspace>"), (Path.home() / ".gradle", "<dependency-cache>"),
                        (Path.home(), "<home>")):
        value = value.replace(str(path), token)
    return re.sub(r"/home/[^/\s\"']+", "<home>", value)


def nodes(value):
    yield value
    if isinstance(value, dict):
        for item in value.values():
            yield from nodes(item)
    elif isinstance(value, list):
        for item in value:
            yield from nodes(item)


def read_result(run, candidate, expected_version):
    if (run / "result.json").is_file():
        return json.loads((run / "result.json").read_text()), [run / "result.json"]
    evidence_path = run / "evidence.json"
    evidence = json.loads(evidence_path.read_text())
    if evidence.get("suite_sha256") != candidate or evidence.get("failure"):
        raise ValueError(f"Wheel evidence failed or used a different candidate: {run.name}")
    result_path, observations_path = run / "control/result.txt", run / "control/observations.json"
    text = result_path.read_text()
    if not text.startswith("PASS ") or "FAIL" in text or evidence.get("result.txt") != text:
        raise ValueError(f"Wheel run has no matching success report: {run.name}")
    observations = json.loads(observations_path.read_text())
    if not isinstance(observations, list) or json.loads(evidence.get("observations.json", "null")) != observations:
        raise ValueError(f"Wheel run has no matching executed observations: {run.name}")
    if not assertion_count(observations):
        raise ValueError(f"Wheel run has no executed assertions: {run.name}")
    client = evidence.get("launches", {}).get("client", {})
    if "-Dsettings.qa.wheel=true" not in client.get("command", []) or not any(
            mod["file"].startswith("hotkey-wheel-") for mod in client.get("mods", [])):
        raise ValueError(f"Settings run did not enable the actual Hotkey Wheel integration: {run.name}")
    frozen = Path(evidence["suite"]).resolve()
    if not frozen.is_relative_to(run) or digest(frozen) != candidate:
        raise ValueError(f"Wheel frozen candidate no longer matches: {run.name}")
    with zipfile.ZipFile(frozen) as archive:
        version = json.loads(archive.read("fabric.mod.json"))["version"]
    if version != expected_version:
        raise ValueError(f"Wheel run does not target {expected_version}: {run.name}")
    for side, audit in evidence.get("launches", {}).items():
        if json.loads((run / side / "launch-audit.json").read_text()) != audit:
            raise ValueError(f"Wheel launch audit changed: {run.name}/{side}")
    result = {"passed": True, "version": version, "bundle_sha256": candidate,
              "scope": "Actual Hotkey Wheel activation and native suite settings callbacks.",
              "observations": observations}
    return result, [evidence_path, result_path, observations_path]


def assertion_count(value):
    if isinstance(value, dict):
        if isinstance(value.get("assertion_count"), int):
            return value["assertion_count"]
        if isinstance(value.get("checks"), list):
            return len(value["checks"])
        if value.get("passed") is True and ("case" in value or "config_id" in value):
            return 1
        return sum(assertion_count(item) for item in value.values())
    if isinstance(value, list):
        return sum(assertion_count(item) for item in value)
    return 0


def verify_run(run, candidate, version=DEFAULT_VERSION):
    result, reports = read_result(run, candidate, version)
    dictionaries = [item for item in nodes(result) if isinstance(item, dict)]
    passed = [item["passed"] for item in dictionaries if "passed" in item]
    if not passed or any(value is not True for value in passed):
        raise ValueError(f"Run is incomplete or failed: {run.name}")
    if any(item.get("exit_code", 0) != 0 for item in dictionaries):
        raise ValueError(f"Run has an unsuccessful process exit: {run.name}")
    if result.get("version", version) != version:
        raise ValueError(f"Run does not target {version}: {run.name}")
    hashes = [item["bundle_sha256"] for item in dictionaries if "bundle_sha256" in item]
    if not hashes or any(value != candidate for value in hashes):
        raise ValueError(f"Run used a different suite candidate: {run.name}")
    if any((run / "control").rglob("failure")):
        raise ValueError(f"Run retains a fixture failure marker: {run.name}")
    sources = list((run / "executed-source").glob("*.java")) + list((run / "executed-source").glob("*.py"))
    if not sources:
        sources = list(run.glob("*.java")) + list(run.glob("executed-*.py"))
    if not any(source.suffix == ".java" for source in sources) or not any(source.suffix == ".py" for source in sources):
        raise ValueError(f"Run has no executed fixture snapshot: {run.name}")
    audits = list(run.glob("*/audit.json")) + list(run.glob("*/launch-audit.json"))
    if not audits:
        raise ValueError(f"Run has no launch audit: {run.name}")
    for audit in audits:
        data = json.loads(audit.read_text())
        suite_mods = [mod for mod in data.get("mods", [])
                      if mod["file"].startswith("vanilla-plusplus-quality-of-life-suite-")]
        if not suite_mods or any(mod["sha256"] != candidate for mod in suite_mods):
            raise ValueError(f"Launch audit does not match candidate: {run.name}/{audit.parent.name}")
        for mod in suite_mods:
            frozen = audit.parent / "mods" / mod["file"]
            if digest(frozen) != candidate:
                raise ValueError(f"Launched suite changed after the run: {run.name}/{audit.parent.name}")
            with zipfile.ZipFile(frozen) as archive:
                actual_version = json.loads(archive.read("fabric.mod.json"))["version"]
            if actual_version != version:
                raise ValueError(f"Launched suite does not declare {version}: {run.name}/{audit.parent.name}")
    fixtures = list((run / "fixtures").glob("client.jar")) + list((run / "fixtures").glob("server.jar"))
    fixtures += list(run.glob("backpack-crafting-probe.jar"))
    if not fixtures:
        raise ValueError(f"Run has no executed fixture JAR: {run.name}")
    mods = [mod for audit in audits for mod in json.loads(audit.read_text())["mods"]]
    for fixture in fixtures:
        recorded = [mod["sha256"] for mod in mods if mod["file"] == fixture.name]
        if not recorded or any(value != digest(fixture) for value in recorded):
            raise ValueError(f"Fixture changed since launch audit: {run.name}/{fixture.name}")
    return result, sorted(sources), sorted(audits), sorted(fixtures), reports


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(sanitize(value), indent=2) + "\n")


def copy_text(source, target, raw_hashes):
    if source.is_symlink():
        raise ValueError(f"Refusing a symbolic link: {source.name}")
    target.parent.mkdir(parents=True, exist_ok=True)
    raw_hashes[str(target)] = digest(source)
    if source.suffix == ".json":
        write_json(target, json.loads(source.read_text()))
    else:
        target.write_text(sanitize(source.read_text()))


def fixture_manifest(jar):
    with zipfile.ZipFile(jar) as archive:
        classes = {entry: hashlib.sha256(archive.read(entry)).hexdigest()
                   for entry in sorted(archive.namelist()) if entry.endswith(".class")}
    return {"file": jar.name, "sha256": digest(jar), "classes": classes}


def checksums(directory):
    files = sorted(path for path in directory.rglob("*") if path.is_file()
                   and path.name != "SHA256SUMS.sha256")
    (directory / "SHA256SUMS.sha256").write_text("".join(
        f"{digest(path)}  {path.relative_to(directory).as_posix()}\n" for path in files))


def archive_run(label, run, verified, target, candidate, version=DEFAULT_VERSION):
    result, sources, audits, fixtures, reports = verified
    target.mkdir(parents=True)
    raw_hashes = {}
    write_json(target / "result.json", result)
    for report in reports:
        copy_text(report, target / report.name, raw_hashes)
    for source in sources:
        copy_text(source, target / "executed-source" / source.name, raw_hashes)
    inputs = {}
    for audit in audits:
        side = audit.parent.name
        data = json.loads(audit.read_text())
        inputs[side] = data["mods"]
        copy_text(audit, target / f"{side}-launch-audit.json", raw_hashes)
        config = audit.parent / "config/sorted.json"
        if config.is_file():
            copy_text(config, target / "config-proof" / side / "sorted.json", raw_hashes)
    write_json(target / "inputs.json", inputs)
    for control in sorted((run / "control").glob("*.json")):
        copy_text(control, target / "diagnostics" / control.name, raw_hashes)
    for fixture in fixtures:
        write_json(target / f"{fixture.stem}-fixture.classes.json", fixture_manifest(fixture))
    screenshots = []
    for audit in audits:
        for screenshot in sorted((audit.parent / "screenshots").glob("*.png")):
            destination = target / "screenshots" / audit.parent.name / screenshot.name
            destination.parent.mkdir(parents=True, exist_ok=True)
            if screenshot.is_symlink():
                raise ValueError(f"Refusing a symbolic link: {screenshot.name}")
            shutil.copyfile(screenshot, destination)
            screenshots.append(destination.relative_to(target).as_posix())
    assertions = assertion_count(result)
    manifest = {"profile": label, "passed": True, "suite_version": version, "suite_sha256": candidate,
                "assertions": assertions, "screenshots": screenshots,
                "raw_input_sha256": {Path(name).relative_to(target).as_posix(): value for name, value in raw_hashes.items()},
                "published_source_sha256": {path.name: digest(path) for path in (target / "executed-source").iterdir()},
                "audit_note": "Absolute personal paths are replaced with portable tokens. Raw hashes identify the retained local inputs; published hashes identify sanitized copies. Worlds, original JARs, raw game logs and native libraries are excluded."}
    write_json(target / "evidence-manifest.json", manifest)
    checksums(target)
    return {"profile": label, "passed": True, "assertions": assertions, "screenshots": len(screenshots)}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--run", type=assignment, action="append", required=True)
    parser.add_argument("--destination", type=Path, required=True)
    parser.add_argument("--candidate-sha256", required=True)
    parser.add_argument("--version", default=DEFAULT_VERSION, help="Exact declared suite version, including the Minecraft suffix")
    parser.add_argument("--attachment", type=assignment, action="append", default=[])
    parser.add_argument("--check-only", action="store_true", help="Validate inputs without creating an archive")
    args = parser.parse_args()
    if not re.fullmatch(r"[0-9a-f]{64}", args.candidate_sha256):
        parser.error("Candidate SHA-256 must be 64 lowercase hexadecimal characters")
    destination = args.destination.resolve()
    labels = [label for label, _ in args.run]
    if len(set(labels)) != len(labels) or any(a != b and b.startswith(a + "/") for a in labels for b in labels):
        parser.error("Run labels must be unique and must not contain another run label")
    if any(label.split("/")[0] in ("reports", "verification.json", "SHA256SUMS.sha256") for label in labels):
        parser.error("Run labels must not use reserved archive filenames or the reports directory")
    attachment_labels = [label for label, _ in args.attachment]
    if len(set(attachment_labels)) != len(attachment_labels) or any(
            a != b and b.startswith(a + "/") for a in attachment_labels for b in attachment_labels):
        parser.error("Attachment labels must be unique and must not contain another attachment label")
    verified = [(label, run, verify_run(run, args.candidate_sha256, args.version)) for label, run in args.run]
    for label, source in args.attachment:
        if source.suffix not in (".json", ".txt", ".log", ".sha256", ".md") or not source.is_file():
            parser.error("Attachments must be explicit existing text reports, not binary artifacts")
    if args.check_only:
        print(f"PASS validated {len(verified)} successful runs; nothing archived")
        return
    if destination.exists():
        parser.error("Destination already exists; choose a new evidence directory")
    destination.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(prefix=".rules-evidence-", dir=destination.parent) as temporary:
        staging = Path(temporary) / "archive"
        staging.mkdir()
        summaries = [archive_run(label, run, data, staging / label, args.candidate_sha256, args.version)
                     for label, run, data in verified]
        attachment_hashes = {}
        for label, source in args.attachment:
            copy_text(source, staging / "reports" / label, attachment_hashes)
        write_json(staging / "verification.json", {
            "suite_version": args.version, "suite_sha256": args.candidate_sha256, "passed": True,
            "runs": summaries, "collector_sha256": digest(Path(__file__)),
            "raw_report_sha256": {Path(name).relative_to(staging).as_posix(): value for name, value in attachment_hashes.items()}})
        checksums(staging)
        staging.rename(destination)
    print(f"PASS archived {len(verified)} successful runs to {destination.name}")


if __name__ == "__main__":
    main()
