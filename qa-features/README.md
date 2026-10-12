# Native additions QA

`python3 qa-features/run.py --jar /path/to/frozen-suite.jar --label unique-candidate-label`

Compiles development-only tests against the exact suite/dependency files, records hashes, then runs disposable dedicated servers with and without Polymer. Each profile executes initial and restart phases. `--compile-only` builds the fixture without launching Minecraft.

The core and pet fixtures exercise actual native world/entity/inventory/menu mechanics. Fixture players use packet-suppressing connections; those results do not establish physical client clicks, resource-pack acceptance or connectivity. Connected native/Fabric/vanilla clients, settings/Mod Menu and Hotkey Wheel checks have separate evidence.

Runs are ignored scratch data. Copy accepted final reports, console logs, launch/input hashes and fixture sources into `evidence/<release-version>/` before publication; preserve candidate/rejected runs under separate labels.

The 1.3.1 regression cases use normal cursor clicks for the SSO scrap recipe and verify soul fire, occupied-mount headroom, leash companion clearance and adjacent lava. [Anvil XP checks](anvil/README.md) run separately; connected grindstone checks accept `--recipe scrap`.
