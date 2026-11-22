---
name: Compose asynchronous loading and lint
description: The current Android lint tooling can reject valid produceState assignments.
---

The project's current Compose lint tooling reported `ProduceStateDoesNotAssignValue`
for asynchronous artwork loaders even though their producer lambdas assigned `value`;
Kotlin compilation and unit tests passed.

**Why:** Treating compilation as sufficient would have left the required lint gate
failing and prevented native emulator verification.

**How to apply:** Prefer the project's existing keyed `remember` state plus
`LaunchedEffect` loading pattern when this tooling limitation occurs. Keep disk reads
and bitmap decoding off the main thread, and keep lint enabled rather than adding a
baseline or suppressing the check.
