# Nested item-template packet regression

This dedicated-server fixture encodes and decodes `ClientboundContainerSetContentPacket.STREAM_CODEC` inside Fabric's real scoped `PacketContext`, using a serverbound `Connection` so Polymer's `ItemStackTemplate` codec transformations are active. It does not substitute an isolated `getPolymerItemStack` call for the actual inventory packet codec.

It checks ordinary and netherite pouches, an attached netherite pouch and backpack, a standalone netherite backpack, sparse shulker inventories, a second shulker nesting level, a shulker inside a netherite pouch (standalone and attached), bundles, atlas map/paper contents, charged projectiles, and use remainders. Recipients include native suite clients, fallback clients, and partial native clients with Tool Pouch/SSO/MapStitch/backpacks/Chalk but without Stackables. It checks empty outer slots/cursor, decoded templates, original-stack immutability, exact native types/positions/quantities, top-level quantity preservation, explicit stack-limit precedence/removal, live changes/clear, and malformed negative-count templates. For fallback clients it checks the actual component patch plus vanilla prototype maximum, independently of the server's configured override getter. Uncapped profiles include 1,243 and 2,048 stone items.

Run from the repository root:

```sh
python3 qa-packet-templates/run.py --label <unique> --jar <candidate.jar>
python3 qa-packet-templates/run.py --label <unique> --jar <candidate.jar> --uncapped
```

Each invocation starts disposable Polymer and no-Polymer dedicated worlds, with current suite dependencies and no Defaulted runtime library. It records the exact suite/dependency SHA-256 values, commands, console logs, and every case in `runs/<label>/`. A reproduced production failure is reported as `passed: false` and an exit status of 1; it is not relabeled as a passing expected-failure check.

Frozen 1.1.5 initial reproduction (`baseline115-default-02`) showed six real encoding failures: a nested shulker containing potion x3, saddle x16, or enchanted book x64 failed with `Stack must be non-empty`, for both native and fallback contexts. The console first reports strict template validation using the vanilla maximum of 1. This is distinct from legitimate sparse slots, which are represented internally by `Optional.empty()` and do not create empty templates. The stronger final fixture also checks decoded nested values and uncapped fallback strict validity.

The user's exact arrangement was subsequently reproduced in `baseline115-exact-pouch-default`: a netherite pouch containing a shulker with potion x3, saddle x16, or enchanted book x64 produces the same encoding exception for all three recipients. Its immutable 1.1.5 JAR is copied from the published release folder. The getter-only candidate passes the default profile but has separately recorded uncapped fallback failures, motivating safe outgoing preview handling while preserving actual inventories.

Final 1.1.6 candidate SHA-256 `ea383c52581970f5b69eb0db0cffdc9c26a0a0efb2f80e0ab82f8b8f9d2a5f86` passes all four default/uncapped and Polymer/no-Polymer profiles: **28,156 assertions** across **1,742 cases**. Native contents retain exact item types, slot positions, and full quantities; fallback metadata caps effective maxima at 99, retains safe previews, and omits only unsafe outgoing preview components while leaving authoritative contents untouched. Explicit limits on native carrier mods without Stackables are covered, including maxima of 2,048 and a live registry maximum of 84. Strict validation continues to reject a genuinely invalid damageable item with maximum stack size 2.

The final API guard checks use Polymer's four-argument `getPolymerItemStack` overload with `TooltipFlag.NORMAL`. This directly exercises the hooked method for clientbound and no-connection contexts. The three-argument convenience overload requires a connection to obtain the player's tooltip preferences and is unsuitable for the no-connection fixture; the initial fixture correction and its results are retained separately. Evidence and provenance are in `evidence/1.1.6/summary.json` and its checksum manifest.

Limit: contexts are synthetic, with real Fabric and Polymer scoped codec behavior, not independently connected graphical clients. Network integration tests must separately confirm recipient negotiation and a real connection.
