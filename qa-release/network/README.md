# Atlas maintenance network regression

This disposable fixture connects a Fabric API-only client and a suite-native client to the exact candidate suite on a local Polymer server. The small observation mod on the fallback client registers no suite channels.

The server gives each player a real atlas containing two copies of one locked saved map with explored pixels and missing item center metadata. It executes the registered `/atlas fix`, `/atlas fix check`, a second repair through `/atlas repair` after all centers are correct, the alias read-only check, `/atlas dedupe`, and `/atlas makecopy`. It requires actual inventory full-content packets after both repairing and unchanged repairs, no full snapshot or atlas mutation from the read-only check, repaired center metadata and one retained map entry, then waits for at least 100 more connected world/server ticks. Command copying consumes exactly one ordinary book, retains the source and saved map IDs/pixels, and delivers one fresh atlas to inventory while excluding stored empty maps and paper. An outgoing packet observer records actual `ClientboundContainerSetContentPacket` container/state/slot counts and atlas presence, vanilla map packets, custom channel names, and each channel's actual Fabric advertised state. Assertions require zero unadvertised mod payloads on either connection, zero refresh payloads on fallback, and exactly three refresh payloads on native.

Each client also opens a real cartography menu and uses the mapped client click method (`MultiPlayerGameMode.handleContainerInput`) to move the atlas/book into input slots and shift-take the result. Every step waits for authoritative server and client state. The fallback observes its unchanged vanilla BOOK placement rule returning false; the actual server click accepts/corrects it. Assertions require one consumed book, the original atlas retained in the top slot and a complete fresh copy in inventory, followed by another 60 connected client ticks.

Run from the repository root:

```sh
python3 qa-release/network/run.py \
  --jar build/libs/vanilla-plusplus-quality-of-life-suite-1.1.4+26.3.jar \
  --label atlas-full-state-final114
```

The runner reuses retained local launcher/dependency caches, including Defaulted's dropfix release and unchanged Fzzy Config. Worlds, fixture JARs, clients and raw launch audits remain under ignored `runs/`; compact sanitized release evidence goes under `qa-release/evidence/1.1.4/network/`.

The runner reads the version from the candidate JAR metadata. `--compile-only` compiles observations without launching any runtime. Historical 1.1.3 results remain under `evidence/1.1.3/network/`; their fixture source is retained in Git commit `dc34ad2`.

Scope is command network safety and inventory menu resynchronization only. This does not repeat the broader suite gameplay, resource pack, pure vanilla or GUI matrix, which has separate evidence.
