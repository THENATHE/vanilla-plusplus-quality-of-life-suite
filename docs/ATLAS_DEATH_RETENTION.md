# Keeping atlases on death, including inside containers

In stable suite **1.1** on `main`, **MapStitch Gameplay Settings → Keep atlases on death** applies to atlases inside Tool Pouches, Tiered Backpacks, shulker boxes and bundles, including combinations nested inside one another. The containing pouch, backpack, box or bundle still drops with its other contents. The atlas returns to the player's normal inventory after respawn, with its maps, name, scale choices and other data preserved.

The setting defaults to off, using the original MapStitch setting and config file. The suite introduces no second switch. `keepInventory` and Simple Death Improvements' independent armor/hotbar/offhand/accessory retention options continue to behave as configured: enabling one of those can deliberately keep additional items.

A vanished atlas or vanishing containing item is not rescued. An atlas still inside a dropped non-vanishing container retains vanilla's existing behavior for enclosed cursed items; this extension does not change nested curse processing.

If more atlases survive than fit in the main inventory, the excess remains in the player's saved data and is returned automatically as slots become available. No outer container or unrelated item is added to that saved list. The atlas itself retains its own contents; its internal map storage is never treated as an outer container to empty.

## Separate implementation

The original MapStitch JAR remains unchanged. The extension lives in `components/mapstitch-mixed-scales/working`:

- `AtlasDeathRetention.java`: recursive extraction from `CONTAINER` and `BUNDLE_CONTENTS`, open-menu/cursor protection and overflow transfer.
- `mixin/AtlasDeathPlayerMixin.java`: death interception, saved overflow and automatic return.
- `mixin/AtlasDeathRespawnMixin.java`: transfer after the normal respawn inventory restoration.

The mapped player inventory includes normal equipment slots. The original optional Tool Pouch and Tiered Backpacks accessory APIs also expose their equipped live stack for extraction when those integrations are installed. Container contents stored in unrelated custom or encrypted component formats are not part of these mods' storage and are not decoded by this extension.

## Historical requested one-off verification

The dedicated fixture is in `qa-death-once`, deliberately separate from the normal QA matrix. It calls the real packaged player's death-drop method and respawn restoration, observes dropped item stacks and retained inventory, and checks the real bundle insertion API. It covers loose/offhand atlases; pouch and leggings attachment; all six backpack tiers and chestplate attachment; open pouch/backpack edits; shulker and bundle storage; nested containers; multiple atlases; full-inventory cursor contents; disabled retention; `keepInventory`; vanishing curse; and retained overflow save/load/recovery.

On 2026-10-05, **all 25 cases passed with and without Polymer: 127 assertions per profile, 254 total**. The tested suite was `1.0.2-merged.2+26.3`, SHA-256 `40c8e7bac2732310a1903b870925e520565c76098ea0f1f8e95e8f4e6c5463e8`. The [candidate-to-final comparison](../qa-merged/evidence/merged2/candidate-to-final-equivalence.json) confirms all 16 nested modules, including the complete death implementation, are byte-identical in the final packaged build; only root settings presentation changed afterward. The actual vanilla bundle predicate and mutable insertion both accepted the atlas; its selected entry was safely reset after extraction.

See [the compact result](../qa-death-once/evidence/result.json), [case matrix](../qa-death-once/evidence/matrix.json), and the [Polymer](../qa-death-once/evidence/death-polymer-audit.json) / [native](../qa-death-once/evidence/death-native-audit.json) dependency audits. Each profile independently checked that only atlases remained after respawn, their own maps survived, no atlas remained duplicated in the dropped container, and all unrelated container contents and inventory items were conserved in the drops.

The fixture deliberately sets Simple Death Improvements' armor/hotbar/offhand retention switches off so those independent features cannot mask retention errors. The optional third-party Trinkets/Ohmega integrations are wired through their existing APIs but are not installed in this one-off baseline; their actual accessory slots are not claimed as runtime-tested here.

Current stable-release checks are recorded separately in [validation](VALIDATION.md); the preceding 254 assertions remain evidence for their exact merged.2 candidate.

For a manual confirmation, turn on **Keep atlases on death**, leave `keepInventory` and the independent retention settings off, place an atlas and a spare item inside one of the supported containers, then die and respawn. The atlas should appear in the inventory; the containing item and spare item should remain in the death drops. Repeat with any container arrangement used in your world.
