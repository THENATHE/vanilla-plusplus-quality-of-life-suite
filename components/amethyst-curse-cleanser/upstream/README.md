# Amethyst Curse Cleanser

A server-side Fabric mod for Minecraft 26.3 with singleplayer support. Vanilla 26.3 clients can join a modded dedicated server and use every feature without installing the mod.

## What it does

Combine one amethyst shard with a cursed tool, weapon, or piece of armor. Taking the output:

- removes Curse of Binding and/or Curse of Vanishing;
- preserves every other enchantment and all item data, including durability, name, lore, and armor trim;
- consumes one amethyst shard; and
- gives you one echo shard. If your inventory is full, the echo shard drops beside you.

One amethyst shard removes both supported curses when an item has both. The operation does not grant grindstone experience.

## How to use it

### Grindstone

Put the cursed item and amethyst shard in the two input slots in either order, then take the cleansed item from the output.

### Smithing table

Leave the template slot empty. Put the cursed item in the middle/base slot and the amethyst shard in the right/addition slot, then take the cleansed item.

## Installation

### Dedicated server

1. Install Fabric Loader 0.19.5 or newer on a Minecraft 26.3 dedicated server.
2. Put the mod JAR in the server's `mods` folder.
3. Do not install Fabric API; this mod does not require it.

Players joining the server do not need to install the mod.

### Singleplayer

1. Install Fabric Loader 0.19.5 or newer in the Minecraft 26.3 client/profile you use to launch the game.
2. Put the mod JAR in that profile's `mods` folder.
3. Launch the Fabric profile. The mod will load in the integrated server and appear in client mod listings.

Fabric Loader does not provide an in-game mods-list screen by itself. Install a compatible Mod Menu build if you want a Mods button inside Minecraft.

The mod deliberately registers no custom items, screens, recipes, or network packets, so unmodified vanilla clients remain compatible.

## Building

Requires JDK 25 or newer:

```bash
./gradlew build
```

The distributable JAR is written to `build/libs/amethyst-curse-cleanser-1.0.1+26.3.jar`.

## 26.3 validation

The 26.3 build uses the explicit server-side inventory prediction mode when granting echo shards. A packaged-JAR test on an isolated 26.3 dedicated server verifies both grindstone input orders, smithing, removal of both curses, preservation of the other enchantment/name/durability, input consumption, and one echo shard per craft.

To repeat with a full JDK 25 or newer:

```bash
./gradlew build -I qa/runtime.init.gradle exportQaClasspath
python qa/run.py
```

The QA harness stays outside the release JAR. It copies the existing development EULA acceptance and creates its own temporary world.

## License

MIT
