---
version: alpha
name: CourtQuest Pro
description: A native Android basketball training and scorekeeping design system.
colors:
  primary: "#FF783D"
  accent: "#FFA35C"
  on-primary: "#211713"
  background: "#121315"
  surface: "#252127"
  surface-warm: "#4B281D"
  court-shadow: "#201A1B"
  text-primary: "#F7F3EF"
  text-secondary: "#B7ACA6"
omitted:
  - section: typography
    reason: Android typography uses scalable sp values documented in the Typography section.
  - section: spacing
    reason: Android layouts use dp values documented in the Layout section.
  - section: rounded
    reason: Android shapes use dp values documented in the Shapes section.
  - section: components
    reason: The Components section maps roles to native Compose code and state.
---

# CourtQuest Pro design standard

## Overview

CourtQuest Pro is a basketball performance studio for a player on a phone. It opens onto a clear training decision, supports a focused drill or one-on-one game, then makes the recorded result easy to find. The established dark, warm, orange identity is the starting point. Use basketball court geometry, confident typography, and carefully prioritized data to give CourtQuest its own identity. Use calmer, more direct layouts while the player is recording attempts or keeping score.

The current design direction follows the owner's build 2 feedback. Onboarding improved, with substantial visual and workflow work remaining across the app. Build 2 used app source `21ff9222edd69f5c65c1999082b751c104d63194`, published in release commit `c3e57e93708edcb6e6fa61c43cddc79dae7314f3`. Confirm the current branch, APK version code, and owner feedback at the start of each pass. This file is the active standard. The earlier [design contract](docs/COURTQUEST_PREMIUM_UI_DESIGN_CONTRACT_v1.md) records the initial baseline.

## Owner-selected brand: Signature

Selected by the owner on November 11, 2026 from five icon and wordmark concepts. The owner chose **05 / SIGNATURE** and supplied the same reference image with the feedback, “This is my favorite!!!” This is the selected branding direction for CourtQuest Pro.

Approved visual reference: **CourtQuest Pro Signature Mark.png**. The owner's confirmation attachment is **1000048412.png**. Preserve the reference artwork's character when preparing production assets.

| Element | Selected treatment |
| --- | --- |
| App name | CourtQuest Pro |
| Launcher identity | Vivid orange field with a warm ivory brushstroke CQ monogram |
| Basketball detail | Dark basketball seams at the lower-right end of the sweeping brushstroke |
| CourtQuest wordmark | Bold, connected, forward-moving brush-script lettering |
| PRO | Orange, widely spaced uppercase sans-serif lettering beneath CourtQuest |
| Supporting palette | Existing orange, charcoal, and warm ivory brand family |

Use the selected CQ mark and wordmark as the reference for launcher artwork, launch branding, onboarding, and appropriate app headers. Preserve the distinctive letter shapes and sweeping composition; a generic script font is not an equivalent replacement. Keep functional labels, instructions, scores, timers, and statistics in readable scalable native type.

Production preparation must separate the CQ icon and wordmark from the presentation board, omit the “05 / SIGNATURE” concept label, and prepare suitable light and dark treatments. Check launcher masking and small-size recognition, then verify the actual Android integration.

**Status:** Owner-selected visual concept. Separate high-resolution launcher artwork and light/dark wordmarks were prepared on November 11, 2026. Android integration and a verified APK containing this identity remain pending. This selection does not establish runtime or launcher acceptance.

### Signature artwork preparation checkpoint

[CourtQuest_Signature_Brand_Pack_v1.zip](https://chatgpt.com/library?file_id=libfile_917d539dbe6c8191b49f05e6635322a1&account_id=personal) contains the original approved reference, a 1254 × 1254 launcher master, two 2172 × 724 wordmark exports, usage notes, and per-file SHA-256 records.

The selected deliverables are **opaque PNGs**. Transparent cream exports showed background-removal residue and were rejected. Do not use the rejected alpha drafts as app resources. The opaque light/dark backgrounds also have slight raster variation; check surface edges before integrating, or prepare clean alpha/vector artwork first.

Generated renditions preserve the selected Signature direction but can vary slightly in letter contours; the included owner-approved reference remains the fidelity anchor. Native work still needs adaptive-icon framing and mask checks, launch branding, onboarding/header integration, and actual Android review. PNG decoding, unchanged-copy hashes, and ZIP CRC integrity were verified. No application source or APK was changed in this artwork-only pass.

## Colors

Keep the palette in sync with [`CourtQuestDesign.kt`](app/src/main/java/com/courtquestpro/CourtQuestDesign.kt). Use near-black for canvas, muted charcoal for utility surfaces, and warm brown selectively for editorial depth. Use vivid orange for the primary action or important active state, with dark ink on filled orange controls. Use soft orange for emphasis and cream for primary text. Give secondary copy adequate contrast against its actual background. Label status with text, iconography, or semantics in addition to color.

## Typography

Use Android's scalable text and native font stack until a typeface has been reviewed at phone sizes and its license is confirmed. Use a distinct display title, a clear screen or section heading, readable body copy, and a compact label style. Apply all-caps to short supporting labels. Favor sentence case for guidance and actions that need to scan quickly. Give scores, timers, and counts stable alignment as digits change. Keep body guidance readable at large font settings and allow content to wrap without moving the primary action out of reach.

## Layout

Use the existing 4, 8, 12, 16, 24, 32, and 40 dp spacing scale. Establish an explicit order before adding surfaces. The primary action and current state come first, with deeper history and editing controls farther down or on their owning screen. Check that Court's Start Training or Resume Session action is visible in the first viewport at 360 dp and default text size. At larger text sizes, keep the next action and all controls reachable with normal scrolling or a stable footer. Respect system bars, the keyboard, and safe areas. Let lists and chip groups reflow instead of shrinking labels.

| Screen | First job | Visual and interaction bar |
| --- | --- | --- |
| Onboarding | Set up a player and a practical starting plan | Distinct purpose per step, clear progress, useful defaults, reachable Continue and Previous, reviewable plan, saved choices after interruption |
| Court | Start or resume today's training | One primary action in the first viewport, visible session state, quick access to the week and one-on-one, supporting XP and streak secondary to the next action |
| Drill and result | Understand the drill, record attempts, and save the result | Readable instruction order, stable timer and counters, clear success and miss controls, one saved result, visible path back to the workout |
| One-on-one | Record the score and correct it quickly | Large scores, clear player and opponent sides, reliable Undo, direct recap of the recorded game |
| Progress | See what was recorded and what it shows | Separate activity from skill measurements, compare the same drill, show sample size, use honest empty states and history |
| Setup | Change player identity and training preferences | Plain sections, current values, clear confirmation, reachable controls with the keyboard open |

## Elevation & Depth

Use one strong editorial surface where it helps establish the screen. Let spacing, typography, dividers, and intentional grouping carry the rest. Use existing court linework as a quiet brand cue in the welcome or Court hero. Keep the active drill and scorekeeper visually stable while controls change state. Use motion to explain a transition, confirm a result, or respond to a tap. Keep entry and exit transitions short, support the system animation setting, and preserve immediate feedback on scoring and recording actions.

## Shapes

Use 12 dp control, 16 dp standard surface, and 24 dp hero corner radii as starting tokens. Use pill shapes for compact status labels or true selection chips. Keep the selected state readable at enlarged text sizes. Give actionable controls at least a 48 by 48 dp target. Check text contrast and accessibility semantics against [Android accessibility guidance](https://developer.android.com/guide/topics/ui/accessibility/apps).

## Components

- `CourtQuestDesign.kt` owns shared native color and spacing tokens.
- `AppHeader.kt` and `CourtOnboardingWelcome.kt` provide existing editorial identity and court artwork.
- `CourtQuestPrimaryButton.kt` and `CourtQuestPremiumSurface.kt` provide shared action and surface primitives. Compose Material 3 supplies semantic, focusable interaction building blocks.
- `MainActivity.kt` currently hosts Court, drill, progress, onboarding, setup, and scorekeeper screens. Preserve normal navigation, scoring, saved state, history, and training rules through each visual pass. Keep names dynamic and preserve the existing generic labels for blank names.

## Design workflow

1. **Ground the pass.** Read this file and the current Compose entry point. Identify the journey, state, primary action, exact APK or commit, and what the owner said. Capture the current Android screen and the states the change touches. Use the installed Product Design tools to structure flow review and findings when the task calls for an audit. Native screenshots come from a real device or Android Emulator with `adb`.
2. **Choose the visual target.** Load UI/UX Pro Max and run its relevant design-system and native-stack searches as documented below. Review the matched category and actual output, then use Taste for additional art direction. Use the Google DESIGN.md format for shared tokens and Figma to compose or review editable screen directions when a material screen redesign benefits from it. Use ImageGen or Product Design ideation for a visual comparison or original brand imagery when useful. Treat generated mockups as art direction and label them as such. Use Dimensional Drawing's approach for exact component measurements when handoff needs a spec.
3. **Build the native slice.** Use the existing Compose and Material 3 stack. Consult official Android samples and Context7 or Android docs for API and version details. Adapt motion guidance from Emil Kowalski's references to Compose, then test its effect on the actual Android interaction. Browser components can inform visual or interaction ideas. Check source licenses before importing code, fonts, or assets.
4. **Review the rendered result.** Apply an Impeccable-based critique to actual before-and-after captures at matching settings. Use Hallmark as an optional second opinion on whether the layout defaults are too generic. Review composition, first action, visual identity, text hierarchy, tap targets, accessibility semantics, compact width, large text, and meaningful state changes. Walk the interaction on Android and distinguish captured evidence from source observations. Fix the largest issue and capture again.
5. **Verify and hand off.** Run the project's unit tests, build, and lint. Keep results separate from device and owner visual acceptance. For an authorized APK update, verify the downloaded GitHub release asset's exact hash, package `com.courtquestpro`, compatible signer, and a version code that advances the installed build.

### UI/UX Pro Max application checkpoint

The first direct CourtQuest searches were run on November 10, 2026 using the repo at commit `6ae86f61f520d6ca229d2f40e070410638c908c5`. Its guidance, search scripts, datasets, and MIT license are installed as the reusable `ui-ux-pro-max` skill. The first broad basketball query returned a General fallback. The refined Fitness/Gym App query matched the product and reasoning records, with `reasoning_default` set to `false`. [Pinned upstream source](https://github.com/nextlevelbuilder/ui-ux-pro-max-skill/tree/6ae86f61f520d6ca229d2f40e070410638c908c5)

| Search | Verified result | CourtQuest decision |
| --- | --- | --- |
| `Fitness/Gym App` with `--design-system` | Fitness/Gym App product and reasoning, Sports/Fitness typography, progress-tracking and workout-plan constraints | Use a state-aware daily-session panel and clear recorded progress. Compare the proposed Barlow Condensed and Barlow pairing in a phone-sized visual target before selecting fonts. |
| `Material3 Scaffold insets semantics` with `--stack jetpack-compose` | Shared Material 3 theme, semantics, reusable layout patterns | Centralize theme and screen primitives and verify labeled controls and system insets against the project's pinned Compose BOM. The source rows target Compose 1.11.4, so check API compatibility with BOM 2024.12.01 before adopting examples. |
| `badge chip label wraps` with `--domain ux` | Compact Label Overflow applies to all platforms | Keep short weekday labels whole and make the chip grid reflow with width and text scale. Use the full day name in semantics. |
| `bottom navigation back behavior` with `--domain ux` | The mobile Back Button record calls for predictable history | Verify Back through the workout, result, and Progress journey. Use native Android history and system insets when adapting the mixed-platform results. |
| App delivery checklist | Android touch targets, focus order, pressed feedback, contrast, safe areas, and large text | Apply the platform-specific 48 dp target rule and review actual Android captures. |

Run searches from the installed skill so its local datasets resolve correctly. Review proposed system output before saving accepted choices in this file.

```bash
cq_pro_max_skill="$(rg -l '^name: ui-ux-pro-max$' "${CODEX_HOME:-$HOME/.codex}/skills/remote-skills" -g SKILL.md | head -1)"
test -n "$cq_pro_max_skill"
cq_pro_max_search="$(dirname "$cq_pro_max_skill")/scripts/search.py"
python3 "$cq_pro_max_search" 'Fitness/Gym App' --design-system --json -p 'CourtQuest Pro'
python3 "$cq_pro_max_search" 'Material3 Scaffold insets semantics' --stack jetpack-compose --json
python3 "$cq_pro_max_search" 'badge chip label wraps' --domain ux --json
python3 "$cq_pro_max_search" 'bottom navigation back behavior' --domain ux --json
```

### Available tools and when to use them

| Tool | Use in CourtQuest |
| --- | --- |
| Figma | Editable Android frames, component anatomy, or a before-and-after review board for a material design pass. Connection verified on November 10, 2026. Verify edit access and target file when needed. |
| Product Design | Flow analysis, visual exploration, and structured review. Use Android captures to ground conclusions about the installed app. |
| ImageGen | Original art and visual direction where a raster asset or design comparison improves the decision. Use exact native drawings for court geometry, icons, and measured component specs. |
| Context7 and Android docs | Resolve Compose APIs against the checked-in BOM and SDK before relying on new examples. |
| GitHub | Locate the exact revision, retain design checkpoints, inspect CI artifacts, and distribute authorized APK updates. |
| Android SDK, Emulator, and `adb` | Launch the APK, record the actual UI, verify screen states, and exercise offline behavior, app updates, font scale, and relaunch. |
| MagicPath | Available as an optional interactive web prototyping tool. It was not installed at this checkpoint. Use when a browser prototype is useful for a design decision. |

### All 14 references from the owner's screenshots

| Reference | CourtQuest use | Upstream |
| --- | --- | --- |
| UI/UX Pro Max | Mobile UX, typography, hierarchy, and accessibility prompts during design exploration. | [nextlevelbuilder/ui-ux-pro-max-skill](https://github.com/nextlevelbuilder/ui-ux-pro-max-skill) |
| Taste Skill | Set art direction using deliberate reference choices. Transfer its visual checking habit to Android capture. | [Leonxlnx/taste-skill](https://github.com/Leonxlnx/taste-skill) |
| Awesome DESIGN.md | Compare how design systems express tokens, intent, and examples. Author an original CourtQuest standard. | [VoltAgent/awesome-design-md](https://github.com/VoltAgent/awesome-design-md) |
| Google DESIGN.md | Keep palette tokens and visual rules together in this file, with Android units in the prose. | [Google Labs format specification](https://github.com/google-labs-code/design.md/blob/main/docs/spec.md) |
| Open Design | Optional local design exploration or handoff workspace if a separate prototype helps. | [nexu-io/open-design](https://github.com/nexu-io/open-design) |
| React Bits | Browser animation and composition reference. Recreate relevant ideas in native Compose. | [DavidHDev/react-bits](https://github.com/DavidHDev/react-bits) |
| Magic UI | Reference selected-state and motion ideas. Use its components for compatible web prototypes. | [magicuidesign/magicui](https://github.com/magicuidesign/magicui) |
| Canvas UI | Browser shader and texture reference for optional brand work. | [DavidHDev/canvas-ui](https://github.com/DavidHDev/canvas-ui) |
| Emil Kowalski's Skills | Reference easing, timing, and responsive feedback for native transitions. | [emilkowalski/skills](https://github.com/emilkowalski/skills) |
| GSAP Skills | Use when a separate web prototype calls for a GSAP animation. | [greensock/gsap-skills](https://github.com/greensock/gsap-skills) |
| Scroll World | Reference for a future browser-based CourtQuest launch or marketing experience. | [oso95/scroll-world](https://github.com/oso95/scroll-world) |
| Dimensional Drawing | Document a custom component's measured padding, radii, alignment, and behavior. | [enesozturk/dimensional-drawing](https://github.com/enesozturk/dimensional-drawing) |
| Impeccable | Default critique vocabulary for the finished Android candidate, including hierarchy, interaction, contrast, and generic patterns. | [pbakaus/impeccable](https://github.com/pbakaus/impeccable) |
| Hallmark | Independent review reference when a candidate still feels like a stock dashboard or card stack. | [Nutlope/hallmark](https://github.com/Nutlope/hallmark) |

Native implementation references include Jetsnack for a custom Compose design system, Jetcaster for distinct yet readable editorial surfaces, and Now in Android for app-wide components and adaptive layout. Use the current project and its pinned dependencies as the implementation authority. [Compose Samples](https://github.com/android/compose-samples) · [Now in Android](https://github.com/android/nowinandroid)

## First daily-use design pass

Build 2's Court implementation places the branded header and player performance card before the daily training section. Its workout panel shows the same completion state as a count, percentage, progress bar, and six-segment bar. The first candidate should lift the state-aware training action above secondary stats and keep one clear drill completion indicator. Carry the same type, action, and status hierarchy through the drill and result so the flow feels like one product. Keep Progress as the home for evidence and Setup as the home for editing preferences. Apply the same system to the rest of the app after reviewing the first complete journey.

| Evidence to keep | Acceptance check |
| --- | --- |
| Build 2 and candidate screenshots at matching size and state | Compare Court, active drill, recorded result, and Progress. Use a normal phone width at default text plus 320 dp at 130% text. Exercise 200% text on critical controls. |
| User journey recorded on Android | Open Court on an existing profile, start or resume a drill, record successful and missed attempts, save once, then find that result in Progress. Check Back and relaunch. |
| Related states | Check a fresh profile, a blank and long player name, a completed workout, no progress history, and active scorekeeping when shared components change. Check TalkBack labels and system animation settings where available. |
| Technical result | With JDK 17 and Android SDK 35, run `./gradlew --no-daemon --console=plain :app:testDebugUnitTest :app:assembleDebug :app:lintDebug`. For changes to repository tooling, run `python3 -m unittest discover -s tests -v`. |
| Review result | Record the exact APK and source, the reference and tools used, the issue fixed, any blocked checks, and the owner's visual decision. Count a premium refinement as accepted only after actual Android evidence and owner review. |
