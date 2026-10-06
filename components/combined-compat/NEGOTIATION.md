# Shared suite negotiation

`src/main/java/com/thenathe/suite/network/SuiteCapabilities.java` is compiled into this component and exposed to the root suite and Chalk component. The root common entrypoint calls `initialize()`; the root client entrypoint calls `initializeClient()`. Both are idempotent. The component common entrypoint independently calls `initialize()` to remove any dependency on entrypoint ordering. The root must exclude network sources from its own compilation to avoid duplicate classes.

The stable coordinator is `1.1.1+26.3`, with protocol v2 payloads and 11 ordered module fingerprints.

## Connection sequence

1. The only early registry scheduling mixin is `combinedshim.mixin.RegistrySyncSchedulingMixin`. It clears all connection-local decisions and invokes registered cleanup hooks.
2. When supported, Fabric's existing common protocol queries PLAY channels once. This preserves recognition of untouched Tool Pouch clients and recognizes the new original Stackables receiver. SSO also needs the block-state confirmation receiver before native selection. MapStitch channel-only recognition is disabled when the mixed-scale addon is present, as it is in stable main.
3. A suite-aware client receives one nonce offer and replies with an ordered fingerprint for each module. There is no complete client mod list and no UUID override file or command. Exact module, relevant addon, suite compatibility versions, registry entries, and block-state definitions are hashed. Missing modules produce empty fingerprints.
4. The server selects exact matches and sends one authoritative decision. Explicit mismatches stay on fallback rather than being overridden by original payload channels. Clients lacking the suite retain native Tool Pouch detection and can advertise `sensible_stackables:stack_sizes` for the new original release. Native SSO needs both `simple_smithing_overhaul:repairables` and the suite’s configuration `chalk_polymer_compat:state_request_v1` receiver; original SSO alone remains on the safe fallback because it cannot supply client block-state IDs. Mixed-scale MapStitch requires the matching addon-aware suite fingerprint; backpacks/Chalk use Polymer fallback when no reliable matching capability is available. Partial native repair/stack tables exclude registry entries that client does not have.
5. Fabric registry synchronization begins after selection. Each Polymer module restores native registry entries only for the selected modules. The combined component owns final item/component/serializer dense wire mappings; The Chalk compatibility component owns the shared block/state map for native Chalk and SSO blocks.
6. If either Chalk or SSO is native, the shared block transport subsequently requests actual client state IDs/bit width after Fabric's remap acknowledgement. This is registry confirmation, not a second capability negotiation. Invalid/incomplete state proofs disconnect with the existing clear diagnostic.

All decisions are scoped to Fabric PacketContext and reset on reconfiguration. Nonces reject stale or duplicate replies. This is capability negotiation, not authentication or protection against modified dishonest clients. Unsupported custom payload sends remain guarded by channel support.

## Optional Polymer

All handshake codecs and the scheduling mixin work without Polymer. Plain SSO loot/anvil/mending/enchanting corrections and Tool Pouch shulker binding fixes remain active with or without Polymer. Every Polymer-specific mixin and initialization is conditional on the corresponding installed Polymer modules. Original game behavior and its normal Fabric synchronization remain active when Polymer is absent. Vanilla access requires server Polymer and the generated server resource pack; optional Polymer does not promise vanilla access when absent. Client Polymer is not required by a matching suite client.

## Build and maintenance

Both compatibility components preserve original Java packages/IDs so resource identifiers and shared mapping integration remain stable. The root Gradle build adds the root network directory to combined-compat's sources. Chalk compiles against combined-compat. `ORIGIN.json` retains per-file hashes of the pre-suite baseline; historical original projects remain outside this directory unchanged.

When a module changes version or registry definitions, its handshake fingerprint changes automatically. If adding/removing suite components, update the fixed MODULES order and dependencies() together and advance the handshake payload version if protocol layout changes between published suite versions. Never add a module to this list as a substitute for implementing its Polymer fallback. New registered items/components/serializers may require new Polymer registration or safe translation. Review API/mixin targets on each Minecraft/Fabric update.

Light validation should compile, inspect metadata/nested modules, and run a short server/client smoke matrix. The complete gameplay and integrated-server matrix belongs in the manual testing guide; standalone historical acceptance results do not verify these changed components.

Native SSO broken-anvil overlays preserve the actual block and state; unsupported clients retain the vanilla damaged-anvil representation. The shared block transport restores only selected modules, so a mismatched SSO client can retain native Chalk and a mismatched Chalk client can retain native SSO without exposing the unsupported blocks.


## Current upstream synchronization

Suite 1.1.5 uses SSO 2.10.0 and Sensible Stackables 3.1.1, which synchronize directly instead of depending on Defaulted. The `simple_smithing_overhaul:repairables` and `sensible_stackables:stack_sizes` send paths are native-guarded. Their clients must advertise support and pass the applicable module decision when Polymer translation is active. Stackables' fingerprint no longer includes Defaulted; fallback stack metadata comes from effective override getters with full counts and a maximum prediction limit of 99. The historical Defaulted integration remains guarded and compile-only. These changes preserve the fixed v2 protocol layout and original module IDs.
