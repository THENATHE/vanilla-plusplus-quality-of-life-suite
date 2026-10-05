# Shared Region Maps Shim

**One region, one shared map record.** A server-side Fabric shim that lets players explore the same ordinary map together, with direct adapters for MapStitch, Map Atlases, and Interdimensional Map Markers.

When players create maps of the same region, dimension, and scale, they receive the same vanilla map ID. Exploration and banner changes belong to that shared record, so players build on one another’s map progress.

The installable mod is named **Shared Region Maps**. Current release: **1.0.3+mc26.3**.

[Download releases](https://github.com/THENATHE/shared-region-maps-shim/releases) · [Validation](VALIDATION.md) · [MapStitch Polymer Shim](https://github.com/THENATHE/mapstitch-polymer-shim)

## Features

- **Shared regional maps:** ordinary empty-map use reuses the canonical map ID for its region, dimension, and scale.
- **All five vanilla scales:** 1:1, 1:2, 1:4, 1:8, and 1:16 each retain their own records. Different dimensions and world saves remain separate.
- **Shared exploration and banners:** copies refer to the same terrain and named-banner data, including maps held by players or displayed in item frames. Updates arrive through normal vanilla map updates.
- **Shared zooming:** cartography-table and crafting zooms of enrolled maps use the canonical record at the next scale. The input record remains unchanged.
- **Independent locked copies:** vanilla map locking produces a separate snapshot that stops receiving changes from the shared original.
- **Persistent IDs:** regional associations survive restarts and upgrades. Repeated lookups do not consume unused map IDs.
- **Existing-map preservation:** legacy maps, unknown custom maps, and plugin artwork are not scanned, relabeled, imported, or merged.
- **MapStitch integration:** automatically generated atlas maps share regional records; missing or stale map-center metadata is corrected on supported creation, insertion, scaling, and repair paths.
- **Map Atlases integration:** ordinary maps created through the supported port’s vanilla map path share records with loose maps and other players’ atlases.
- **Interdimensional player markers:** the marker mod’s Nether/End player icons no longer interrupt sharing or shared zooming.
- **Small server footprint:** no new items, screens, custom packets, configuration file, required resource pack, or client installation. The standalone mod needs neither Fabric API nor Polymer.

This shares discovered map data; it does not reveal unexplored terrain or explore unloaded chunks. Item-frame and inventory updates still follow Minecraft’s normal update timing. Atlas-specific waypoints remain owned by their atlas mod.

## Install

| Requirement | Version |
| --- | --- |
| Minecraft Java dedicated server | 26.3 |
| Fabric Loader | 0.19.5 or newer compatible with Minecraft 26.3 |
| Java runtime | 25 or newer |

1. Download `shared-region-maps-1.0.3+mc26.3.jar` from Releases.
2. Put it in the dedicated server’s `mods/` directory, replacing any older Shared Region Maps JAR.
3. Restart the server and create a fresh ordinary map to enter the shared system.

Clients install nothing for Shared Region Maps. Optional atlas mods retain their own client and dependency requirements. This shim does not give vanilla clients an atlas GUI.

## How it works

The shim intercepts supported map-creation and zoom paths and looks up a persistent regional index before requesting a new vanilla map ID. Each key contains the dimension, actual map center X/Z, and scale; the index is stored inside that world save. Matching requests reuse the existing vanilla `MapItemSavedData` record, which is why pixels and banners stay shared without a custom synchronization protocol.

Grid alignment follows Minecraft’s fixed origin, including negative coordinates:

```text
width  = 128 << scale
center = floor((coordinate + 64) / width) * width + width / 2 - 64
```

Only recognized creation paths enroll maps. General `MapItem.create` calls keep independent allocation. Shared zooming requires an enrolled ID and an ordinary map record; matching pixels alone never establish provenance.

## Mods with direct compatibility layers

| Mod | Direct integration | Tested edition |
| --- | --- | --- |
| **MapStitch** | Redirects automatic atlas map generation to the regional index; initializes map centers before insertion; refreshes centers on insertion/scaling; repairs missing centers in existing carried atlases. | Original 1.1.6+26.3 and Toolpouch-patch.1 |
| **Map Atlases** | Adapts `MapType.VANILLA.createNewMapItem` for ordinary, nonsliced maps. Reconstruction and extraction preserve the resulting canonical ID. | Custom Fabric 26.3-6.7.3-port.5 |
| **Interdimensional Map Markers** | Accepts its exact registered vanilla red/blue player-marker types while that mod is loaded, preserving reuse and zoom with players in other dimensions. | 1.0.5 |

These adapters activate only when their corresponding mod is present. They do not require rebuilding the target mods. The Map Atlases adapter targets the listed custom port; compatibility with every upstream release is not implied.

### Related mods and safeguards

- **[MapStitch Polymer Shim](https://github.com/THENATHE/mapstitch-polymer-shim):** a separate companion for servers mixing vanilla and MapStitch clients. Shared Region Maps supplies map sharing; that shim supplies the Polymer fallback and client compatibility. Shared Region Maps has no direct Polymer adapter or Polymer requirement.
- **Moonlight, CodecUI, and Accurate Maps:** tested alongside the Map Atlases stack. They are dependencies or coexistence checks, not additional direct adapters in this mod.
- **Supplementaries:** a conservative presence check disables sharing for Atlas-created maps because antique-ink processing can alter their saved records. This is an exclusion safeguard, not support for antique or sliced maps.
- **Improved Maps:** intentionally incompatible. It controls allocation with another registry; Fabric rejects the combination so the two allocators cannot compete.

## Preservation and limits

Maps created before installation, unknown plugin-created maps, explorer/treasure maps, sliced/antique maps, and unsupported custom maps remain independent. Zooming an unenrolled map also remains independent. Items carrying custom data or target decorations bypass shared zooming even when their ID is enrolled.

MapStitch repair preserves IDs, explored pixels, stack order/counts, names, decorations, and the selected bundle entry. It does not merge duplicate stacks, reinsert through ordinary bundle capacity limits, or invent a missing saved map record. Missing centers inside existing atlases are repaired on inventory ticks; already-present centers are refreshed on insertion/scaling rather than repeatedly checked every tick.

The regional index is saved at:

```text
<world>/dimensions/minecraft/overworld/data/shared_region_maps/regions.dat
```

Back up the index together with vanilla map records and the map-ID counter. If an enrolled record is missing, locked in place, recentered, or changed into an unsupported special map, the mod refuses creation for that region instead of replacing the record. An unreadable index is also left untouched.

Removing the mod leaves existing maps usable as vanilla maps. Copies with the same ID continue sharing that record. Reinstalling with the same index resumes regional reuse. Upgrading to 1.0.3 preserves the mod ID, index format, and existing map IDs.

## Java integration

Server-side mods can explicitly request a supported ordinary shared map:

```java
import org.sharedregionmaps.SharedMaps;

ItemStack map = SharedMaps.create(level, x, z, (byte) scale, true, false);
```

Call on the server thread with a `ServerLevel` and scale 0–4. Callers must not subsequently recenter or render special content into the shared record. Custom Java integrations using the earlier package name must update their imports and recompile for 1.0.3.

## Build and validation

Use a complete **Java 25 JDK** for Gradle. Set `JAVA_HOME` when another Java version is the default.

```sh
bash ./gradlew build
bash ./gradlew qaJar
```

Gradle downloads the pinned Minecraft/Fabric dependencies. Add `--offline` only when they are already cached. Production output is in `build/libs/`; `qaJar` builds a separate development fixture in `build/qa/`, excluded from the installable mod.

The dedicated-server fixture can use an explicitly supplied, already accepted EULA file:

```sh
bash ./gradlew -PliveQa -PqaEulaFile=/path/to/accepted/eula.txt runServer
```

Use a disposable world. Run it twice against the same QA directory to check persistence. Recorded release validation includes **13,328 assertions**, upgrade preservation, corrupted-index protection, optional dependencies, and combined adapters. See [VALIDATION.md](VALIDATION.md) for exact scope; those results do not claim a new graphical-client playtest.

## License

The existing mod license declaration is **All-Rights-Reserved**. Publishing this repository does not change that declaration. Third-party dependencies retain their own licenses.
