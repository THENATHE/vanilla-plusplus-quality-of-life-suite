# Validation scope

This component is modified for the suite. Historical standalone shim tests do not verify this changed binary. Compilation and static validation are required for this release; use the root suite testing guide for connection, native gameplay, Polymer fallback, and integrated-server smoke tests.

The suite 1.0.1 final artifact passed the shared native broken-anvil/Chalk transport matrix, including both independent mismatch directions, reconfiguration, reconnect, client Polymer, and integrated singleplayer. See [current suite validation](../../docs/VALIDATION.md#current-101-network-evidence) and the exact-hash records in `qa/evidence/1.0.1/`. Historical standalone tests are unchanged.
