# Validation — 1.0.4+26.3

Recorded 2026-10-03. Release SHA-256:

`48c218887c60fe3628822d79b608aeb25fff1a12590de736e799ce4133f4566b`

Target: Fabric Minecraft 26.3, original Tool Pouch 1.1.10, Tiered Backpacks 1.0.20 and optional ClientSort 3.104.1. Multi-Shim remains 1.0.3. This release uses the developer-release integration stack. **The SSO-port track is paused at the user's request and was not built or tested for this update.** Original mod JARs and dependencies remain unchanged. [Compile inputs](1.0.4/compile-inputs.json), [official ClientSort release metadata](1.0.4/clientsort-upstream-version.json).

## ClientSort container support

Tool Pouch and Tiered Backpacks menus use ClientSort's current chest policy when no explicit/inherited policy exists for the menu. This is an in-memory fallback; saved configuration is not rewritten. ClientSort continues to supply buttons, shortcuts, sort orders, operation policies, ignored slots, item-lock integration and client/server operation selection. Its default player-inventory refill policy hides directional refill buttons; enabling refill buttons on both sides displays them normally.

ClientSort's generic LIGHT-item accessibility probe rejects restricted Tool Pouch slots. The integration recognizes those slots only at that probe call, retaining real-item validation. Client-side transfer/refill evaluates the live cursor and destination before each ordinary click, respecting per-slot capacity and allowlists. Inventory access stays on Minecraft's client thread while ClientSort's existing queue/rate control remains responsible for sequencing. Sort order and matching-transfer selection remain supplied by ClientSort.

Tool Pouch's stack-count check now excludes the destination slot itself: refilling or replacing that slot does not consume an additional occupied-slot quota. New stacks remain limited. For accelerated pouch sorting, the original ClientSort policy/schema checks still run; a bounded server-thread permutation validates real destination acceptance and capacities, commits only a valid result, and restores exact original contents on rejection. Accelerated transfers repeat ClientSort’s original checked insertion while the source count decreases, so per-slot caps do not stop the transfer after a single empty slot. Client-side sort waits for an empty cursor; accelerated sorting leaves the cursor unchanged. Backpack restrictions and owning-container guards remain intact.

| Profile | Result |
| --- | --- |
| Released addon 1.0.3 baseline | Missing bag-side buttons reproduced on all ten menu variants. [Evidence](1.0.4/clientsort-baseline.json). |
| Final addon, ClientSort on client only | **90 operations and 7,864 authoritative server assertions** passed, plus six client policy checks. [Evidence](1.0.4/clientsort-native.json). |
| Final addon, ClientSort on client and server | **90 operations and 7,864 authoritative server assertions** passed, plus six policy checks and seven server validation/rollback groups. The fixture observed 120 real ServerOperator constructions across those operations. [Evidence](1.0.4/clientsort-accelerated.json). |

The ten variants are ordinary/netherite pouches in inventory and attached to leggings, plus all six backpack tiers. Each runs container sort, player-inventory sort, refill/matching-transfer/bulk-transfer in both directions, and occupied-cursor sorting. Tests verify exact item conservation, slot caps, stack-count quotas, prohibited items, owning-container protection, unchanged carried items and sorted physical-slot persistence after closing/reopening. Normal ClientSort buttons are clicked; refill uses its operation entry point under default keybind-only preferences and real refill widgets in the enabled-button profile. Native operations use real networking; the seven malformed-request/rollback groups exercise the helper directly.

Representative pouch and backpack screens were rendered and inspected in both final profiles. Default policy shows three controls per group; enabling the player-inventory refill-button preference shows four, including matching transfer. [Native pouch](1.0.4/screenshots/clientsort-native-attached-netherite-pouch.png), [native backpack](1.0.4/screenshots/clientsort-native-netherite-backpack.png), [accelerated pouch with refill](1.0.4/screenshots/clientsort-accelerated-attached-netherite-pouch.png), [accelerated backpack with refill](1.0.4/screenshots/clientsort-accelerated-netherite-backpack.png).

## Regression without ClientSort

The final `48c218…` binary passed **178 native capacity/conservation checks** with ClientSort absent on both client and server, including default/custom pouch dimensions and attachment lifecycle. [Final evidence](1.0.4/no-clientsort-final-capacity.json).

Before the final two ClientSort-only corrections, candidate `b3454d135071306e795e73f961a3b777b6056be8a4721ccb2b5013807302992a` passed controls/flight **101 checks with MapStitch** and **101 without**, and developer-SSO Mending **46 checks enabled** and **46 disabled**, all without ClientSort installed. Those records retain that exact candidate hash: [controls with MapStitch](1.0.4/no-clientsort-elytra-with-mapstitch.json), [controls without MapStitch](1.0.4/no-clientsort-elytra-without-mapstitch.json), [Mending enabled](1.0.4/no-clientsort-xp-enabled.json), [Mending disabled](1.0.4/no-clientsort-xp-disabled.json).

All **26 non-ClientSort classes are byte-for-byte identical** between that candidate and the final release, including existing gameplay features, shared pouch quota correction and mixin gating. The final changes are the guarded ClientSort server-transfer mixin and client sorting cursor guard; final absence testing above confirms optional loading. [Bytecode comparison](1.0.4/original-feature-bytecode.json). These broader results are not represented as fresh runs of the final binary.

## Reproduction and limits

The [native ClientSort fixture](../qa/clientsort/README.md) records the actual client/server artifacts, controls, operations and conservation checks. Tests use disposable local worlds and configurations. Original JARs, raw launch commands and test worlds remain excluded from published source.

Coverage is specific to ClientSort 3.104.1 and the original mod versions above. Arbitrary third-party GUI replacements, accessory mods and item-lock mod runtimes are not certified. Their existing ClientSort selection hooks remain in place. The integration does not give vanilla clients native pouch/backpack gameplay. The addon still installs on both server and participating native clients; ClientSort is optional, and its own server installation enables acceleration.

Build uses Java 25 Gradle with a JDK 27 compiler targeting Java 25 bytecode. Prior release artifacts and historical validation remain preserved below; old results are not relabeled as new tests.

---

# Validation — 1.0.3+26.3

Recorded 2026-10-03. Exact addon SHA-256:

`17b19dbaaaa5a82c659600fa8dbe3fef1e4bcd878df90c81d291e6f0b19b8513`

Target: original developer Tool Pouch 1.1.10+26.3, Minecraft 26.3, Java 25, Fabric Loader 0.19.5 and Fabric API 0.161.0+26.3. The separate Multi-Shim stays at 1.0.3. Original JARs and dependencies remain unchanged. [Compile input inventory](1.0.3/compile-inputs.json).

## Attached netherite capacity and tier lifecycle

The original Tool Pouch dimension helpers check whether the holder itself is the netherite pouch item. Attached pouches are represented by leggings with container and tier components, so they incorrectly use ordinary pouch dimensions (4×4 instead of 5×5 by default). Both menu construction and internal inventory helpers allocate that smaller container, which can truncate items in the upper slots when contents are saved.

The addon now recognizes attached leggings carrying the existing netherite tier marker and returns the configured netherite rows and columns on both client and server. Standalone pouches, ordinary attachments, item identity and configuration values remain unchanged. Menus, previews, quick-move and internal storage helpers share those corrected dimensions. These fixes apply independently of MapStitch and the optional integrated-toggle detector.

A second upstream lifecycle fault leaves the tier marker on leggings after detachment. Reattaching an ordinary pouch can then incorrectly produce a netherite pouch on the next detachment. The addon removes the detached marker and normalizes each attachment from its actual crafting ingredient; it does not rely on the recipe singleton's cached ingredient. Contents, dye and other armor components remain owned by the original recipes.

| Profile | Result |
| --- | --- |
| Old addon 1.0.2, original Tool Pouch without Polymer | 146 baseline checks reproduced both faults. Attached netherite opened 16 instead of 25 slots and lost nine stored items on close; custom settings opened six instead of 35 and lost 29. Standalone and ordinary attached controls retained their contents. [Evidence](1.0.3/capacity-baseline.json). |
| Fixed addon, developer SSO / Multi-Shim stack | All 178 checks passed across eight native scenarios. [Evidence](1.0.3/capacity-developer.json). |
| Fixed addon, existing SSO-port / Multi-Shim stack | All 178 checks independently passed with the port's dependency set. [Evidence](1.0.3/capacity-sso-port.json). |

The eight cases cover standalone and leggings-attached ordinary/netherite pouches, each with default dimensions (16/25 slots) and custom dimensions (6/35). Actual client screen packets and inventory clicks withdraw and redeposit the last slot, then close/reopen; server and client agree on capacity. Internal last-slot replacement preserves sibling items. Serialization, full stored-item counts, empty cursors, detachment, dye preservation and stale-marker reattachment are checked. Negative controls reject netherite sizing for a false marker, bare leggings and non-armor items. Original recipe APIs construct/detach holders; these checks do not claim a full crafting-screen interaction test. See the [fixture and retained screenshots](../qa/attached-capacity/README.md).

## Existing feature regressions

| Check | Result |
| --- | --- |
| Controls/flight with MapStitch | 101 checks passed, including restart and dimension synchronization. [Evidence](1.0.3/elytra-with-mapstitch.json). |
| Controls/flight without MapStitch | 101 checks passed independently. [Evidence](1.0.3/elytra-without-mapstitch.json). |
| Atlas/minimap integration | 39 client and 38 server checks passed. [Evidence](1.0.3/atlas.json). |
| XP Mending: developer SSO | 46 checks passed with regular Mending [disabled](1.0.3/xp-developer-disabled.json), and 46 with it [enabled](1.0.3/xp-developer-enabled.json). |
| XP Mending: existing SSO port | 46 checks passed with regular Mending [disabled](1.0.3/xp-sso-port-disabled.json), and 46 with it [enabled](1.0.3/xp-sso-port-enabled.json). |
| Enabled-Mending process restarts | Saved durability/XP and client agreement passed on [developer](1.0.3/xp-developer-enabled-reconnect.json) and [SSO-port](1.0.3/xp-sso-port-enabled-reconnect.json) profiles. |
| Mixed-server vanilla guard | 1,767 server storage/serialization assertions plus an actual vanilla client connection/guard check passed. [Evidence](1.0.3/vanilla-guard.json). |

These regression profiles retain Multi-Shim 1.0.3 and use official Tiered Backpacks 1.0.20. SSO's regular-Mending setting is still respected; no configuration or dependency is bypassed. The vanilla client retains Type A display/guards and is not given a native pouch GUI.

## Build, review and limitations

The release built with Java 25 Gradle and `-PcompilerVersion=27`, targeting Java 25 bytecode. Independent review checked injection descriptors against the original developer JAR, both-side menu sizing, configurable dimensions, item/component preservation and common mixin gating. The addon and Multi-Shim have no duplicate class paths.

Install the updated addon on both server and native clients so menu geometry agrees. Existing attached netherite pouches work without detachment. The patch cannot recover items already discarded by an earlier undersized-container save. Existing attached items with a true tier marker are treated as netherite; an already-corrupted attachment has no reliable record of which historical ingredient was used. Shrinking configured dimensions below previously stored contents and arbitrary third-party accessory integrations are outside this fix.

Tool Pouch has no separate Minecraft-version port in this release inventory. Optional developer SSO and the existing SSO port retain their independent inputs and verification. Earlier release artifacts and historical validation below are preserved; earlier results are not relabeled as new runs.

---

# Validation — 1.0.2+26.3

Recorded 2026-10-02. Exact addon SHA-256:

`e08a8a45c2a89b4a7d64fadf27dd20e6a2bf1b1635da50c64333b8bd42de3748`

Minecraft 26.3, Java 25, Fabric Loader 0.19.5, Fabric API 0.161.0+26.3, original Tool Pouch 1.1.10+26.3. Mixed profiles use unchanged Multi-Shim 1.0.3 (`d5d08c6a5164206c9c2d47a58a89fec5f56ae03c7fcb12cbe04c98ed7d259722`). Original mod/dependency JARs remain unchanged; exact client/server hashes are in each evidence record. [Compile input inventory](1.0.2/compile-inputs.json).

## XP Mending behavior and tests

Vanilla's equipment scan does not include Elytra stored in a pouch. The original addon 1.0.1 control confirmed the gap: real XP orbs credited the player while stored Mending Elytra remained damaged. Version 1.0.2 repairs directly stored Elytra using XP left after the regular equipment pass, with vanilla repair-effect evaluation and integer XP accounting.

**The user's selected policy is to follow SSO's regular-Mending setting.** With SSO's Mending rework enabled and `enableRegularMendingBehavior=false` (the tested default), neither equipped nor pouch items mend from XP; the player receives it. With regular Mending enabled, pouch repair works. The addon does not edit that setting or require SSO. Its hook is inside the vanilla method body, which SSO's disabled-Mending wrapper skips. Both exact SSO targets verified this interaction at runtime.

Each full profile performed 22 actual ExperienceOrb pickups, 12 authoritative durability/XP checks and 12 settled-client comparisons: **46 checks**. Orbs were spawned at the connected native player and collected by normal collision, with actual `playerTouch` tracing; the production repair helper was not invoked directly.

| Profile | Result and evidence |
| --- | --- |
| Prior addon 1.0.1, isolated Tool Pouch | 46 expected-control checks passed, documenting absent pouch repair while ordinary worn-item Mending worked. [Baseline](1.0.2/xp-baseline.json). |
| New addon, isolated Tool Pouch | 46 passed without SSO, MapStitch or Polymer. [Evidence](1.0.2/xp-isolated.json). |
| Developer SSO 2.9.14, regular Mending disabled | 46 passed: no XP repair, ordinary XP credit retained. [Evidence](1.0.2/xp-developer-disabled.json). |
| Developer SSO 2.9.14, regular Mending enabled | 46 passed: pouch repairs and XP accounting correct. [Evidence](1.0.2/xp-developer-enabled.json). |
| Existing SSO 2.9.14-port.1, regular Mending disabled | 46 independently passed with its separate dependencies. [Evidence](1.0.2/xp-port-disabled.json). |
| Existing SSO port, regular Mending enabled | 46 independently passed: same requested behavior. [Evidence](1.0.2/xp-port-enabled.json). |
| Saved-world process restarts | All five candidate profiles passed a full server/client restart without reseeding, preserving durability, XP and client agreement. [Isolated](1.0.2/xp-isolated-reconnect.json), [developer disabled](1.0.2/xp-developer-disabled-reconnect.json), [developer enabled](1.0.2/xp-developer-enabled-reconnect.json), [port disabled](1.0.2/xp-port-disabled-reconnect.json), [port enabled](1.0.2/xp-port-enabled-reconnect.json). |

The 12 scenarios cover inventory/leggings pouches; flight toggle off; unenchanted/full/loose-inventory negative controls; 5-damage repair with XP remainder; broken Elytra; equipped-item priority; multiple stored wings; an open parent pouch menu; and a final leggings state for restart. A 7-XP orb repairs 14 damage normally. Repairing 5 damage consumes 2 XP and awards 5; two items with 80 total damage consume 40 out of 42 collected XP. Open-menu repair changes the live container and survives its close/save, preserving the unrelated clock. Settings changes belong only to the disposable test server's memory.

## Existing feature regressions

| Check | Result |
| --- | --- |
| Controls/flight with MapStitch | 101 assertions passed: original category identity, key/payload, inventory/leggings flight, chest fallback, commands, respawn, process restart/rejoin and dimension change. [Evidence](1.0.2/elytra-with-mapstitch.json). |
| Controls/flight without MapStitch | The same 101 assertions independently passed. [Evidence](1.0.2/elytra-without-mapstitch.json). |
| Atlas, minimap and crafting | 39 client plus 38 server assertions passed with the exact addon and current combined shim. [Evidence](1.0.2/atlas-with-mapstitch.json). |

Build passed using Java 25 Gradle with `-PcompilerVersion=27` and Java 25 bytecode output. Independent source review covered active-pouch selection, live-menu persistence, unchanged unrelated parent slots, bounded zero-effect handling, XP accounting, SSO wrapper interaction and optional mixin gating. No class-name collisions exist between the addon and current combined shim. New Mending hooks are independent of the native-toggle feature detector.

## Reproduction and scope

The [XP fixture](../qa/pouch-mending/README.md) records exact inputs and expected behavior. The existing [controls/atlas fixture](https://github.com/THENATHE/SSO-backpack-toolpouch-mapstitch-shim/tree/v1.0.3%2B26.3/qa/addon) supplies broader regressions. Original libraries, credentials, raw launch commands and disposable worlds remain excluded from published source; no user world/configuration was changed.

This covers normal Elytra directly in the active pouch. Elytra nested inside another container, custom replacement glider items, third-party accessory API runtime behavior and arbitrary datapack enchantment conditions were not tested. Eligibility uses the normal chest-slot repair-with-XP enchantment effect; zero/nonpositive effects are skipped without an unbounded loop. Equipment retains priority, and the active-pouch lookup retains Tool Pouch's inventory setting and companion shim's native-client guard. The latter was source-reviewed here; true-vanilla XP pickups were not part of this native feature suite.

Tool Pouch has no separate Minecraft version port in this release inventory. Developer SSO and the existing SSO port are independently tested optional integrations; SSO is not a new required dependency. Previous addon versions and their original source/release records remain preserved. Historical validation below refers to the older artifact hashes, not new 1.0.2 runs.

---

# Validation — 1.0.1+26.3

Recorded 2026-10-02 for Fabric Minecraft 26.3. Release SHA-256:

`77dc668b164bca182eec1e558b2948bd98ee40545ef30881bcdd471f43af1b0a`

| Scenario | Result |
| --- | --- |
| Original addon controls baseline | Actual vanilla KeyBindsList contained two Tool Pouch headings. [Evidence](keybind-heading-baseline.json). |
| Final addon with original Tool Pouch and MapStitch | 101 assertions passed: exactly one heading, one registered toggle sharing the original category; normal key/payload state, inventory/leggings flight, cosmetic wings, chest-slot fallback, respawn, full restart/rejoin, nonoperator on/off/toggle commands, and real Nether transition resynchronization. [Evidence](keybind-elytra-with-mapstitch.json). |
| Final addon without optional MapStitch | The same 101 assertions passed, independently exercising optional mixin gating. [Evidence](keybind-elytra-without-mapstitch.json). |

These tests used the exact addon above and combined-shim candidate `fc756498299259767766054a9c817a4d183c7467c5b62c342eadde92b8554926`. The server's later capacity-preserving atlas repair change does not change addon bytecode or its elytra paths. Exact server/client dependency hashes are retained in each result. The final atlas integration used this addon with release server SHA `d71a659db9c2025542953839facc5263cf2f80085644608a753913d50786e7d4` and passed 39 client plus 38 server assertions: actual minimap/world-map rendering and terrain updates in inventory/leggings pouches, authoritative native crafting, metadata/large-content preservation, and world-transition cache invalidation. [Exact atlas evidence](atlas.json), [baseline reproductions](atlas-baseline.json). The cache test uses a stale non-atlas sentinel and a deliberately wrong center during real Nether travel, then verifies reconstruction on return; a legitimately repopulated cache is allowed.

The client cache fix invalidates minimap centers when the client world or atlas metadata changes. It is independently gated by MapStitch's presence, including installations with an already-integrated pouch bridge. Original Tool Pouch and MapStitch JARs are unchanged. The original dependencies, saved preferences and key identifier are preserved.

Reproducible current fixtures: [Multi-Shim addon QA](https://github.com/THENATHE/SSO-backpack-toolpouch-mapstitch-shim/tree/main/qa/addon). They use disposable localhost profiles and the workspace's existing game libraries; original game/mod JARs, worlds and raw launch audits are not republished.

Not tested: physical keyboard hardware, sustained rocket flight, third-party accessory/Aileron integrations, arbitrary client mods, or all server configurations. The standalone original-addon checks below are historical 1.0.0 results, not claimed as fresh 1.0.1 runs.

---

# Validation — 1.0.0+26.3

Recorded 2026-09-30 for Fabric Minecraft 26.3. Release SHA-256:

`9969d1e8e4debb7e85832137add1fc5f5ae49c2f235469a9f95dacc9e3bc7be6`

| Scenario | Assertions | Result |
| --- | ---: | --- |
| Original Tool Pouch + modification; no MapStitch or Polymer | 41 | Passed dedicated Elytra checks |
| Original Tool Pouch + original MapStitch + modification; no Polymer | 75 | Passed dedicated atlas checks |
| Native client/server with original Tool Pouch + modification; no Polymer | 68 | Passed keybind, custom payload, respawn, and restart/reconnect checks |
| Native atlas client/server with original mods + modification and both Polymer shims on the server | 24 | Passed minimap/world-map rendering and item-discovery checks |

**208 assertions passed.** The dedicated suites were rerun against the final release hash. The original Tool Pouch and MapStitch files were not changed.

Coverage includes flight eligibility, midflight disable logic, chest Elytra durability, retained cosmetic wings, inventory/leggings pouch selection, broken wings, player isolation, saved preferences, allowance migration, opt-outs, map allocation/ticking/ejection, multiple atlases, open-menu snapshot protection, and compass/clock lookup. Registered key presses were dispatched programmatically through the normal network path.

The companion Polymer shim additionally passed native menu extraction/reinsertion, mixed Tool Pouch/MapStitch client capability tests, unmodified vanilla connections, and equipped-armor notices. See [its validation record](https://github.com/THENATHE/toolpouch-polymer-shim/blob/main/docs/VALIDATION.md).

Not runtime-tested: physical keyboard hardware, sustained real-world flying/rocket boosting, third-party accessory/Aileron integrations, or explicit Nether/End travel. Level-change synchronization invalidation is implemented and compiled; respawn and reconnect were exercised. These results do not establish compatibility with other Minecraft/Fabric/Polymer versions.

The runtime harness used local dedicated-server/client installations and is not a portable part of this repository. `./gradlew build` checks compilation and packaging; it does not rerun those gameplay tests. Test worlds, dependency JARs, account/environment paths, and raw runtime logs are intentionally not published.
