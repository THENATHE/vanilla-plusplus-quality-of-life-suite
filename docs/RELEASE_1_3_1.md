# Vanilla++ 1.3.1+26.3

This patch corrects three issues found during the 1.3.0 code review:

- Enchanted anvil outputs charge XP for both normal pickup and shift-click. SSO's configured free repairs for unenchanted equipment still work.
- Netherite scrap can enter the additional grindstone slot for SSO's repair-cost reduction recipe. The recipe follows SSO's own switch independently of enchantment extraction.
- Pet recall checks the space and hazards occupied by the animal and its passengers and attached animals before teleporting.

The fixes live in this suite. All original nested mod JARs and dependency versions are retained unchanged, with existing attribution and source boundaries. The independent SSO port remains paused.

## Installation

Replace the previous suite JAR with `vanilla-plusplus-quality-of-life-suite-1.3.1+26.3.jar` on the server and matching native clients. Minecraft 26.3, Fabric Loader 0.19.5 and Java 25 or newer are required. See [exact dependencies](DEPENDENCIES.md) and [installation pack](INSTALLATION_PACK.md). No save migration or configuration reset is required.

The four release assets are README.md, the suite JAR, docs.zip and `vanilla-plusplus-installation-pack-1.3.1+26.3.zip`. Fzzy Config remains an official manifest download.

## Verification

[Validation](VALIDATION.md) and the [build record](build-verification.json) identify the tested artifact and the fresh regression, networking, Hotkey Wheel and installer evidence. Earlier release records retain their original results and limitations.
