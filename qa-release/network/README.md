# Atlas maintenance network regression

This disposable fixture connects a Fabric API-only client and a suite-native client to the exact candidate suite on a local Polymer server. The small observation mod on the fallback client registers no suite channels.

The server gives each player a real atlas containing two copies of one saved map with explored pixels and missing item center metadata. It executes the registered `/repairmaps` and `/dedupemaps` commands, verifies the repaired center and one retained map entry, then waits for at least 100 more connected world/server ticks. An outgoing packet observer records vanilla map packets, custom channel names, and each channel's actual Fabric advertised state. Assertions require zero unadvertised mod payloads on either connection, zero refresh payloads on fallback, and exactly two refresh payloads on native.

Run from the repository root:

```sh
python3 qa-release/network/run.py \
  --jar build/libs/vanilla-plusplus-quality-of-life-suite-1.1.3+26.3.jar \
  --label atlas-maintenance-final113
```

The runner reuses retained local launcher/dependency caches, including Defaulted's dropfix release and unchanged Fzzy Config. Worlds, fixture JARs, clients and raw launch audits remain under ignored `runs/`; compact sanitized release evidence goes under `qa-release/evidence/1.1.3/network/`.

Scope is command network safety only. This does not repeat the broader suite gameplay, resource pack, pure vanilla or GUI matrix, which has separate evidence.
