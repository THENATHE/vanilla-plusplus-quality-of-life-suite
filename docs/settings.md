# Suite settings and original configuration ownership

Open **Vanilla++ Quality of Life Suite** from Mod Menu, or run the client command `/suite-settings` while connected. You can also bind **Open Suite Settings** under **Options → Controls → Vanilla++ Quality of Life Suite**; its default is **Not Bound** to avoid collisions. The binding opens settings from gameplay and leaves other open screens alone. The screen uses the existing Fzzy Config interface with one shared navigation page and sidebar. Each original gameplay/client configuration remains its own category, with the original fields, descriptions, validation, reset defaults, list/map editors, restart/resource-reload notices and server permission checks.

## Configuration identities

| Component | Original identifiers | Persistence and synchronization |
| --- | --- | --- |
| Simple Smithing Overhaul | `simple_smithing_overhaul:config-v2` and any other registered GUI configs in its namespace | Original Fzzy Config objects and files |
| Bannerpoint | `bannerpoint:config`, `bannerpoint:client_config` | Original Fzzy Config objects and files; server transmission options retain the relog notice |
| MapStitch | `mapstitch:config`, `mapstitch:client_config` | Original Fzzy Config objects and files |
| Tool Pouch | `toolpouch:config`, `toolpouch:client_config` | Original Fzzy Config objects and files |
| Sensible Stackables | `sensible_stackables:config`, `sensible_stackables:client_config` | Original Fzzy Config objects and files; uncapping requires restart |
| Tiered Backpacks | `tiered_backpacks:config` | Original Fzzy Config objects and files |
| MiscTweaks | `misctweaks:config`, `misctweaks:client_config` | Original Fzzy Config objects and files; Sodium/Raised compatibility retained |
| Simple Death Improvements | `simple_death_improvements:config` | Original Fzzy Config objects and files; optional accessory compatibility retained |
| Chalk | `ChalkConfig.EmitParticles` | The client bridge reads/writes the original Cloth AutoConfig holder and saves the original `Chalk` config file |
| Shared Region Maps | None in the local component | Vanilla/MapStitch sharing runs automatically |
| Amethyst Curse Removal | None in the local component | Amethyst shard + cursed item in grindstone |
| Colorful Chalk | None; marker enables Chalk colors | Dye/glow crafting provided by integrated Chalk compatibility |
| Elytra/MapStitch addon | Original Tool Pouch settings, key bindings and addon commands | Original implementations retained |

The visible categories are **Simple Smithing Overhaul Settings**, **MapStitch Gameplay Settings**, **MapStitch Client Settings**, **Tool Pouch Gameplay Settings**, **Tool Pouch Client Settings**, **Tiered Backpacks Settings**, **MiscTweaks Gameplay Settings**, **MiscTweaks Client Settings**, **Simple Death Improvements Settings**, **Sensible Stackables Gameplay Settings**, **Sensible Stackables Client Settings**, **Chalk Settings**, **Bannerpoint Gameplay Settings**, and **Bannerpoint Client Settings**. Each landing-page action button reads **Configure...**, while its row label, sidebar label, actual configuration title, and screen-reader category name remain fully named. The shorter button text avoids scrolling long titles in the fixed-width native action button. Original configuration IDs remain unchanged. These title overrides are resource translations; original mod JARs and configuration values are preserved. An always-enabled built-in settings language pack loads above the individual bundled mod packs, so original resource-pack ordering cannot overwrite these category titles. The Fabric resource-loader API registers it as required at the top by default, with no manual enable step. Each same-namespace English resource includes all entries from the exact bundled upstream mod, with only its category titles changed. Minecraft can select the suite resource as a whole instead of combining a partial same-path file, so preserving this complete language resource prevents original item, HUD and field labels from disappearing. Refresh the complete copies under `src/main/resources/resourcepacks/settings_titles/assets/<namespace>/lang/en_us.json` from the matching original artifacts when updating mods. `SuiteResources.java` registers the required pack using the existing Fabric API dependency; its metadata targets the exact Minecraft 26.3 resource format 97.1.

Key bindings remain in **Options → Controls**. They are not copied into a new storage schema. Server settings remain subject to the original operator/permission requirements. A vanilla client has no mod settings GUI; server configuration files and the original Fzzy server controls remain available to server administrators.

The native root page and sidebar contain only usable configuration categories. Automatic addons and shims have no placeholder buttons or fake settings entries. The former `thenathe_mod_suite.overview` configuration is no longer registered; an old saved overview file can remain harmlessly on disk. Multiscale atlas controls live in the atlas screen rather than a separate settings category. Client rendering features such as MiscTweaks' lowered shields, brightness and raised hotbar remain client features and require the suite client installation.

## How the combined Fzzy screen preserves behavior

`src/main/java/com/thenathe/suite/client/SuiteSettings.java` collects registered GUI configurations from the eight included Pajic namespaces in stable suite 1.1.2, including `bannerpoint`. It uses each existing active config and the original default/base object. It constructs a single Fzzy `ConfigScreenManager` containing the original keys and `ConfigSet`s, rather than serializing fields into another configuration type. Server/client classification uses the original `SyncedConfigRegistry.hasConfig` result, exactly as Fzzy's own screen factory does.

Fzzy's `ConfigSingleUpdateManager` derives save behavior and network update keys from `ConfigSet.active.getId().toLanguageKey()`. Consequently, grouping configurations under the suite sidebar changes their presentation while preserving their original synchronization destinations, file paths and permissions. Original namespace screen providers and manager cache entries route forwarded changes into this same manager, preserving the original review/approval workflow.

The combined manager is reused when the screen closes and reopens, so pending forwarded proposals remain available for review. When Fzzy removes a namespace cache after a server sync/update, the next suite or original-module opening rebuilds the combined manager, replays pending proposals into its fresh native caches, and restores every namespace route. Client play connection initialization and disconnection clear the manager and its aliases: proposals and cached permissions never carry from one server to another.

The adapter uses the pinned **Fzzy Config 0.7.7+fix2+26.3** runtime. `SettingsNavigationButtonMixin` changes only the config-entry button-builder label for namespaces collected by the suite hub. It leaves Fzzy’s category text, original narration supplier, click callback, permission state, sidebar and nested-section actions intact. This client mixin depends on Fzzy Config itself and works when Mod Menu is absent. Recheck `EntryCreators.createConfigEntry` button construction when updating Fzzy. Java-visible internal classes are used because the public API exposes per-namespace screens but no public cross-namespace grouping builder. The only reflective accesses are:

- `ClientConfigRegistry.clientConfigs`, to obtain registered entries.
- `ClientConfigEntry.getBase()` and `getNoGui()`, because the entry implementation is package-private.
- `ClientConfigRegistry.configScreenManagers`, to keep forwarded-update routing attached to the combined screen.
- `ConfigScreenManager.screenCaches` and each cache's `getForwardedUpdates()`, to retain pending proposals when native sync/update invalidation requires rebuilding widgets. Proposals are replayed through Fzzy's original receiver; entries are resolved against the current configuration objects.

No upstream config values, serialization methods or library binaries are replaced. When updating Fzzy, verify these APIs before changing its pinned version. A changed internal API stops opening the combined screen with an explicit error; it does not guess a field schema or discard saved settings.

## Chalk bridge

`ChalkSettings.java` presents the single Chalk option as a Fzzy client-only category. Before opening the suite screen it refreshes from `AutoConfig.getConfigHolder(ChalkConfig.class).getConfig().EmitParticles`. Applying a change updates that same holder and calls its native `save()`. Refreshing or applying also aligns the original `Chalk.CONFIG` runtime reference with the holder's canonical instance, so an AutoConfig reload cannot leave the particle runtime using a stale object. Cloth Config remains installed and owns Chalk's persistent configuration. A Fzzy bridge cache file may be created after saving; it is not the authoritative Chalk file and is refreshed from Chalk on every new screen opening.

Only the Chalk bridge is registered as a suite client configuration. The suite landing page is Fzzy's native navigation screen, with no fabricated root configuration. Selecting Chalk's configuration button in Mod Menu opens **Chalk Settings** in this same grouped interface through Mod Menu's provided config factory API. The original Cloth library and AutoConfig holder still own Chalk persistence.

`SuiteKeybindings.java` registers one standard Minecraft key mapping in the suite Controls category. Its unbound default and user-selected binding persist through Minecraft's existing `options.txt`; there is no duplicate configuration file. Client tick handling consumes key presses safely and requires a loaded player/world plus no existing screen before opening settings.

## Bannerpoint settings

Stable suite 1.1.2 includes fourteen functional categories: the previous twelve plus **Bannerpoint Gameplay Settings** and **Bannerpoint Client Settings**. Gameplay retains transmit-named-banners, transmit-map-linked-banners and maximum-range options. Client settings retain banner-name color, shadow, background and background-opacity options. Client-only name rendering requires Bannerpoint’s code; a server resource pack provides waypoint icons, not these text-rendering features. The original IDs, files, server permissions and relog notices remain authoritative. The compatibility component is automatic and has no placeholder settings page. Bannerpoint remains an individual feature entry in Mod Menu, while its compatibility module belongs under the suite.

## Stable 1.1 settings presentation

The focused fixture now requires exactly twelve functional config IDs in stable suite **1.1** on `main`, checks every effective native category title, rejects the removed overview ID, and requires original HUD/field translations from every overridden namespace. It verifies that the suite binding appears in Minecraft Controls, dispatches a temporarily bound key through Minecraft's actual key click path, and checks opening from gameplay or ignoring input outside a world. Existing Chalk/Tool Pouch persistence, server permissions, forwarded proposals and manager lifecycle checks are retained. Fresh execution results belong in the release validation report; historical records below describe earlier builds and their former overview/title presentation.

## Historical verification and remaining user checks

The native runtime fixture passed both an operator and non-operator dedicated-server session against the frozen settings candidate SHA-256 `3bc3f314b70d78010334d62501ac1e7dfdcb2507ba72e36a24e1f61e5bc7f621`. Each opened/rendered all eleven native configuration screens, verified original IDs and native row widgets, saved Chalk false/true through its original AutoConfig file, reloaded/reopened it with `Chalk.CONFIG` aligned to the canonical object, and saved/re-read Tool Pouch's original client file. The operator pressed an actual MiscTweaks checkbox and the original server config received/saved it. The non-operator saw the native locked `Can't Edit` button; pressing it did not change the local or server setting. Records and screenshots are under `qa-settings/runs/settings-final-admin/` and `qa-settings/runs/settings-final-guest-02/`.

The final branded bundle SHA-256 `0312a5d7ce8cf1e597e21a9d111f8efaa47fa7c01c6b18effb48977cd9bbc957` passed the graphical overview/sidebar check. All 657 root and nested Java classes are byte-identical to the operator/non-operator-tested candidate. The final screen retains Tiered Backpacks' native title with an explicit module description. Sanitized results are in [settings evidence](../qa-settings/evidence/results.json); actual rendered [overview](../qa-settings/evidence/overview.png) and [expanded native sidebar](../qa-settings/evidence/sidebar.png) are preserved alongside them.

These checks exercise representative native persistence and permissions, not every upstream setting combination, reset/restart path, forwarded-edit approval, or broadcast to a second connected client. Remaining acceptance steps:

1. Open the suite settings from Mod Menu and `/suite-settings`. Bind **Open Suite Settings** in Controls, return to gameplay and press the chosen key; confirm it opens the same screen.
2. Confirm original gameplay and client sections are present, including list/map editors.
3. Change one client-only option, apply, reconnect and check persistence.
4. As a server operator, change one server option and check another client receives it. As a non-operator, confirm protected fields are disabled or changes follow Fzzy's original approval flow.
5. Change Chalk particles, apply, inspect the original Chalk config and reopen its original screen; both should agree.
6. Restore a field to its original default, exercise a restart-required field and check the original notice.
7. Check Tool Pouch/MapStitch addon controls in Options → Controls and the original addon command help.

Record completed runtime checks separately; these steps are not a claim that the full matrix has been executed.

## Settings lifecycle regression — 1.0.1+26.3

Candidate SHA-256 `47e656b8856b9c36164fe47189b6e798f1139ce8af1e2fce7174d0ef5e58cd3d` passed real graphical client runs `lifecycle-admin-to-guest-1.0.1-02` and `lifecycle-guest-to-admin-1.0.1`. Both rendered all eleven config categories and repeated Chalk/Tool Pouch persistence plus the native operator/guest setting checks. They verified pending proposals survive closing/reopening, native `receiveUpdate` invalidation rebuilds widgets and restores namespace routing while preserving proposals, and actual reconnection to a different dedicated server clears proposals and uses that server's opposite permission level. Each second server's saved setting was checked too. Proposals were supplied to the original Fzzy client receiver; this does not claim two-player forwarding transport coverage.

Sanitized results are in [lifecycle evidence](../qa-settings/evidence/lifecycle-1.0.1.json). The first sandbox launch could not create its localhost socket and is excluded. Packaging review subsequently identified stale nested version metadata in the tested candidate; the corrected release hash, class equivalence and focused lifecycle smoke are recorded separately in this evidence rather than labeling the earlier candidate as the final artifact.

The corrected final release SHA-256 `8e4783b66633a7f6f5cfa38e285b530638d4d82326d5eca15376f7b698d0791a` passed `lifecycle-final-1.0.1`: the graphical client opened the combined screen, validated all eleven configuration identities, retained a proposal across close/reopen, and rebuilt native update-invalidated caches while retaining proposals and restoring routing. [Recursive class comparison](../qa-settings/evidence/lifecycle-1.0.1-class-equivalence.json) confirms all 658 root/nested Java classes match the full two-server matrix candidate exactly. Only packaging metadata changed between those candidates.
