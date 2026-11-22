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
An unchanged introductory summary can also fill the entire enlarged-text viewport,
completely hiding a new chart that is obvious at normal text size.

**How to apply:** Review actual enlarged-text screen bodies as well as navigation. Reflow
narrow multi-column controls instead of shrinking or capping the user's chosen font scale.
Check the initial viewport before scrolling; reachable new content is not necessarily
an immediately visible redesign.

Capture all primary destinations before deep interaction checks, while retaining failure evidence.

**Why:** A drill-discovery assertion stopped several candidate captures before the other
primary screens were recorded, preventing a coordinated visual review despite successful builds.
Declarative initial values alone also did not describe the visible browse state after navigation.

**How to apply:** Keep matched release/candidate fixtures and capture the complete screen set
first, then exercise transitions and persistence. Check actual post-navigation expansion/filter
state before assuming a drill is reachable; distinguish a broken UI flow from an obsolete selector.

Pixel differences and valid image manifests do not establish anatomical fit.

**Why:** Valid layered shoe assets initially left original toes visible and used
the full athlete foot angle as an extra rotation, despite the source shoe already
having its own angle. Hash/integrity checks could not reveal the disconnected fit.

**How to apply:** Inspect original ankle-to-toe alignment and actual alpha extent,
including anti-aliased edges, before fitting replacement wearables. Review close-up
Android renders as well as full-body composition; prove old-layer clearance and
viewport coverage separately from whether each option changes pixels.
