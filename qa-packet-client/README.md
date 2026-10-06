# Physical nested-container packet regression

Run `python3 qa-packet-client/run.py --jar <suite.jar> --label <new-label> --stackables-actions --stackables-uncapped`.

This isolated copy of the bounded connection fixture additionally gives each player a netherite tool pouch containing a shulker box holding three potions. The physical client must receive both container levels and the three-potion count/limit. Matching suite clients additionally check that an unpatched potion template reads its effective limit of three and creates a nonempty stack. The ordinary native/Fabric fallback negotiation, resource-pack, 2,048-item inventory packet moves, and native atlas controls remain exercised.

Results, frozen input artifacts, exact runtime commands, disposable worlds, and server/client console logs are recorded under `runs/<label>/`. This is targeted packet reception coverage, not an exhaustive inventory gameplay test. No developer input JARs or production source are modified by the fixture. The source copies are deliberately isolated from the historical `qa/light.py` fixture.
