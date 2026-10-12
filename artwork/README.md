# Vanilla++ artwork

The built-in imagegen tool generated the three new item sprites and the modpack cover. Item textures are strictly 16 by 16 pixels, nearest-neighbor sampled from the preserved generated artwork; alpha is either 0 or 255. Texture blur is disabled. The modpack cover is a separate branding image and is also used as the suite Mod Menu icon at 256 by 256.

## Prompts

- Magnet: one red horseshoe magnet with silver tips, flat Minecraft vanilla item style, a 16 by 16 logical pixel grid, hard square pixels, transparent background, restrained palette, no antialiasing, text, glow or backdrop.
- Collector: one small brown wicker basket with squared handle, flat Minecraft vanilla item style, a 16 by 16 logical pixel grid, hard square pixels, transparent background, no antialiasing, text, glow or backdrop.
- Wrench: regenerate the earlier wrench as one diagonal silver crescent wrench with copper handle accent, flat Minecraft vanilla item style, a 16 by 16 logical pixel grid, hard square pixels, transparent background, no antialiasing, text, glow or backdrop.
- Cover: square pixel art collage of the suite sprites with VANILLA++ title; brown backpack and pouch, smithing template, wrench, magnet, basket, atlas, whetstone, white and purple chalk, banners, amethyst, inventory stacks, death-recovery motif, elytra and map/compass. Dark slate backdrop, coherent chunky Minecraft item art. References supply shapes, not their UI grids or uncolored tint layers.

## Files

- `vanilla-plusplus-modpack.png`: generated cover.
- `generated/`: preserved generator output for the three item icons.
- `references/` and `sprite-references.png`: original suite item/mod icon references used for the generated cover. These remain covered by their respective original licenses in `../licenses/`; no original mod JAR or original gameplay texture was changed.
- Runtime textures: `../src/main/resources/assets/thenathe_mod_suite/textures/item/` and `../src/main/resources/assets/debugwrench/textures/item/debug_wrench.png`.

The integrated wrench uses only its fixed16 sprite. No resource-pack-resolution detection or alternative32 sprite exists in this update.
