# Compile dependencies

When building the combined suite from its root, use `tools/stage-inputs.py` as described in [the suite update guide](../../../../docs/UPDATING.md#rebuilding-inputs). Every suite component uses the verified root inputs. ClientSort is required only to compile the optional sorting integration; staging places it in `libs/compile-only/` so it stays out of the bundled feature JAR and normal QA runtime profiles.

The standalone dependency table below preserves the original 1.0.4 build inputs. Current suite 1.1.5 compiles against MapStitch 1.1.7 and Fzzy Config 0.7.7+fix3+26.3 via the root build; do not use this historical table as its installation requirements.

For that historical standalone component build, obtain the following exact publisher artifacts and place them in this component's `libs/` directory. They are ignored by Git and excluded from its output JAR.

| Filename | Source |
| --- | --- |
| `clientsort-fabric-3.104.1+26.3.jar` | [ClientSort 3.104.1](https://modrinth.com/mod/clientsort/version/UWMryUad) |
| `toolpouch-fabric-1.1.10+26.3.jar` | [Tool Pouch](https://modrinth.com/mod/tool-pouch) |
| `mapstitch-fabric-1.1.6+26.3.jar` | [MapStitch](https://modrinth.com/mod/mapstitch) |
| `fzzy_config-0.7.7+fix2+26.3.jar` | [Fzzy Config exact release](https://modrinth.com/mod/fzzy-config/version/thw1Z19c) |
| `kotlin-stdlib-2.4.20.jar` | Extract unchanged from the pinned Fabric Language Kotlin JAR's `META-INF/jars/` directory |

MapStitch is required to compile its integration even if only the Elytra feature is used at runtime. Gradle resolves the pinned Minecraft, Fabric Loader and Fabric API dependencies. ClientSort remains optional at runtime: installing its original client mod enables sorting, and installing its original server mod enables its acceleration path. Do not bundle these compile dependencies.
