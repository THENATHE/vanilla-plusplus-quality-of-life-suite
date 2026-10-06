# Stable 1.1.2 Bannerpoint evidence

`result.json` contains one complete passing five-profile run on suite `1.1.2+26.3` (SHA-256 `c21a103fc19be5121ab3c21bf50c02133dff2719812985010767e3c28cc7c3dd`). Profiles: full suite, untouched original Bannerpoint only, Fabric API only, Mojang vanilla Main with zero mods/loader/agents, and suite native without Polymer.

Native clients decode the original custom-name payload into Bannerpoint's actual renderer cache. A genuine current Polymer pack renders both original colored flags; a genuinely loaded stale replacement without Bannerpoint artwork hides them; restoring the verified current pack restores them. Removing or declining the pack hides flags while preserving the ordinary locator dot. Screenshots wait for the reload overlay to disappear and 20 quiet HUD ticks. Every one of the five generated resources is byte-identical to original Bannerpoint artwork.

The no-Polymer server saves and restarts the same world. Original Bannerpoint restores both named/map-linked banners, UUIDs, saved entries and transmitters without fixture recreation. Breaking the map-linked banner through the real player game-mode method removes only its waypoint, transmitter and saved entry; the named banner and ordinary locator dot remain.

Pure vanilla pack/waypoint protocol is verified, but no pure vanilla UI screenshot is claimed: Wayland focus cannot be established safely. No desktop or unrelated application is captured. Seven non-success states are negative direct API checks; successful loading always comes from actual client protocol responses.

Settings evidence remains separately under `settings/`. Experimental branch evidence is preserved at its original paths.
