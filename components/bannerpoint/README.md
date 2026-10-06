# Bannerpoint developer component

Stable suite **1.1.4** on `main` includes [Pajic’s Bannerpoint](https://modrinth.com/mod/bannerpoint) as a separate unchanged developer module. Thanks for your hard work, **pajic**!

| Record | Exact input |
| --- | --- |
| Publisher release | [Bannerpoint 1.1.2+26.3 for Fabric](https://modrinth.com/mod/bannerpoint/version/Vrns75Ns) |
| Publisher version ID | `Vrns75Ns` |
| Original filename | `bannerpoint-fabric-1.1.2+26.3.jar` |
| Fabric metadata version / ID | `1.1.2` / `bannerpoint` |
| SHA-256 | `499823f5adf1dce27f62362b1674eb9f1b7f3d9d821dccfbf611e00bf8b5acc0` |
| License | MIT; original notices retained |
| Minecraft / loader | `26.3` / Fabric |
| Stable suite | `1.1.4+26.3` |
| Separate compatibility module | `components/bannerpoint-compat/`, `1.0.1+26.3` |

The developer binary, published source archive and publisher metadata are preserved independently from the suite compatibility source. Root artifact locks and [provenance](../../docs/PROVENANCE.md) identify their precise source locations and hashes. A GitHub source snapshot is update context, not a substitute for the published source archive associated with this binary. No modified original JAR or Bannerpoint version port is produced by this integration.

## Native features and settings

Bannerpoint adds colored banner waypoints to Minecraft’s locator bar, with server-configurable named/map-linked transmission and range. Its client renderer shows names using the original player-list/sneak interaction and name appearance options. It does not add atlas-map decoration textures.

The original Fzzy IDs `bannerpoint:config` and `bannerpoint:client_config` remain authoritative. The suite groups them as **Bannerpoint Gameplay Settings** and **Bannerpoint Client Settings**, retaining original fields, validation, files, permissions and relog notices. Bannerpoint remains an individual feature entry in Mod Menu; the compatibility addon is grouped under the suite.

## Separate compatibility and resource pack

`components/bannerpoint-compat/` owns detection, pack-asset contribution and per-player banner-waypoint gating. Native clients are detected using Bannerpoint’s original advertised `bannerpoint:banner_name` channel, including untouched original clients without the suite. Other clients receive icons only after Polymer’s main server pack successfully loads; pending, failed, declined and removed packs keep banner waypoints hidden. Ordinary player waypoints and saved banner UUIDs/data are preserved.

Bannerpoint’s style JSON and four locator-bar sprites are normal Minecraft resource-pack assets. They can be included unchanged in Polymer’s generated pack. Custom banner-name labels require the original client code and are not added through the pack. See [Bannerpoint support](../../docs/BANNERPOINT.md) for the client matrix, exact libraries, installation and testing steps.

The compatibility module also reconciles map-linked tracking after a coordinated atlas operation, preserving transmission when a disabled layer in that atlas still carries the mark. It does not globally count references from unrelated books. The suite’s separate mixed-scale atlas addon coordinates banner marks across enabled generation scales; that change is outside the original Bannerpoint JAR and the locator-bar compatibility module. See [mixed-scale maintenance](../../docs/MIXED_SCALES.md).

## Update procedure

1. Preserve the current published binary/source and metadata before selecting a new official release for the target Minecraft version.
2. Record publisher version ID, source and binary hashes, dependency declarations and original config IDs in the component/root locks.
3. Compare published waypoint factories, original naming channel, style resources and saved-data lifecycle with the separate compatibility source; keep the original JAR unchanged.
4. Refresh the original asset contribution and complete English settings-title resources, preserving upstream strings and settings semantics.
5. Build with retained suite dependencies, then verify native clients, clients without Bannerpoint with successful/declined/failed/removed packs, player-waypoint preservation and banner persistence against the final build hash.
6. Preserve prior testing artifacts as historical records and publish new verified updates under the stable suite’s own version. Follow [the source/update guide](../../docs/UPDATING.md) and [release procedure](../../docs/RELEASE_WORKFLOW.md).
