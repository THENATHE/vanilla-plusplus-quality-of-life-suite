# Suite settings and original configuration ownership

Open **Thenathe Mod Suite** from Mod Menu, or run the client command `/suite-settings` while connected. The screen uses the existing Fzzy Config interface with one shared overview and sidebar. Each original gameplay/client configuration remains its own category, with the original fields, descriptions, validation, reset defaults, list/map editors, restart/resource-reload notices and server permission checks.

## Configuration identities

| Component | Original identifiers | Persistence and synchronization |
| --- | --- | --- |
| Simple Smithing Overhaul | `simple_smithing_overhaul:config-v2` and any other registered GUI configs in its namespace | Original Fzzy Config objects and files |
| MapStitch | `mapstitch:config`, `mapstitch:client_config` | Original Fzzy Config objects and files |
| Tool Pouch | `toolpouch:config`, `toolpouch:client_config` | Original Fzzy Config objects and files |
| Tiered Backpacks | `tiered_backpacks:config` | Original Fzzy Config objects and files |
| MiscTweaks | `misctweaks:config`, `misctweaks:client_config` | Original Fzzy Config objects and files; Sodium/Raised compatibility retained |
| Simple Death Improvements | `simple_death_improvements:config` | Original Fzzy Config objects and files; optional accessory compatibility retained |
| Chalk | `ChalkConfig.EmitParticles` | The client bridge reads/writes the original Cloth AutoConfig holder and saves the original `Chalk` config file |
| Shared Region Maps | None in the local component | Vanilla/MapStitch sharing runs automatically |
| Amethyst Curse Removal | None in the local component | Amethyst shard + cursed item in grindstone |
| Colorful Chalk | None; marker enables Chalk colors | Dye/glow crafting provided by integrated Chalk compatibility |
| Elytra/MapStitch addon | Original Tool Pouch settings, key bindings and addon commands | Original implementations retained |

Key bindings remain in **Options → Controls**. They are not copied into a new storage schema. Server settings remain subject to the original operator/permission requirements. A vanilla client has no mod settings GUI; server configuration files and the original Fzzy server controls remain available to server administrators.

The root overview contains explanatory entries for automatic modules. It does not introduce replacement enable switches for upstream features. Client rendering features such as MiscTweaks' lowered shields, brightness and raised hotbar remain client features and require the suite client installation.

## How the combined Fzzy screen preserves behavior

`src/main/java/com/thenathe/suite/client/SuiteSettings.java` collects registered GUI configurations from the six included Pajic namespaces. It uses each existing active config and the original default/base object. It constructs a single Fzzy `ConfigScreenManager` containing the original keys and `ConfigSet`s, rather than serializing fields into another configuration type. Server/client classification uses the original `SyncedConfigRegistry.hasConfig` result, exactly as Fzzy's own screen factory does.

Fzzy's `ConfigSingleUpdateManager` derives save behavior and network update keys from `ConfigSet.active.getId().toLanguageKey()`. Consequently, grouping configurations under the suite sidebar changes their presentation while preserving their original synchronization destinations, file paths and permissions. Original namespace screen providers and manager cache entries route forwarded changes into this same manager, preserving the original review/approval workflow.

The adapter uses the pinned **Fzzy Config 0.7.7+fix2+26.3** runtime. Java-visible internal classes are used because the public API exposes per-namespace screens but no public cross-namespace grouping builder. The only reflective accesses are:

- `ClientConfigRegistry.clientConfigs`, to obtain registered entries.
- `ClientConfigEntry.getBase()` and `getNoGui()`, because the entry implementation is package-private.
- `ClientConfigRegistry.configScreenManagers`, to keep forwarded-update routing attached to the combined screen.

No upstream config values, serialization methods or library binaries are replaced. When updating Fzzy, verify these APIs before changing its pinned version. A changed internal API stops opening the combined screen with an explicit error; it does not guess a field schema or discard saved settings.

## Chalk bridge

`ChalkSettings.java` presents the single Chalk option as a Fzzy client-only category. Before opening the suite screen it refreshes from `AutoConfig.getConfigHolder(ChalkConfig.class).getConfig().EmitParticles`. Applying a change updates that same holder and calls its native `save()`. Cloth Config remains installed and owns Chalk's persistent configuration. A Fzzy bridge cache file may be created after saving; it is not the authoritative Chalk file and is refreshed from Chalk on every new screen opening.

The suite overview and Chalk bridge are registered as client-only configuration. They do not create replacement server settings for any original module.

## Light checks and user testing

Compilation/API checks validate the pinned adapter and included entrypoints. A graphical smoke test should open the suite screen and verify every component appears in the sidebar. For acceptance:

1. Open the suite settings from Mod Menu and `/suite-settings`.
2. Confirm original gameplay and client sections are present, including list/map editors.
3. Change one client-only option, apply, reconnect and check persistence.
4. As a server operator, change one server option and check another client receives it. As a non-operator, confirm protected fields are disabled or changes follow Fzzy's original approval flow.
5. Change Chalk particles, apply, inspect the original Chalk config and reopen its original screen; both should agree.
6. Restore a field to its original default, exercise a restart-required field and check the original notice.
7. Check Tool Pouch/MapStitch addon controls in Options → Controls and the original addon command help.

Record completed runtime checks separately; these steps are not a claim that the full matrix has been executed.
