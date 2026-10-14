> Measurement correction: earlier caption-shadow overlap statements used an approximate `font.width`/`font.lineHeight` box. Exact native prepared-text bounds later measured the caption ending at Y149 and the information background starting at Y149, which touch without overlap. The old shadow assertion is a fixture false positive, not a verified physical collision. Original receipts, source and bytecode remain intact. See `../2026-10-13-e466-native-bounds-fixture-correction/`. The final30-scene fixture separately checks actual Y148 overlap and Y149 touching.

# Historical HUD evidence

Recorded QA observer JARs and frozen development inputs remain local execution artifacts. Their hashes identify what ran. Public source/documentation may include the fixture source, sanitized logs/configuration and recorded hashes; it does not distribute QA JARs.
