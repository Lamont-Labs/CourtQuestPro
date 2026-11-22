---
name: Compose lazy-list test scrolling
description: Why Compose 1.7 UI tests need both list placement and descendant scrolling.
---

With Compose UI Test 1.7, place the target's lazy item with the list's `performScrollToNode`, then call the target's `performScrollTo` and assert that it is displayed.

**Why:** The official 1.7.6 implementation returns the first matching ancestor under the list, rather than the deepest matched descendant. A settings panel taller than the viewport can therefore be placed while its requested button remains below the viewport. Conversely, child-only scrolling cannot reliably position an unplaced cached lazy item. Actual emulator screenshots and semantics exposed both cases.

**How to apply:** Test large-text settings and return navigation using both operations, retaining visibility assertions. Do not add sleeps or bypass pointer visibility. Recheck this behavior against the official sources when upgrading Compose; later implementations changed the descendant search.
