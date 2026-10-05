"""Locate retained local QA inputs before or after suite consolidation."""
from pathlib import Path
from types import ModuleType
import os

ARCHIVE_ROOT = Path('Backups/General Utilities (Non-WP)/Minecraft')
SNAPSHOT = '2026-10-04-suite-consolidation'


def project_path(workspace, name):
    """Prefer an active checkout, otherwise use its preserved consolidation snapshot."""
    workspace = Path(workspace)
    active = workspace / 'Minecraft' / name
    archived = workspace / ARCHIVE_ROOT / name / 'Snapshots' / SNAPSHOT
    return active if active.is_dir() or not archived.is_dir() else archived


def load_helper(path, name):
    """Import retained helper code without writing bytecode into the snapshot."""
    path = Path(path)
    module = ModuleType(name)
    module.__file__ = str(path)
    exec(compile(path.read_bytes(), str(path), 'exec'), module.__dict__)
    return module


def retained_path(workspace, path):
    """Resolve a moved source/release path without changing historical audit files."""
    workspace, path = Path(workspace), Path(path)
    if path.exists():
        return path
    try:
        relative = path.relative_to(workspace / 'Minecraft')
        candidate = project_path(workspace, relative.parts[0]).joinpath(*relative.parts[1:])
    except (ValueError, IndexError):
        try:
            relative = path.relative_to(workspace / 'Builds/Minecraft')
        except ValueError:
            return path
        candidate = workspace / 'Backups/old builds/Minecraft' / relative
    return candidate if candidate.exists() else path


def mapstitch_launch(workspace):
    """Keep historical launch helpers anchored to the real workspace after moving."""
    launch = load_helper(project_path(workspace, 'mapstitch-polymer-compat-26.3') / 'qa/launch.py', 'launch')
    launch.ROOT = Path(workspace)
    launch.OLD = project_path(workspace, 'map-atlases-26.3')
    launch.SERVER_AUDIT = launch.OLD / 'build/port3-polymer-final/packaged-launch-audit.json'
    launch.VANILLA_AUDIT = launch.OLD / 'qa/registry-fix/vanilla/vanilla-launch-audit.json'
    original_audits = launch.audits

    def relocated_audits():
        audits = original_audits()
        for audit in audits:
            command = audit['command']
            index = command.index('-cp') + 1
            command[index] = os.pathsep.join(str(retained_path(workspace, p)) for p in command[index].split(os.pathsep))
            if '--assetsDir' in command:
                index = command.index('--assetsDir') + 1
                command[index] = str(retained_path(workspace, command[index]))
        return audits

    launch.audits = relocated_audits
    return launch
