# 1.1.5 Bannerpoint and resource-pack sanity QA

Fresh run on 2026-10-06, using released suite `1.1.5+26.3`, SHA-256 `67be52fd07c2d3dcfda4045dff5463e45bc0d91d37d3f759f4ebb3be043cd8f5`. No production changes or rebuild were involved. Defaulted was absent from every server/client runtime. Required Fzzy Config was `0.7.7+fix3+26.3`; SSO was `2.10.0`, Stackables `3.1.1`, MapStitch `1.1.7`, and original Bannerpoint `1.1.2`.

The complete `qa-bannerpoint/run.py --restart` matrix passed: five client profiles, twelve recorded observations, seven non-success resource-pack states and six actual Minecraft screenshots. Native suite clients, untouched original-Bannerpoint-only clients, Fabric API-only clients, pure vanilla, and native suite clients on a server without Polymer all passed their applicable paths. This is fresh evidence on the exact release artifact rather than a relabeled historical result.

## Verified behavior

- Suite and original-only native clients received Bannerpoint's original custom-name payload and decoded it into the actual renderer cache. Unsupported clients received no custom-name payload.
- A genuinely downloaded current Polymer pack rendered both original colored flag sprites. Resource-manager sprite IDs matched their requested IDs and actual locator rendering was traced.
- Fabric API-only fallback began with no flags, preserved an ordinary locator waypoint through every state, and remained unsupported for all seven non-success pack responses. Those seven negatives are direct API checks; successful loading is verified through actual client protocol responses.
- A genuinely loaded stale replacement without Bannerpoint artwork hid the flags. Loading the current pack again restored them. Removing or explicitly declining the pack hid them while preserving the normal locator dot.
- The pure vanilla profile ran Mojang's client entrypoint without Fabric Loader, mods or a QA agent. Its genuine successful pack response and waypoint packets passed. No pure vanilla UI capture is claimed; safe screenshot evidence comes from the Fabric API-only client using the vanilla locator renderer.
- Without Polymer, native Bannerpoint rendered flags and decoded the native banner name. The same server world saved and restarted; original Bannerpoint restored both UUIDs, the custom name, original saved entries/transmitters and the map-linked banner. Breaking the map-linked banner through the player's game-mode method removed only its own waypoint, saved entry and transmitter.

## Fresh resource checks

All 43 direct SSO/Bannerpoint input JSON/PNG resources parsed successfully; custom item-model texture references resolved to original textures. The newly generated Polymer pack passed ZIP integrity. All 36 SSO non-language resources and all five Bannerpoint sprite/style resources were byte-identical to their exact upstream inputs. SSO's language JSON was semantically identical after Polymer serialization. Bannerpoint's omitted language file contains only nine native configuration labels, which fallback clients do not use. Optional upstream SSO builtin rename packs are separate from direct mod assets; this check does not claim that Polymer copies those optional builtin packs.

The input and generated-pack records are `input-artwork-validation.json`, `upstream-resource-manifest.json` and `generated-pack-verification.json`. All six screenshots were visually inspected: accepted profiles show original colored flag icons without missing-texture squares; stale/removed states hide those icons and preserve the ordinary dot. The world banner blocks remain present throughout, as intended.

## Evidence and limits

`result.json` records actual observations and packet/pack states; `sanity-summary.json` records fixture hashes, counts, visual findings and log classification. `launches/` retains sanitized exact mod hashes/commands; `logs/` retains sanitized process logs. Raw temporary worlds and client directories are retained under ignored `qa-bannerpoint/runs/1.1.5-sanity-full/`.

No suite regression or unclassified runtime error was found. All six clients logged expected host/account diagnostics: OpenGL was unavailable and rendering successfully used Vulkan, the offline QA account received Mojang/Realms authentication failures, and the host lacked the native flite narrator library. These are retained and classified rather than hidden or counted as suite failures. Every dedicated server log was free of error matches.

Atlas banner stamping across enabled scales, foreign dimensions, missing/disabled coverage, marker capacity and invalid-edge atomic preflight was separately verified in the 5,314-assertion atlas matrix at `qa-multiscale/evidence/1.1.5-sanity/server/`; it was not duplicated here. Newly completed 1.1.5 Mending, Stackables, inventory-packet and installation tests were not rerun in this matrix. This local suite fixture does not reproduce the user's entire remotely hosted mod stack.
