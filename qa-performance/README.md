# Allocation regression and release acceptance

The current [1.4.5 acceptance record](evidence/1.4.5/verification.json) identifies
the exact suite JAR and links the executed behavior, allocation, packet, HUD,
connection, build and installation checks. The [release guide](../docs/RELEASE_1_4_5.md)
explains the production changes and performance limits.

The two measured allocation comparisons isolate the old component-patch split
expression and the old uncached atlas lookup expression. The latter runs with
the optimized stack getter on both paths. These figures are allocated bytes for
specific operations, not whole-game memory usage, retained heap, FPS or pause-time
predictions. User Spark recordings establish the observed workload; they are
not controlled before/after code-fix measurements.

The [graphical integration runner](integration/README.md) uses the current wheel,
BuildPrint, Blocktone and BRBR with native and Polymer clients. Its atlas helper
is maintained in [qa-allocation](../qa-allocation/README.md). Other fixtures stay
in their existing directories and receive the exact candidate path explicitly:

```sh
python3 qa-stackables/run.py --jar build/libs/vanilla-plusplus-quality-of-life-suite-1.4.5+26.3.jar --label allocation-final
python3 qa-packet-templates/run.py --jar build/libs/vanilla-plusplus-quality-of-life-suite-1.4.5+26.3.jar --label allocation-default
python3 qa-packet-templates/run.py --jar build/libs/vanilla-plusplus-quality-of-life-suite-1.4.5+26.3.jar --label allocation-uncapped --uncapped
python3 qa-hud/run.py --suite build/libs/vanilla-plusplus-quality-of-life-suite-1.4.5+26.3.jar --label allocation-hud
python3 qa-hud/run.py --suite build/libs/vanilla-plusplus-quality-of-life-suite-1.4.5+26.3.jar --label allocation-hud-polymer --client-polymer
python3 qa/light.py --jar build/libs/vanilla-plusplus-quality-of-life-suite-1.4.5+26.3.jar --label allocation-connections --stackables-actions --stackables-uncapped --all-profiles
```

Use unique labels and reserve the shared graphical display before running client
fixtures. Runtime directories, worlds and QA binaries remain local. Committed
receipts retain hashes and scope. Initial receipts under
`evidence/1.4.5-initial-before-live-limit-invalidation` and
`integration/evidence/initial-before-live-limit-invalidation` identify the
superseded first candidate, which lacked live stack-limit cache invalidation. The later `evidence/1.4.5-live-limit-reflection` candidate used reflective invocation of the optional API; related integration diagnostic/failure folders preserve its exact scope.
They are not release acceptance. The final helper adds atlas and nested-map
maximum changes from two to one and back to two without replacing pouch contents.
