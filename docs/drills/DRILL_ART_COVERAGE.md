# CourtQuest Pro Drill Illustration Coverage

Verified built-in drills: **18/18**.
Every plate is bundled offline as optimized WebP under `app/src/main/assets/drill_art/`.
The manifest is generated from the current Kotlin drill catalog and records exact source instructions, asset dimensions, byte size, and SHA-256.
The upper panel provides the drill-specific movement sequence. A comparison panel is included only when it can be shown safely and accurately; exclusions are explained per drill.

Custom drills have no inferred drill-specific illustration. They retain their saved instructions and use generic court geometry. Missing built-in artwork displays an explicit offline fallback.

| ID | Built-in drill | Asset | Dimensions / size | Sequence | Form comparison | Verification |
| --- | --- | --- | --- | --- | --- | --- |
| `s1` | Five-Spot Form Shooting | `drill_art/s1.webp` | 1024 × 1024 (131 KiB) | 3 phases | Yes — GOOD REP / COMMON MISS | PASS |
| `s2` | Catch, Set, Shoot | `drill_art/s2.webp` | 1024 × 1024 (106 KiB) | 3 phases | Yes — GOOD REP / COMMON MISS | PASS |
| `s3` | Move Into Midrange | `drill_art/s3.webp` | 1024 × 1024 (97 KiB) | 3 phases | Yes — GOOD REP / COMMON MISS | PASS |
| `b1` | Stationary Crossover Control | `drill_art/b1.webp` | 1024 × 1024 (127 KiB) | 3 phases | Yes — GOOD REP / COMMON MISS | PASS |
| `b2` | Change-of-Pace Lane | `drill_art/b2.webp` | 1024 × 1024 (111 KiB) | 3 phases | Yes — GOOD REP / COMMON MISS | PASS |
| `b3` | Combo Into Drive | `drill_art/b3.webp` | 1024 × 1024 (139 KiB) | 3 phases | Yes — GOOD REP / COMMON MISS | PASS |
| `f1` | Right and Left Layups | `drill_art/f1.webp` | 1024 × 1024 (112 KiB) | 3 phases | Not shown — The plate contrasts right- and left-hand layup patterns; it does not show a reliable common-mistake comparison. | PASS |
| `f2` | Change Direction Finish | `drill_art/f2.webp` | 1024 × 1024 (129 KiB) | 3 phases | Yes — GOOD REP / COMMON MISS | PASS |
| `f3` | Reverse Finish Footwork | `drill_art/f3.webp` | 1024 × 1024 (97 KiB) | 3 phases | Yes — GOOD REP / COMMON MISS | PASS |
| `d1` | Defensive Slide Intervals | `drill_art/d1.webp` | 1024 × 1024 (94 KiB) | 3 phases | Yes — GOOD REP / COMMON MISS | PASS |
| `d2` | Closeout and Contest | `drill_art/d2.webp` | 1024 × 1024 (97 KiB) | 3 phases | Not shown — A wrong closeout image could normalize contact or entering a shooter's landing space; the plate shows safe spacing instead. | PASS |
| `d3` | Mirror Footwork | `drill_art/d3.webp` | 1024 × 1024 (128 KiB) | 3 phases | Yes — GOOD REP / COMMON MISS | PASS |
| `r1` | Box-Out Footwork | `drill_art/r1.webp` | 1024 × 570 (63 KiB) | 3 phases | Not shown — The generated lower inset depicted forearm contact and was cropped before bundling; the safe sequence remains. | PASS |
| `r2` | Secure and Outlet | `drill_art/r2.webp` | 1024 × 1024 (126 KiB) | 3 phases | Yes — GOOD REP / COMMON MISS | PASS |
| `r3` | Solo Shot and Rebound | `drill_art/r3.webp` | 1024 × 1024 (110 KiB) | 3 phases | Not shown — A negative example could imply entering the shooter's landing area; the plate shows safe spacing only. | PASS |
| `p1` | Target Chest and Bounce Passes | `drill_art/p1.webp` | 1024 × 1024 (102 KiB) | 3 phases | Not shown — The plate compares two valid pass types, not a common mistake. | PASS |
| `p2` | Pass and Cut | `drill_art/p2.webp` | 1024 × 1024 (131 KiB) | 3 phases | Yes — GOOD REP / COMMON MISS | PASS |
| `p3` | Rebound Outlet Decisions | `drill_art/p3.webp` | 1024 × 1024 (119 KiB) | 3 phases | Yes — GOOD REP / COMMON MISS | PASS |

All assets passed RIFF/WebP integrity, dimension, SHA-256, filename mapping, and 512 KiB per-image budget checks. Android decode and display are verified separately by the native library workflow.
