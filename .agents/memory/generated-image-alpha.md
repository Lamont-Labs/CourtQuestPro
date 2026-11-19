---
name: Generated image transparency
description: Verify real alpha again after image edits before bundling composited Android artwork.
---

Image editing can flatten a transparent generated cutout onto a visible
checkerboard even when its PNG still reports RGBA. Do not treat the filename,
RGBA mode, preview checkerboard, or an edit request to preserve transparency
as proof of genuine alpha.

**Why:** A shoe-logo cleanup returned an entirely opaque PNG while the
original generated cutout had valid transparency. Using it would have shipped
a checkerboard rectangle inside the player card.

**How to apply:** Inspect alpha extrema and the subject bounds on every edited
cutout. Retain the genuine-alpha source and make a constrained compositing
repair, or remove the new background again, before producing runtime layers.
