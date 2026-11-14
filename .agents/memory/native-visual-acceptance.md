---
name: Native visual acceptance
description: CourtQuest owner feedback and evidence requirements for native visual changes.
---

Native UI work must produce a visible change in the actual Android app, not merely a new APK,
changed version metadata, release infrastructure, recolored tokens, or a web replacement.

**Why:** After installing the pipeline-only v21 update successfully, the owner reported
“There was absolutely no change at all” and explicitly requested native implementation.

**How to apply:** Compare real before/after emulator screenshots against the installed release
using matching saved data, display density and font scale. Review all affected destinations,
above-the-fold hierarchy, tap targets and enlarged text before calling a visual transformation
verified. Keep signing identity, existing player data and approved artwork unchanged. Do not
equate successful build/release checks with owner visual acceptance.
