# Toolbox beta 2 evidence

Final iron/netherite candidate: `7e734d89c51a35e4203a39ab978f16d9db9ec2bb58a9949ef05dd6b204fe787c`.

- `artifact.json` and `bundle.json`: fixed 16×16 sprites, binary alpha, palette limits, archive and original-input checks.
- `netherite-server-clientsort/`: 187 assertions each in native and Polymer profiles, including actual recipe selection and normal/shift smithing with a vanilla template-consumption control.
- `wheel/`: exact Hotkey Wheel 1.0.6 integration in native and Polymer profiles.
- `connections/`: final four-profile connectivity and native/fallback item checks.
- `class-comparison.json`: identifies changed classes against beta 1; menu, recipe-book screens, BRBR hooks, networking and ClientSort remain byte-identical. Historical beta1 BRBR/singleplayer acceptance retains its original scope and hash.

`server-clientsort/`, `iron-diamond-superseded-artifact.json` and `class-comparison-diamond-variant.json` preserve the earlier unshipped iron/diamond proposal, tested at hash `262d4d2a7811f070a30d8d85c7fac202412f4feaf07e2c8630781da135655783`. They do not establish final netherite behavior.
