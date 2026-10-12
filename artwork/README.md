# Vanilla++ artwork

The built-in imagegen tool generated the three new item sprites and the modpack cover. Item textures are strictly 16 by 16 pixels, nearest-neighbor sampled from the preserved generated artwork; alpha is either 0 or 255. The local 1.2.1 collector replacement fills 14 by 14 visible pixels within that canvas, replacing the earlier basket that occupied only 6 by 6. Texture blur is disabled. The modpack cover is a separate branding image and is also used as the suite Mod Menu icon at 256 by 256.

## Prompts

- Magnet: one red horseshoe magnet with silver tips, flat Minecraft vanilla item style, a 16 by 16 logical pixel grid, hard square pixels, transparent background, restrained palette, no antialiasing, text, glow or backdrop.
- Collector, original 1.2.0: one small brown wicker basket with squared handle, flat Minecraft vanilla item style, a 16 by 16 logical pixel grid, hard square pixels, transparent background, no antialiasing, text, glow or backdrop.
- Collector, 1.2.1 replacement: the exact edit prompt is preserved below. The built-in imagegen output is resized with nearest-neighbor sampling and centered as 14 by 14 visible pixels in the 16 by 16 runtime sprite.
- Wrench: regenerate the earlier wrench as one diagonal silver crescent wrench with copper handle accent, flat Minecraft vanilla item style, a 16 by 16 logical pixel grid, hard square pixels, transparent background, no antialiasing, text, glow or backdrop.
- Cover: square pixel art collage of the suite sprites with VANILLA++ title; brown backpack and pouch, smithing template, wrench, magnet, basket, atlas, whetstone, white and purple chalk, banners, amethyst, inventory stacks, death-recovery motif, elytra and map/compass. Dark slate backdrop, coherent chunky Minecraft item art. References supply shapes, not their UI grids or uncolored tint layers.

## Exact 1.2.1 basket prompt

```text
Use case: precise-object-edit. Asset type: Minecraft 16x16 inventory item sprite. Edit target: supplied brown wicker pickup basket with square handle and tiny cyan plus. Enlarge the basket to occupy almost the full square canvas, with only one logical pixel of transparent margin on every side. Preserve recognizable brown wicker basket, squared handle, tiny cyan plus and vanilla Minecraft pixel art palette. Render all artwork on a strict 16x16 logical pixel grid, enlarged nearest-neighbor to output size; each logical pixel must be one flat solid color with hard square edges, no gradient, no antialiasing. Basket silhouette must occupy 14x14 logical pixels centered. Transparent background. No text, no added objects, no border, no backdrop. This is a single flat item sprite, not a 3D render.
```

## Files

- `vanilla-plusplus-modpack.png`: generated cover.
- `generated/`: preserved original generator output for the three item icons.
- `generated/pouch_collector-1.2.1.png`: fresh built-in imagegen basket output; the original `generated/pouch_collector.png` remains preserved.
- `references/` and `sprite-references.png`: original suite item/mod icon references used for the generated cover. These remain covered by their respective original licenses in `../licenses/`; no original mod JAR or original gameplay texture was changed.
- Runtime textures: `../src/main/resources/assets/vpp_mod_suite/textures/item/` and `../src/main/resources/assets/debugwrench/textures/item/debug_wrench.png`.

The integrated wrench uses only its fixed16 sprite. No resource-pack-resolution detection or alternative32 sprite exists in this update.
