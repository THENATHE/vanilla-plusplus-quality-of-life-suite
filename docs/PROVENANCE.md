# Provenance and compatibility tracks

Stable suite **1.1.5+26.3**, on `main`, targets Minecraft 26.3, Fabric Loader 0.19.5 and Java 25. Its 18 nested modules include official SSO **2.10.0**, MapStitch **1.1.7**, Sensible Stackables **3.1.1** and Bannerpoint **1.1.2**. The coordinator is **1.1.1+26.3** and the mixed-scale addon is **1.1.5+26.3**. Combined compatibility is **1.1.1+26.3**, Stackables compatibility is **1.0.1+26.3**, and the pouch addon is **1.0.9-suite.1+26.3**. Existing atlas maintenance/copying, multiscale banners, saved IDs, configuration identities, Unbreaking and SSO material repair remain intact. This release replaces the active Stackables port with the unchanged official developer binary and adds native guards for the new repairables/stack-size payloads. It fixes the independently reproduced Clumps early-return bypass of pouch XP repair. Defaulted leaves the runtime/installation set; its exact historical input remains compile-only for guarded optional compatibility.

This distribution combines original developer releases, existing ports/additions and suite compatibility code. It does not relabel original mods as official releases of the suite. Bannerpoint's developer-release compatibility track is supported here; no ChatGPT Bannerpoint port exists or is created. Other existing compatibility tracks and the paused SSO port remain preserved.

The [2026-10-06 publisher audit](pajic-release-audit-2026-10-06.json) distinguishes newest releases for this Minecraft target from newer releases for other targets. The [verified comparison](pajic-input-verification-2026-10-06.json) records source references, hashes, dependencies and unchanged inputs; repository commits are reference snapshots, not invented exact binary build revisions.

## Included inputs

| Module/library | Artifact version | SHA-256 | Origin |
| --- | --- | --- | --- |
| bannerpoint | 1.1.2 | `499823f5adf1dce27f62362b1674eb9f1b7f3d9d821dccfbf611e00bf8b5acc0` | Official developer Fabric 26.3 release [Vrns75Ns](https://modrinth.com/mod/bannerpoint/version/Vrns75Ns); exact source record components/bannerpoint/inputs.lock.json. |
| amethyst_curse_cleanser | 1.0.1+26.3 | `5ec26e2a0010dfdee82aa5cb27664314150a4daa36deadc10f0c659f399507b6` | Existing local authored 26.3 mod; source snapshot components/amethyst-curse-cleanser/upstream |
| chalk | 3.2.1+26.3 | `886b0b10cc12df65d47f446c2bd54c0c670f9a54fa09a1fe95c9ec46df565f49` | Existing ChatGPT Fabric 26.3 port of DaFuqs Chalk; source snapshot components/chalk/upstream; historical git dfb461ef9eb6f7bf5188570594c3bd21171c2391 |
| chalk-colorful-addon | 2.1.1-port.1+26.3 | `69a2443250abd95100636194a145b5479d240151cd19bf4186766117d0710b34` | Existing metadata-only ChatGPT 26.3 port; original uploaded addon retained; source components/chalk-colorful/upstream |
| codecui | 26.3-1.4.3 | `4d07c219bd85b58283a0317d6f43cca838adf9cd16be321316cd32f2fb1be898` | Retained 26.3 CodecUI fork-family input, exact private build commit unrecorded; Extension API matches Pajic fork reference components/codecui-reference. Separate ChatGPT Access-based 26.3-port.1 artifact is not used. |
| defaulted (compile-only historical compatibility) | 1.3.8+26.3.dropfix.1 | `e339d6f0eb471a4ac41185fb9dbe0cfaa78a290c6ceedf92a49ba9110f732c61` | Retained compile-only local dropfix modification; not installed at runtime by this release. Exact original hash 0f6efc6423902b83da78ce85a119c69536517b1a9af52ed7046858859ff582a0; source components/defaulted-dropfix |
| fabric-language-kotlin | 1.14.1+kotlin.2.4.20 | `620c2709be2a262b837cf976f6f85f6eda80a56af2ba4242e0bdc83a60fee7a5` | Pinned existing runtime library, unchanged archive |
| fzzy_config | 0.7.7+fix3+26.3 | `3d98c61215ae5eb2fe08db5cac6b0da08dc1b6f0a9ac082a3a9ead8eed736b2a` | Byte-identical official public [Modrinth release YkOumqzV](https://modrinth.com/mod/fzzy-config/version/YkOumqzV), verified 2026-10-06; externally installed, never bundled; comparison in components/fzzy-config-reference/ |
| mapstitch | 1.1.7 | `d7949f082d365fa4b71ce29b7fdce21ecec3f759ef9f9da9be34b880fc64b5e4` | Official developer Fabric 26.3 release [oDB5IJ7O](https://modrinth.com/mod/mapstitch/version/oDB5IJ7O); components/mapstitch/inputs.lock.json. All 90 Java classes match 1.1.6; published source changes only Russian translation. |
| misctweaks | 1.4.4 | `97fe263764cb6b93822e13fe9423cf91c3a6a0169aeeb8734b65136cc9ea9647` | Official developer Fabric 26.3 release; components/misctweaks/inputs.lock.json |
| mixson | 2.2.1 | `e4d38f31be95a1f03f8bef290c079f4bb5573d8f4e15808e8c1fc8ba3e5913a0` | Pinned existing runtime library, unchanged archive |
| simple_death_improvements | 1.6.0 | `67070a8add053ee1a2cad1c310d36cdac0cf53c7950873e20d43d7da8a0ffcc2` | Official developer Fabric 26.3 release; components/simple-death-improvements/inputs.lock.json |
| simple_smithing_overhaul | 2.10.0 | `b23d91bf8bf71eb9747a32cad1caf3e7a0ab4618428850c7c40e1eae9431db47` | Official public Fabric 26.3 release [k5NaPhrq](https://modrinth.com/mod/simple-smithing-overhaul/version/k5NaPhrq); historical private official 2.9.14 and public 26.2 baseline preserved separately. |
| sensible_stackables | 3.1.1 | `bf750cbe40394caffe3fd7c9bc2b57985d4110c5cae51dc726f948b55a25598a` | Official public Fabric 26.3 release [oRpBICTz](https://modrinth.com/mod/sensible-stackables/version/oRpBICTz); exact published source/binary records under components/sensible-stackables/developer-release/. |
| tiered_backpacks | 1.0.20 | `b129b9b61ec1da62842dc2dfa5b166a6df8e91c796666dcee70f7b8acf1ecc33` | Official developer Fabric 26.3 release 1.0.20; components/tiered-backpacks/inputs.lock.json |
| toolpouch | 1.1.10 | `a5bb4f084e7d929f3a89cc43317af63bd45abe3e3f4fed8b45f99b2da846e903` | Official developer Fabric 26.3 release; components/toolpouch/inputs.lock.json |
| compile-only Kotlin stdlib | 2.4.20 | `078594f80e214438a35454e3f16f890a68dba6a607aa2c83014065344d13308a` | Compile-only unchanged META-INF/jars/kotlin-stdlib-2.4.20.jar extracted from pinned Fabric Language Kotlin archive |

## Modified suite components

| Component | Suite version | Baseline / record |
| --- | --- | --- |
| Combined compatibility | 1.1.1+26.3 | Original standalone Multi-Shim 1.0.5+26.3; components/combined-compat/ORIGIN.json |
| Chalk compatibility | 1.1.1-suite.1+26.3 | Original standalone Chalk shim 1.1.0+26.3; components/chalk-compat/ORIGIN.json |
| Shared Region Maps | 1.0.5-combo.1+mc26.3 | Original local 1.0.3, commit 338270a70c14be19ba79b535b40fac980e414470; components/shared-region-maps/upstream-manifest.json |
| Tool Pouch atlas/Elytra addon | 1.0.9-suite.1+26.3 | Original local 1.0.4; components/toolpouch-atlas-elytra/upstream-manifest.json |
| Mixed-scale MapStitch addon | 1.1.5+26.3 | Separate module components/mapstitch-mixed-scales/working; exact stable coordinator dependency |
| Sensible Stackables historical port (not nested) | 3.0.3-port.1+26.3 | Preserved published 3.0.3+26.2 baseline and independent port source/dependency records in components/sensible-stackables/ |
| Sensible Stackables Polymer compatibility | 1.0.1+26.3 | Separate components/sensible-stackables/compat module |
| Bannerpoint compatibility | 1.0.1+26.3 | Separate components/bannerpoint-compat module; original artwork notices and client/pack detection retained. Reconciles map-linked tracking after multiscale atlas edits through the internal addon event. |
| Suite root / settings | 1.1.5+26.3 | Newly authored source in src/ |

The production archive retains these original module IDs and embeds each module separately. Original standalone repositories and versioned releases remain available; the suite variants do not overwrite them.

## Private official SSO release and paused port track

The user confirmed on **2026-10-04** that the retained `simple_smithing_overhaul-fabric-2.9.14+26.3.jar` is an **official private developer release**. It was privately released while the Defaulted maintainer was unavailable. This provenance is user-supplied; there is no invented public Modrinth download or source commit for that binary. The user explicitly authorized full combined-suite testing with this exact artifact.

The public Modrinth 2.9.14 release targets 26.2 and is a different binary. The retained official private 26.3 binary has 81 of 83 classes identical to that public binary; its two changed classes correspond to the 26.3 conditional branches present in published upstream source. This corroborates version-specific source consistency without establishing a private build commit. Read-only evidence: `components/sso/upstream/metadata/local26.3-analysis.json`.

The previous public 26.2 and private official 26.3 input records remain in versioned baselines. Current `components/sso/inputs.lock.json`, `components/sso/suite-input.lock.json` and root `locks/artifacts.json` select public official 2.10.0+26.3. Never substitute the historical differently hashed binaries for the current locked input.

The separately named ChatGPT SSO **2.9.14-port.1+26.3** project, its companion port-target shim and historical tests remain paused. No source changes, builds or tests of that distinct project are part of this suite. Using an official developer release does not resume the separate ChatGPT port track.

## Chalk track separation

The suite uses the exact local Chalk 3.2.1+26.3 port and colorful-addon 2.1.1-port.1+26.3 input. Its new compatibility variants are compiled and tested for those inputs only. Historical original Chalk 3.2.0+26.2, original colorful-addon 2.1.1+1.19.3 and standalone shim 1.1.0+26.2 remain in their existing source/release track. This suite does not modify or newly verify that 26.2 installation. Sensible Stackables now uses its official 26.3 developer release; its previous 26.3 port and untouched 26.2 baseline remain historical, separately identifiable tracks. The other Pajic modules have no separately maintained ChatGPT port in this suite; no unsolicited port is invented.

## Validation authority

Only a run against the final suite artifact and its exact dependency hashes verifies this combination. Historical standalone tests, unchanged binary identity and source/API compile checks are recorded as context and do not establish new native/fallback or graphical correctness. See docs/VALIDATION.md for actual completed cases, pending manual steps and the final tested hash.

## Retained library source families

The CodecUI input is `26.3-1.4.3`, SHA-256 `4d07c219…be898`. Its HolderSetCodecExtension API matches the pinned Pajic fork reference, whose current reference source declares 1.4.4. It is therefore source-family/update context, not an invented exact private build commit. The separate historical ChatGPT CodecUI port 1 uses HolderSetCodecAccess and is not the suite target. See components/codecui-reference/README.md.

Defaulted's retained original 26.3 input and exact dropfix modification are separate records. The pinned Pajic fork source is future-update context; the patcher applies only to the exact original binary. The historical dropfix output remains SHA-256 `e339d6f0…32c61`. The original mods now implement their own repair/stack synchronization, so Defaulted is no longer installed or required by the suite. Its guarded optional integration keeps this exact compile input rather than replacing its API locally. See components/defaulted-reference/README.md and components/defaulted-dropfix/README.md.

Fzzy Config 0.7.7+fix3+26.3 is byte-identical to official public release YkOumqzV. It stays external, with a public download URL in the artifact lock. No Fzzy source or binary is bundled in the suite or the installation ZIP; its official URL and hashes remain in the installation manifest, and the helper downloads it directly from the publisher.

## Stable integration and historical branches

Mixed-scale MapStitch and Sensible Stackables are included in stable main as separate modules. Current artifact/component versions and exact hashes are in [build verification](build-verification.json); [validation](VALIDATION.md) distinguishes new release checks, implementation-equivalence comparisons, and historical records. The earlier `feat/mapstitch-mixed-scales`, `feat/sensible-stackables`, and `merged` versions keep their original provenance and evidence; their old digests are not relabeled as stable-release test results.

## Bannerpoint source and artwork

The exact official binary is retained unchanged, with its published source JAR SHA-256 `1206ab9856f64dabf56b2ce0b5a0c69bfd0dd538bb209c78700d346c4b0a738d`. GitHub revision `e910c0155617b814bfd8690ce4ab0ce85a74efad` is a pinned source/history reference (September 18, 2026), not an invented private build revision. Its source ZIP SHA-256 is `6967a0f3170d93355e46bd87e5e95047670ec96f8e2c12f40be7be77f921b6ff`. The original standalone Bannerpoint Icons.zip contains the same waypoint-style JSON and four GUI sprites byte-for-byte. Artwork/source notices retain copyright 2026 pajic under MIT.

The compatibility module copies only those five original resources into the generated Polymer pack, plus credits and the complete MIT artwork notice. It does not copy client code or overwrite suite settings translations. Native detection reuses the original advertised `bannerpoint:banner_name` channel, so original clients need no suite handshake and the existing fixed suite v2 protocol is unchanged. Fallback eligibility requires an observed offer whose SHA-1 matches the verified five-resource local Polymer archive, and actual SUCCESSFULLY_LOADED acknowledgement for that main pack; ACCEPTED/DOWNLOADED alone do not enable icons. Name labels remain client code, documented in [Bannerpoint](BANNERPOINT.md).
