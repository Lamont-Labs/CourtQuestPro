---
name: Native accessibility exports
description: Non-obvious Android accessibility export behavior relevant to native navigation verification.
---

Do not equate UIAutomator's `clickable` field with whether the current native
tab can receive a pointer tap. Android can export an active Compose `Role.Tab`
as selected and focusable but non-clickable. A destination's accessible name
can also be exported on a child node rather than its tagged wrapper.

**Why:** Real Android captures exposed both behaviors. Requiring a parent's
content description or a click action on an already-selected tab rejected
working navigation.

**How to apply:** Require the exact accessible name within the intended
destination's own semantic subtree. Keep enabled/visible/touch-size checks.
An active selected/focusable tab can be tested by tapping it and verifying the
actual result (including auto-dismiss), not by skipping that interaction.
Continue rejecting disabled or non-actionable unselected destinations.

Wait for the required observable drawer open/closed state after transitions,
using bounded readiness checks rather than assuming one fixed delay is enough.

**Why:** A failing initial hierarchy read and a later failure capture can show
different UI states without any app defect.

**How to apply:** Retain failure captures and verify the stable semantic state
before selecting a target. A timeout must still fail, not silently continue.
