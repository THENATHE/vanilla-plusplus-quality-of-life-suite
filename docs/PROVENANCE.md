# Provenance and compatibility tracks

This suite targets Minecraft 26.3, Fabric Loader 0.19.5 and Java 25. It combines official developer releases, existing local ports/additions and new suite-specific compatibility code. It does not relabel the entire distribution as an official developer release.

## Included inputs

| Module/library | Artifact version | SHA-256 | Origin |
| --- | --- | --- | --- |
| amethyst_curse_cleanser | 1.0.1+26.3 | `5ec26e2a0010dfdee82aa5cb27664314150a4daa36deadc10f0c659f399507b6` | Existing local authored 26.3 mod; source snapshot components/amethyst-curse-cleanser/upstream |
| chalk | 3.2.1+26.3 | `886b0b10cc12df65d47f446c2bd54c0c670f9a54fa09a1fe95c9ec46df565f49` | Existing ChatGPT Fabric 26.3 port of DaFuqs Chalk; source snapshot components/chalk/upstream; historical git dfb461ef9eb6f7bf5188570594c3bd21171c2391 |
| chalk-colorful-addon | 2.1.1-port.1+26.3 | `69a2443250abd95100636194a145b5479d240151cd19bf4186766117d0710b34` | Existing metadata-only ChatGPT 26.3 port; original uploaded addon retained; source components/chalk-colorful/upstream |
| codecui | 26.3-1.4.3 | `4d07c219bd85b58283a0317d6f43cca838adf9cd16be321316cd32f2fb1be898` | Existing local Fabric 26.3 CodecUI port; source remains sibling Minecraft/codecui-26.3/port26 |
| defaulted | 1.3.8+26.3.dropfix.1 | `e339d6f0eb471a4ac41185fb9dbe0cfaa78a290c6ceedf92a49ba9110f732c61` | Existing local dropfix modification, exact original hash 0f6efc6423902b83da78ce85a119c69536517b1a9af52ed7046858859ff582a0; source components/defaulted-dropfix |
| fabric-language-kotlin | 1.14.1+kotlin.2.4.20 | `620c2709be2a262b837cf976f6f85f6eda80a56af2ba4242e0bdc83a60fee7a5` | Pinned existing runtime library, unchanged archive |
| fzzy_config | 0.7.7+fix2+26.3 | `138e377563a8ca536a9af00d8eeefb7fe160903392df8e4ca7cd4b5da6f0bb5f` | Pinned existing 26.3 fix2 runtime library; original source/ownership retained, no suite binary modifications |
| mapstitch | 1.1.6 | `e1b768bbd1ae06f83305eeba3ab19bfe4eb99bf21de12d57364d04ce9a1cde81` | Official developer Fabric 26.3 release; components/mapstitch/inputs.lock.json |
| misctweaks | 1.4.4 | `97fe263764cb6b93822e13fe9423cf91c3a6a0169aeeb8734b65136cc9ea9647` | Official developer Fabric 26.3 release; components/misctweaks/inputs.lock.json |
| mixson | 2.2.1 | `e4d38f31be95a1f03f8bef290c079f4bb5573d8f4e15808e8c1fc8ba3e5913a0` | Pinned existing runtime library, unchanged archive |
| simple_death_improvements | 1.6.0 | `67070a8add053ee1a2cad1c310d36cdac0cf53c7950873e20d43d7da8a0ffcc2` | Official developer Fabric 26.3 release; components/simple-death-improvements/inputs.lock.json |
| simple_smithing_overhaul | 2.9.14 | `fb6cbf8c13938d68fb19171dac386b5567d71625bce727b22d3380f6b7880126` | Existing local 26.3 build, builder/source revision unverified; two changed classes match published upstream 26.3 branches; NOT verified official developer release; official 26.2 baseline preserved components/sso/upstream |
| tiered_backpacks | 1.0.20 | `b129b9b61ec1da62842dc2dfa5b166a6df8e91c796666dcee70f7b8acf1ecc33` | Official developer Fabric 26.3 release 1.0.20; components/tiered-backpacks/inputs.lock.json |
| toolpouch | 1.1.10 | `a5bb4f084e7d929f3a89cc43317af63bd45abe3e3f4fed8b45f99b2da846e903` | Official developer Fabric 26.3 release; components/toolpouch/inputs.lock.json |
| compile-only Kotlin stdlib | 2.4.20 | `078594f80e214438a35454e3f16f890a68dba6a607aa2c83014065344d13308a` | Compile-only unchanged META-INF/jars/kotlin-stdlib-2.4.20.jar extracted from pinned Fabric Language Kotlin archive |

## Modified suite components

| Component | Suite version | Baseline / record |
| --- | --- | --- |
| Combined compatibility | 1.0.5-suite.1+26.3 | Original standalone Multi-Shim 1.0.5+26.3; components/combined-compat/ORIGIN.json |
| Chalk compatibility | 1.1.0-suite.1+26.3 | Original standalone Chalk shim 1.1.0+26.3; components/chalk-compat/ORIGIN.json |
| Shared Region Maps | 1.0.4-combo.1+mc26.3 | Original local 1.0.3, commit 338270a70c14be19ba79b535b40fac980e414470; components/shared-region-maps/upstream-manifest.json |
| Tool Pouch atlas/Elytra addon | 1.0.5-suite.1+26.3 | Original local 1.0.4; components/toolpouch-atlas-elytra/upstream-manifest.json |
| Suite root / settings | 1.0.0+26.3 | Newly authored source in src/ |

The production archive retains these original module IDs and embeds each module separately. Original standalone repositories and versioned releases remain available; the suite variants do not overwrite them.

## SSO provenance caveat and paused track

The local `simple_smithing_overhaul-fabric-2.9.14+26.3.jar` had previously been called a developer release in local records. Fresh verification found official Modrinth 2.9.14 published for 26.2 only. Against that exact official binary, 81 of 83 local classes are byte-identical. The remaining TriggerInstance and VillagerTradeMixin changes correspond to 26.3 conditional branches in the published source. This supports an upstream-source build for 26.3, but its original builder/download/source commit cannot be proven from the retained archive. It is classified here as an existing local build with unverified build provenance, never as an official 26.3 developer release. Detailed archive/bytecode evidence: components/sso/upstream/metadata/local26.3-analysis.json.

The official 26.2 binary/source lock stays separate in components/sso/inputs.lock.json. The actual suite input is components/sso/suite-input.lock.json and root locks/artifacts.json. The distinct ChatGPT SSO `2.9.14-port.1+26.3` project, its companion shim track and historical tests remain paused; no source changes or build of that project are part of this suite. Any combo-only use/testing of the existing local 26.3 input must follow the user's scope clarification.

## Chalk track separation

The suite uses the exact local Chalk 3.2.1+26.3 port and colorful-addon 2.1.1-port.1+26.3 input. Its new compatibility variants are compiled and tested for those inputs only. Historical original Chalk 3.2.0+26.2, original colorful-addon 2.1.1+1.19.3 and standalone shim 1.1.0+26.2 remain in their existing source/release track. This suite does not modify or newly verify that 26.2 installation. Other Pajic modules have no separately maintained ChatGPT port in this suite; no unsolicited port is invented.

## Validation authority

Only a run against the final suite artifact and its exact dependency hashes verifies this combination. Historical standalone tests, unchanged binary identity and source/API compile checks are recorded as context and do not establish new native/fallback or graphical correctness. See docs/VALIDATION.md for actual completed cases, pending manual steps and the final tested hash.
