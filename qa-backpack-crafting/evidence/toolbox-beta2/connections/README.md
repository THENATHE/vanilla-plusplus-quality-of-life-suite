# Final toolbox beta2 connections

All four `qa/light.py --all-profiles` connection profiles passed on suite **1.4.0-beta.2+26.3**, SHA-256 `7e734d89c51a35e4203a39ab978f16d9db9ec2bb58a9949ef05dd6b204fe787c`.

- Matching suite client with Polymer server: native toolbox IDs preserved.
- Fabric-only client with Polymer server: iron toolbox represented by `minecraft:iron_axe`, netherite toolbox by `minecraft:netherite_axe`; unsupported backpack opening blocked.
- Unmodified vanilla client with Polymer server: connected successfully, resource pack loaded, unsupported backpack opening blocked.
- Matching suite client with no Polymer server: native toolbox IDs and module capability negotiation preserved.

The high-tier native item intentionally retains serialized ID `vpp_backpack_crafting:diamond_hammer`. The checks verify actual client inventory IDs for observed Fabric/native clients. The unmodified vanilla profile is observed from the server and does not claim a manual crafting interaction.

`multiplayer.json` records assertions and exact bundle hash. `runtime-mods.json` retains runtime JAR hashes without command lines or local paths. `executed-source/` and `fixture-classes.json` identify the executed fixture. Earlier singleplayer and BRBR checks were not repeated because the toolbox changes retain their menu code, serialized IDs and protocol; those earlier records keep their original artifact hashes.
