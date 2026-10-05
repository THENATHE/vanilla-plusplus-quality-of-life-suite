# Shared Region Maps — combo component

Version: **1.0.4-combo.1+mc26.3**. Minecraft 26.3, Fabric Loader >=0.19.5, Java >=25. Runtime Fabric API is not required by this component; the separate QA fixture requires it. MapStitch is optional when building or loading this component alone.

This variant supplies shared ordinary vanilla map IDs and MapStitch integration in Thenathe Mod Suite. It retains the original `shared_region_maps` mod ID and persistent regional-index format. It is loadable on dedicated servers and in clients running an integrated server. It introduces no client packets, registries, resource pack, or settings screen.

- Maps created for the same dimension, aligned region, and scale reuse one map ID and share explored terrain and banners.
- All five vanilla scales have separate records. Zooming enrolled maps reuses the next scale; locked copies remain independent.
- MapStitch automatic maps, insertion, scaling, and missing-center repair retain correct map-center metadata without changing IDs, stack order, or selected entries.
- Legacy or unknown plugin maps, custom data, explorer maps, special decorations, locked records, and unreadable/missing records retain conservative protection.

The regional index remains `<world>/dimensions/minecraft/overworld/data/shared_region_maps/regions.dat`. Back it up together with vanilla map records and the map-ID counter. Existing maps are not rescanned or merged on installation. Removing the component leaves their vanilla IDs usable.

Only vanilla maps and MapStitch have direct integrations. The Map Atlases adapter, Supplementaries antique-ink check within it, Improved Maps incompatibility declaration, and Interdimensional Map Markers exception were removed. Removing the incompatibility declaration does **not** establish compatibility with another map-ID allocator; those mods are outside this suite's supported scope.

Use the suite distribution rather than installing another standalone Shared Region Maps JAR alongside it. Original source is preserved in `../upstream/`; provenance and hashes are in `../upstream-manifest.json`. Full changes and update instructions: `../../../docs/shared-region-maps-changes.md`.

## Build

```sh
JAVA_HOME=/usr/lib/jvm/java-25-openjdk bash ./gradlew build --offline -Pjavac=/usr/lib/jvm/java-27-openjdk/bin/javac
```

`--offline` requires cached dependencies. A Java 25 JDK can compile directly without the explicit `javac` override. `qaJar` builds a separate development fixture excluded from release JARs. Production output: `build/libs/shared-region-maps-1.0.4-combo.1+mc26.3.jar`.

## License

The original locally authored component declares All-Rights-Reserved. Inclusion is authorized by its owner in this task. This declaration must not be replaced with an upstream Pajic MIT notice. Dependencies keep their own licenses.
