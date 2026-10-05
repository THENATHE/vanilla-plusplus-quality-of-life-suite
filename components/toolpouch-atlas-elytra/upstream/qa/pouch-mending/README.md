# Stored Elytra XP Mending regression

Run from the addon source directory with the workspace's cached Minecraft 26.3/Fabric launch inputs and local graphics session available:

```sh
python3 qa/pouch-mending/run.py --track isolated --label baseline
python3 qa/pouch-mending/run.py --track isolated --label candidate --candidate --addon build/libs/toolpouch-atlas-elytra-compat-1.0.2+26.3.jar
python3 qa/pouch-mending/run.py --track developer --label developer-disabled --candidate --addon build/libs/toolpouch-atlas-elytra-compat-1.0.2+26.3.jar
python3 qa/pouch-mending/run.py --track developer --label developer-enabled --regular-mending --candidate --addon build/libs/toolpouch-atlas-elytra-compat-1.0.2+26.3.jar
```

Repeat the last two with `--track sso-port` and fresh labels to verify the existing SSO port and its isolated dependencies. `--resume` with the same label/options restarts the saved world and client without reseeding, verifying persisted durability, XP and client synchronization. `--port` avoids collisions for parallel disposable profiles.

The default addon is the preserved 1.0.1 baseline. `--candidate` changes expected results; it does not select a JAR. Isolated means original Tool Pouch and dependencies plus the addon, without SSO, MapStitch or Polymer. Developer and SSO-port profiles include Multi-Shim 1.0.3, original companion mods and the selected addon. Both client and server dependencies are recorded with SHA-256 hashes. The regular-Mending switch changes only the disposable server's in-memory SSO setting; no user configuration is edited.

A native client connects to a real dedicated server. The fixture places actual 7-XP ExperienceOrb entities at the player, waits for natural collision/pickup, and traces the original playerTouch path. It never calls the production repair helper directly. Each profile checks 12 cases: inventory pouch, leggings pouch, flight disabled, no Mending, full durability, loose non-equipped Elytra, odd damage/unused XP, broken wings, equipped-item priority, multiple stored wings, open pouch menu, and a final leggings state for restart verification. Twenty-two actual orb pickups plus 12 authoritative result and 12 settled-client checks produce 46 checks per full run.

The old-addon isolated control expects stored wings not to mend. Candidate isolated/regular-enabled profiles expect repair; both SSO default-disabled profiles require unchanged durability and ordinary XP credit. The open-menu case waits for a real native pouch screen and confirms its repaired live contents persist after closing. XP accounting includes 5 damage repaired from a 7-XP orb leaving 5 XP, and 80 total damage across two items consuming 40 of 42 XP.

Worlds, raw launch commands/logs, copied dependencies and compiled fixtures remain ignored under `runs/`. Sanitized evidence is published in `docs/1.0.2/`. Original input hashes are checked again after shutdown. This is a local regression harness, not a redistribution of game assets or a standalone downloadable test environment.
