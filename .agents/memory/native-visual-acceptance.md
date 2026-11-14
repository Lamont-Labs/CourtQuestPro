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

Batch visual critique across normal and enlarged-text captures before pushing the next revision.

**Why:** A visible normal-size redesign can still split single-word labels at 200% text;
navigation target measurements and pixel-difference gates do not detect that defect.

**How to apply:** Review actual enlarged-text screen bodies as well as navigation. Reflow
narrow multi-column controls instead of shrinking or capping the user's chosen font scale.
