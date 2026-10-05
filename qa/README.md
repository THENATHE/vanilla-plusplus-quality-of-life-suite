# Light suite QA

`light.py` checks four connection profiles using the final suite JAR and cached exact libraries. It preserves a hashed copy of the production bundle, runs disposable localhost worlds, checks per-module native decisions, confirms several in-world frames, verifies generated pack loading for fallback clients, and verifies the exact Defaulted dropfix artifact. No original JAR, launcher profile, or existing world is changed.

Profiles currently require explicit resolution of the SSO artifact provenance/port-pause condition; do not infer a runtime pass from the presence of this harness. See `../docs/VALIDATION.md` for current evidence and manual acceptance steps.

Tests are intentionally bounded: no gameplay matrix, reconnect stress, integrated-server test, or settings UI automation is included. `runs/` is development-only and must be excluded from source/release publication, except selected sanitized evidence files if desired. Logs can contain disposable offline profile IDs and machine-local paths.
