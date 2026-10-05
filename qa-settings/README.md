# Native settings validation

Current stable **1.1** uses this fixture on `main`. [VALIDATION.md](../docs/VALIDATION.md) records the exact release checked; older candidates and their former labels below remain historical.

This development fixture opens the suite's actual Fzzy Config GUI in a disposable graphical client. It validates the twelve functional original/grouped configuration identities in the stable suite (ten without Sensible Stackables), their effective Settings titles, absence of the former fake overview, and the rebindable suite hotkey, constructs every native configuration screen, renders screenshots after the initial loading overlay has closed, and presses native checkbox widgets rather than substituting a settings serializer.

```sh
python3 qa-settings/run.py --label standalone --settings-only
python3 qa-settings/run.py --label branded-gui --settings-only --visual-only
python3 qa-settings/run.py --label remote-admin --port 25983
python3 qa-settings/run.py --label remote-guest --guest --port 25983
python3 qa-settings/run.py --label lifecycle-admin-to-guest --reconnect --port 25983 --suite <candidate.jar>
python3 qa-settings/run.py --label lifecycle-guest-to-admin --guest --reconnect --port 25983 --suite <candidate.jar>
python3 qa-settings/run.py --label lifecycle-focused --lifecycle-only --port 25983 --suite <candidate.jar>
```

Use `--suite <exact-jar>` for a frozen candidate; default is the current build output. Each run records suite/dependency hashes, launch commands, logs, observations and screenshots under `qa-settings/runs/<label>/`. The fixture never edits the production JAR or a user's configuration directory. It creates isolated localhost test worlds and preserves all native libraries. `-Djava.awt.headless=true` suppresses Fabric error dialogs while Minecraft's LWJGL rendering remains enabled.

The standalone check toggles Chalk's native UI bridge false/true, calls original AutoConfig save/load, reopens the unified screen, and verifies the original Chalk file remains authoritative. It also toggles Tool Pouch's native `showUIHints` checkbox false/true and checks original `config/toolpouch/client_config.toml` values plus a native Fzzy file re-read. Chalk additionally verifies the particle runtime object equals the original AutoConfig holder after reload.

The operator run opens every original config on a dedicated server, verifies permission level4, presses the native MiscTweaks server-setting checkbox, and checks the dedicated server received and saved the value under the original configuration identity. The guest run presses the native locked `Can't Edit` explanation button and verifies both the configuration and server value remain unchanged. This button is active so it can show permission help; its active flag does not imply edit access. Screen/data observers use reflection only in the disposable fixture.

Connected runs additionally queue a forwarded proposal through Fzzy's original receiver, close/reopen the suite, and check the same proposal remains. They feed a native config update into `receiveUpdate`, confirm its namespace invalidation, reopen the original Tool Pouch scope, and verify rebuilt widgets retain the proposal and receive another through restored routing. `--reconnect` launches a second localhost server on `--port + 1` with opposite operator permissions. The graphical client actually disconnects and connects to that server; the fixture checks removal of old manager aliases, absence of old proposals, fresh proposal routing, changed native permission widgets, and the second server's saved setting. Proposal injection uses native client receive methods; it does not claim a two-player network forwarding test.

`--lifecycle-only` retains config identity validation and runs opening/proposal reopen/update invalidation checks without repeating the full per-category persistence matrix. It can also be combined with `--reconnect`.

Earlier `settings-standalone-01`/`02` runs had a fixture packaging mistake: `suite_settings_qa_client` accidentally declared the unrelated `suite-hud-qa.mixins.json` HUD observer mixins without including those observer classes. That declaration was removed from the settings fixture. Those runs are excluded from suite acceptance. Both client/server QA fixture mods are development-only and are excluded from the release bundle. `settings-standalone-03` passed logic and persistence checks, but its first screenshot preceded the loading-overlay close and is excluded as root-screen visual evidence. Later connected runs wait for the overlay to close and a rendered frame before capturing screenshots.

Runtime checks do not imply a complete gameplay matrix or every upstream field combination was exercised. They specifically verify combined presentation, native object ownership, representative persistence, server routing and permission behavior.

## Historical settings candidates

The frozen final settings candidate `3bc3f314b70d78010334d62501ac1e7dfdcb2507ba72e36a24e1f61e5bc7f621` passed `settings-final-admin` and `settings-final-guest-02`. `settings-final-guest` had an incorrect fixture assertion that interpreted the locked explanation button as editable; the corrected rerun pressed it and proved no setting change. `settings-remote-guest-01` could not connect because that older fixture left the dedicated-server whitelist enabled; current isolated servers explicitly disable it. Neither excluded run indicates a production failure.

Final bundle `0312a5d7ce8cf1e597e21a9d111f8efaa47fa7c01c6b18effb48977cd9bbc957` passed the short branded GUI check in `settings-branded-final`. Recursive comparison found all 657 root/nested Java classes unchanged from the native runtime-tested candidate. Sanitized observations and actual rendered overview/sidebar images are committed in [evidence/](evidence/), without launch paths or machine/account metadata. The original Tiered Backpacks label stays **Mod Configuration**; its native configuration description identifies the module.

The 1.0.1 lifecycle regressions are preserved separately in [evidence/lifecycle-1.0.1.json](evidence/lifecycle-1.0.1.json), including the exact runtime candidate and the distinction between runtime checks and final packaging metadata verification.
