# Bannerpoint branch checks

Run from the suite source root with the frozen branch JAR:

```bash
python3 qa-bannerpoint/run.py \
  --jar build/libs/vanilla-plusplus-quality-of-life-suite-1.1.2+26.3.jar \
  --label stable112-bannerpoint-final --restart
```

This uses the retained Minecraft 26.3/Fabric launch libraries, Java 25 at runtime, Java 27 to compile disposable fixtures, the official Defaulted drop-fix build, and a graphical X11 display. It creates fresh local worlds and an HTTP server serving Polymer's genuinely generated resource pack. The existing accepted QA EULA is reused. It does not contact a public Minecraft server.

The five client profiles cover the full suite, the untouched original Bannerpoint mod without the suite, Fabric API without Bannerpoint, pure vanilla with no loader or mods, and the full suite connected to a server without Polymer.

The fixture places a named red banner and links an unnamed blue banner through the real vanilla map API. Native clients must decode the original banner-name payload into Bannerpoint's actual name renderer cache. With `--restart`, the no-Polymer server cleanly saves and restarts the same world. The test checks original Bannerpoint startup restores both transmitters, saved banner entries, UUIDs and the custom name without recreating the blocks. It then destroys the blue banner through the real player game-mode block-breaking method and requires only that banner's waypoint/transmitter/saved entry to disappear. The normal locator waypoint and named red banner remain.

The Fabric profile first declines the pack and receives no banner waypoints. Seven non-success states are additionally checked through the compatibility API on the already offered main-pack UUID; these are negative state-machine checks, not injected successful-load acknowledgements. The client then allows and actually downloads the pack. Its waypoint manager, four GUI atlas sprites and real Locator Bar rendering are inspected. The test replaces the accepted pack with a real downloadable stale pack lacking Bannerpoint artwork, verifies that successful loading still keeps banners hidden, restores the current pack, removes it and explicitly declines another offer. An ordinary vanilla locator waypoint must survive every state. Native name packets must never reach the fallback client.

The pure vanilla profile runs Mojang's `Main` directly, with no QA client mod, loader or agent. Vanilla's saved server-pack policy enables the genuine download. Server-side status and packet observations establish successful loading and delivery of both banners. Pure vanilla UI capture is not performed because this Wayland session does not reliably focus the test window. Actual original-sprite rendering and screenshots are verified with the Fabric API-only client, which has no Bannerpoint or suite rendering code. No desktop or unrelated application is captured.

The fixture installs the generated pack's actual SHA-1 into the disposable server's resource-pack offer before clients connect. This preserves the normal configuration-stage offer while avoiding a checksum guessed before generation. The native clients still work without a pack, and the no-Polymer profile verifies that optional Polymer classes do not prevent native startup and connection.

Raw runs, worlds, fixture JARs and logs stay under ignored `runs/`. Stable release evidence is retained in `evidence/1.1.2/`; previous experimental branch evidence remains at its original paths. Settings-screen and unchanged-module comparison evidence is maintained separately.
