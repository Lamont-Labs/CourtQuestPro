---
version: alpha
name: CourtQuest Pro
description: A native Android basketball training and scorekeeping design system.
colors:
  primary: "#FF5B0A"
  accent: "#FFA36B"
  on-primary: "#1B0C03"
  background: "#050505"
  surface: "#101112"
  surface-elevated: "#191A1C"
  border: "#292A2C"
  control-outline: "#777A7D"
  surface-warm: "#291207"
  court-shadow: "#030303"
  text-primary: "#F7F4EF"
  text-secondary: "#AAA9A6"
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

## Premium customization and whole-app quality batch

The approved expansion retains existing artwork and live jersey state. There are
43 avatar choices across seven dimensions: eight hairstyles, five headwear
choices, four illustrated builds, nine skin tones, seven shoe colors, six
accessory combinations and four footwear constructions (the original plus
three new models). Four card materials, five original poses and four anonymous
face treatments bring the separate visible-choice inventory to 56; this is not
a count of independent full-body models or theoretical combinations.

New ready/follow-through athletes have original approved-direction illustration
plates and authored garment/face/skin boundaries. New Court Low, Elevate High and
Lockdown shoes use different original mesh, high-collar and strap constructions,
not the same recolored silhouette. Detailed original hair cutouts replace the
old repeated-dot curls/locs/braids. The original three athlete plates and three
material masters remain untouched; Obsidian Chrome adds its own composition.

Studio materials have short touch-triggered rail reflections, not perpetual
shimmer. Edition changes retain the chosen pose. Larger material thumbnails
scroll horizontally rather than shrinking four cards across a narrow phone.
Detail inspection uses full-resolution textures, with inactive uniform maps
sampled smaller and shared hair cutouts to constrain decoded memory. This is
still a layered 2D illustration system, not a simulated 3D body or garment fit.

The native source audit repaired lost informational section counts, added
screen-reader heading semantics and large-text section reflow, corrected Back
inside settings subpages, removed team-oriented custom lettering copy while
preserving its saved field, and let the Quests hero grow with readable text
rather than clipping its fixed-height content.

UI/UX Pro Max remains unavailable; no use is claimed. The bundled Impeccable
critique is applied to the impatient, one-handed and accessibility-dependent
journeys. Existing Compose APIs, Barlow typography, near-black/orange tokens,
48dp targets and solo-player scope remain authoritative. Matched baseline and
candidate Android captures, isolated artwork pixel comparisons, legacy-save
migration, four display configurations and deeper receipt/progression/game
journeys are verification gates. CI evidence and PR #9 record their actual
outcomes; source inspection and generated-art integrity are not runtime proof.

## Player Card V2

### Owner-requested avatar and collector-material revision

The owner reported that the athlete integration was improving but the cards
were “extremely mediocre.” The next pass prioritizes hair/headwear, build/skin
tone, and shoes/accessories. Original generated metal/foil/heritage sports
material plates replace the simple procedural frames. Athletes, player names,
numbers and the saved uniform remain live native layers, not baked screenshots.

The avatar editor uses Hair, Body and Gear tools inside the existing studio,
with fixed save/discard behavior and atomic cosmetic preferences separate from
training state. Skin and shoe dyes are clipped to pose-specific alpha mattes;
skin excludes the uniform. Proportions are modest localized 2D illustration
deformations, not real measurements or simulated 3D anatomy. Hair, headbands,
sleeves and wrist tape use pose-specific anchors. Anonymous faces remain the
default and no actual person's likeness is introduced. The existing Barlow,
Compose APIs, 48dp controls and artwork cache patterns remain authoritative;
no new runtime service or dependency is introduced.

UI/UX Pro Max remains unavailable in this workspace; no use is claimed. The
bundled Impeccable review is adapted to the native collector's identity hierarchy,
material distinction, live customization, legibility and actual Android captures.
Technical success is not owner aesthetic approval. Font-scale, persistence and
rendered evidence must be reported separately from source and asset integrity.

The first actual Android captures of the expanded editor exposed undersized
hair previews and category tabs that disappeared while scrolling the choices.
Hair/shoe tools now pair the complete collectible with a pose-aware detail
viewport, while Hair/Body/Gear tabs stay pinned above the scrollable inspector.
The 200% header uses the shorter “Studio” label without reducing font scaling.
Card lettering is fitted inside the authored nameplate, not its upper metal edge.

The selected collectible is now the main Me identity, replacing the duplicate
jersey/profile hero. XP/rank, actual saved training summary, milestones, profile
settings, history and Jersey Studio remain beneath it. No player metrics are
invented or included as cosmetic collectible ratings.

Holographic Elite uses cool prismatic foil and silver edge geometry; Court
Legends uses heritage metallic framing, engraved court texture and serif
collectible lettering; Battle Series uses angular black/orange light and action
composition. Original full-body standing, dribbling and layup illustrations
remain anonymous, with shadow, featureless and question-mark face treatments.
Live uniform masks carry all saved dye/ink roles and front lettering. The
edition/pose/face transaction is separate from player/training saves, preserves
the prior edition preference key and exits only on successful persistence.

The phone studio keeps a large live preview above progressive Edition/Pose/Face
tools and a fixed Save & Exit action. Large-text edition choices reflow to
thumbnail rows; native labels and controls respect font scaling. Back offers
save/discard rather than silently writing a draft. Brief selection crossfades
follow platform animation scaling; no continuous animation was added.

Existing native Barlow, surface primitives, selectable semantics and the pinned
Compose BOM remain authoritative. UI/UX Pro Max was not installed in this
workspace; no use is claimed. The bundled Impeccable reference informs identity
hierarchy, progressive disclosure, explicit save/discard copy, and actual Android
capture review. No browser prototype, dependency upgrade or branding replacement
is part of this implementation. Asset construction and illustration limits are
documented in `tools/player-card/README.md`. Build/runtime/visual verification is
recorded separately from owner approval; a green build is not aesthetic approval.

## v42 drill-to-receipt batch

Drill discovery matches words across the catalog and written technique cues.
Drill detail puts the target and observable technique checkpoint before the
practice action, then uses the actual bundled plate with an offline pan-and-zoom
viewer. The 18 approved plates are preserved. Illustrated sequences and
comparisons are references, not proof of technique; written cues specify what to
count, including side balance, intervals, spacing and controlled attempts rather
than treating every successful action as a made basket. Custom drills keep their
own instructions without inferring a movement pattern.

Live recording prioritizes counts and large attempt actions over the timer.
Corrections persist to the draft; falling below the target clears confirmation.
Saving requires matching confirmed counts on the current daily slot and refuses
an existing result for that slot and date. Receipts separate self-recorded
counts and participation XP from skill claims and have no second Save action.
New drill-specific measurement wording is frozen when recorded; old result and
Mission snapshots are not rewritten.

## v42 first solo-player implementation batch

The owner authorized implementation of Court, Train, five-tab navigation and the
walkthrough as one substantial batch. “We will be leaving school and coach
functions out completely for now. Walkthrough is still huge. I dont want to have
to do 100 tasks.” Team entry points are also excluded from these active surfaces;
historical data/models remain untouched.

Court prioritizes the actual saved next drill and Start/Resume action, with the
approved Signature wordmark and arena artwork retained. Train uses a compact
header, real saved-plan actions and closer search/results rather than a 190dp
decorative hero. The segmented selector uses a true equal-width Row at normal
font sizes and an explicit vertical accessibility layout above 130%.
Emulator review caught a nested full-width heading that starved the adjacent drill
count of width and inflated the Library header. The corrected weighted heading,
short search label, expandable advanced filters and initially expanded drill groups
keep actual artwork/results closer to search. Level, duration, favorites, custom
creation and Mission Builder remain available; enlarged text gets more room than
decorative row thumbnails.
Rendered 200% text review additionally required a full-width plan count with the
difficulty label below it, not competing beside it. At enlarged text the same
Start/Resume control moves to the top of that panel so the primary action remains
in the first viewport. Normal/130% text retain the compact side-by-side summary.

The optional walkthrough is a contextual strip reserved by Scaffold above the
dock, not an overlay on the screen. It starts on Court, supports Back/Next/Skip,
persists dismissal, and leaves all five destinations available. Its controls have
48dp targets. The 58dp dock retains original navigation artwork, adds readable
labels at normal text sizes, and keeps accessible full names at enlarged text
without capping font scaling. No new libraries, web mockups or generated art.

Reuse review applied the bundled Impeccable shape/critique/clarify/onboard guidance
and existing native controls. The UI/UX Pro Max installation was not found in
this workspace; no use of the historical Product Design/Figma/Context7
installations is claimed. Existing pinned Compose
APIs and native tokens remain authoritative. Rendered acceptance requires the
matched v41/candidate Android captures, separate from build/unit/lint checks.
This batch does not authorize publication or change package, signer, version or
the saved-data schema.

## Offline drill illustrations

The current library contains 18 drill-specific generated athlete plates, bundled
as WebP with a deterministic catalog-derived manifest. The original court diagrams,
written instructions, practice controls, branding and navigation remain intact.
Generic posture silhouettes are replaced by illustrated sequences and drill-specific
setup/movement/checkpoint cues. Comparison panels are omitted when unsafe or misleading;
the coverage report explains each exclusion.

The installed UI/UX Pro Max guidance was unavailable in this workspace for this pass.
Existing native tokens, Barlow typography, responsive image aspect ratios and 48dp
controls remain authoritative; no newer Compose APIs or web patterns were introduced.
Generated plates were visually reviewed before compression, including removal of an
unsafe contact inset from the box-out plate. This is not biomechanical certification.
Android display/scrolling evidence is captured through the existing native library run,
separately from image integrity, catalog coverage, compilation, tests and lint.

## Overview

CourtQuest Pro is a basketball performance studio for a player on a phone. It opens onto a clear training decision, supports a focused drill or one-on-one game, then makes the recorded result easy to find. The latest owner-selected direction is deep black, brighter orange, polished glass layers, crisp high-definition rendering, and Signature branding. Use basketball court geometry, confident typography, and carefully prioritized data to give CourtQuest its own identity. Use calmer, more direct layouts while the player is recording attempts or keeping score.

## Approved cinematic eight-screen rearchitecture — November 15, 2026

Owner: “These are badass!!! Do that!!” This explicitly replaces an incremental
polish scope with the described eight-screen arena composition: immersive
athlete-led welcome; personalized Court hero and six shortcuts; hoop-led Train;
dominant live clock ring; evidence-based Progress chart; dimensional Quest
shields; honest Team space; jersey-centered Me. Keep the original Signature
logo and jersey artwork, real saved names/numbers/results, package and signer.
The owner supplied the approved 1199 × 1312 eight-panel reference as
`attached_assets/file_00000000552c81fd852527adb08f0afa_1794708439004.png`.
Use its hierarchy, orange arena lighting, athletic type, glass panels, and
compact five-item dock as the visual target. Its sample player, jersey, rank,
scores and “Indians” team are illustrative only; never treat those values as
real saved player/team data.

Presentation ownership stays separate from persistence, scoring, workout
generation and award rules. The existing source contains no authorized team
membership backend; Team must clearly state that it is not configured and
must not invent a roster, schedule, assignments or working invite service.
Bottom navigation stays one row and 56dp, with five non-overlapping 48dp
targets and an original native basketball medallion for Court in the center.
Original artwork in the side drawer is retained.

Guidance rerun against pinned UI/UX Pro Max `6ae86f61`: Fitness/Gym App matched
Sports/Fitness and the existing Barlow/Barlow Condensed pairing; native
`jetpack-compose` searches matched shared layout and explicit semantics; UX
matched 48dp Android touch targets and readable contrast. Its marketing
showcase layout, alternate green CTA and newer Compose examples are not
adopted. Native Canvas charts use real dated results, an honest zero baseline,
accessible textual values and no invented proficiency. Assets are documented
in [cinematic provenance](docs/tooling/CINEMATIC_ASSET_PROVENANCE.md).

Acceptance requires actual baseline/candidate emulator captures for all eight
screens, including no-team and no-history states, compact/enlarged text and
existing full native/drawer regressions. Compilation or pixel differences alone
are not aesthetic acceptance. No new public APK is authorized by this work.

## Native transformation following the v21 phone review

The owner confirmed v21 installed correctly but reported “There was absolutely no change at all.”
That release repaired the update pipeline; it was not a visual redesign. This pass starts at
`de7127db` and changes the real Kotlin/Compose presentation on `dev/premium-flow-toolkit`.
It does not authorize a new public APK release.

- Court foregrounds actual daily completion and the start/resume action with an editorial numeric hierarchy.
- Training entry separates the saved target from planned minutes while preserving all recording controls.
- Train/Library uses an integrated discovery header and a native segmented selector instead of stacked default chips.
- Progress uses a saved-record feature with actual XP, recorded activity and history.
- Quests gives existing daily evidence a segmented visual treatment; no new award rules are implied.
- Me leads with the existing approved jersey and saved player identity, with profile editing and routine controls retained.
- A single-row 56dp bottom dock complements, not replaces, the edge drawer. All destinations retain
  accessible names and 48dp targets. At enlarged text only the selected label is rendered; its scalable
  type is never capped. The drawer stays clear of the dock and system bars.

This owner request supersedes the older “do not redesign other screens” restriction and the
edge-only navigation presentation below. Original Signature resources, navigation artwork, fonts,
save schema, app identity, signing configuration and versionCode remain unchanged.

Guidance used: pinned UI/UX Pro Max source `6ae86f61`, queried for **Fitness/Gym App**
(exact product/reasoning match; Sports/Fitness Barlow pairing), native **jetpack-compose**
semantics/reusable layouts, and compact navigation touch/overlap guidance. Retain the bundled
Barlow fonts and the owner's black/orange identity; reject the web showcase layout, green CTA,
hover effects and 48px showcase gaps. The dataset's Compose 1.11.4 examples are newer than the
project BOM, so existing compatible Compose primitives remain authoritative. Apply the local
Impeccable critique to actual Android captures, not generated mockups.

Rendered verification: `verify-premium-visuals.yml` installs the hash-pinned released v21 on a
guarded disposable Android 35 AVD, captures identical synthetic data before/after at
360dp/100%, 320dp/130% and 320dp/200%, checks data-preserving installation, measures every
dock target and compares body pixels excluding system bars/dock. Seven normal-size comparison
images cover Court, training entry, Library, Progress, Quests, Me and the drawer.
The first matched Android comparison passed for all seven surfaces in normal, compact and 200%
text configurations. Manual review confirmed body-level changes, not just a new dock or palette.
That review also caught split words in Library controls at 200% text. Rails and Library actions
now reflow to full-width targets above 130% text without reducing the user's font scale. The
emulator gate additionally requires reachable, full-width large-text actions and captures them
separately. Final checks of this accessibility refinement and owner visual acceptance remain pending.
Existing full native journeys, unit/lint and compact-drawer checks remain separate acceptance gates.

Early real Android critique: compilation, 72 Android unit tests, lint (0 errors) and the existing
Maestro navigation flow passed. The first rendered review identified excessive Library controls
before the drill list and overly warm surface lighting. The candidate now integrates the filtered
count into a compact header, puts category/filter controls in a reflowing row with 48dp minimum
targets, and reduces orange illumination to restrained edge reflection. Matching before/after
capture acceptance remains pending. The capture harness explicitly relaunches after fixture
seeding, since the existing seeder intentionally force-stops the test application.

This design standard began with the owner's build 2 feedback. Build 5 received positive visual feedback (“Its looking amazing”); build 6 extends that approved system with the Drill Library. Build 2 used app source `21ff9222edd69f5c65c1999082b751c104d63194`, published in release commit `c3e57e93708edcb6e6fa61c43cddc79dae7314f3`. Confirm the current branch, APK version code, and owner feedback at the start of each pass. This file is the active standard. The earlier [design contract](docs/COURTQUEST_PREMIUM_UI_DESIGN_CONTRACT_v1.md) records the initial baseline.

## Latest owner visual direction: deep black and brighter orange

Saved November 11, 2026. Owner instruction: “super dark black and brighter orange” and “Super sharp, crisp and high definition.”

This applies across the entire app: launch branding, onboarding, Court, training, scorekeeping, Progress, and Setup. The selected **Signature** CQ emblem and CourtQuest Pro wordmark remain the brand identity.

| Area | Required direction |
| --- | --- |
| Main canvas | Deep, true black; use black as the dominant app background |
| Supporting surfaces | Polished smoked-black glass layers with glossy reflections, crisp highlighted edges, and clear depth |
| Orange | Brighter, vivid, saturated orange for primary actions, active states, and brand accents |
| Text and edges | Crisp high-contrast lettering, sharp artwork edges, and precise alignment |
| Artwork quality | High-resolution masters or suitable native/vector assets; preserve aspect ratio and avoid visible pixelation, blur, halos, and extraction residue |
| Signature identity | Preserve the approved brush lettering, CQ silhouette, underline, and basketball detail while adapting the surrounding palette |

This direction supersedes the earlier warm-brown and muted-charcoal emphasis. The front-matter values now match the Signature journey implementation and shared native tokens. Choose the exact brighter orange against the rendered black UI and Signature artwork rather than treating the older orange token as a locked shade.

“High definition” means clean assets and precise rendering at actual phone sizes and densities. Maintain readable scalable text, accessible contrast, and clear control boundaries on black. Sharpness does not require removing the approved logo's brush-shaped terminals or changing every corner radius.

### Polished glass layers

Added November 11, 2026. The owner also requested “super high quality shiny glassomorphic layers.” Combine this with the deep-black canvas, brighter orange, crisp rendering, and selected Signature branding as one app-wide visual direction.

| Material detail | Required treatment |
| --- | --- |
| Glass color | Rich smoked-black translucent layers that preserve the dominant black palette |
| Shine | Deliberate glossy reflections, fine illuminated edges, and controlled specular highlights |
| Layering | Visible separation between background, glass surfaces, floating navigation, and overlays |
| Orange lighting | Bright orange accents and restrained reflected orange light along selected glass edges |
| Background depth | Subtle court linework or light gradients behind selected glass areas so translucency has visible depth |
| Surface quality | Smooth gradients, clean corners, precise clipping, and crisp edges at actual device density |
| Readability | Sharp white or ivory lettering and clearly readable orange actions over the final composited surface |

Apply a coherent glass material to hero panels, training surfaces, navigation, and overlays, with shine and depth scaled to each surface's role. Keep active scorekeeping and drill controls stable, immediate, and easy to read. Render the reflection and translucency behind foreground content; text, scores, icons, and the Signature artwork must stay sharp.

Use controlled light direction and consistent edge treatment to make the layers feel like polished tinted glass. Keep the underlying app predominantly black and preserve clear differences between resting, pressed, selected, and disabled states.

Design reference checked: the installed UI/UX Pro Max **Glassmorphism** style record supports dark mode, translucent layers, light reflections, subtle borders, and depth. Its implementation examples target web frameworks; use the material principles for native Compose and validate actual rendering, contrast, animation settings, and device performance. A blur value from a web example is not an approved native token. Treat blur as an optional part of the material and verify any chosen implementation against the app's supported Android versions.

**Status:** Preference saved as the app-wide visual target. This documentation update does not claim that the installed app or APK already uses the revised palette.

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
| Supporting palette | Deep black, brighter orange, and crisp white or ivory brand lettering |

Use the selected CQ mark and wordmark as the reference for launcher artwork, launch branding, onboarding, and appropriate app headers. Preserve the distinctive letter shapes and sweeping composition; a generic script font is not an equivalent replacement. Keep functional labels, instructions, scores, timers, and statistics in readable scalable native type.

Production preparation must separate the CQ icon and wordmark from the presentation board, omit the “05 / SIGNATURE” concept label, and prepare suitable light and dark treatments. Check launcher masking and small-size recognition, then verify the actual Android integration.

**Status:** Owner-selected visual concept. Separate high-resolution launcher artwork and light/dark wordmarks were prepared on November 11, 2026. Android integration and a verified APK containing this identity remain pending. This selection does not establish runtime or launcher acceptance.

### Signature artwork preparation checkpoint

[CourtQuest_Signature_Brand_Pack_v1.zip](https://chatgpt.com/library?file_id=libfile_917d539dbe6c8191b49f05e6635322a1&account_id=personal) contains the original approved reference, a 1254 × 1254 launcher master, two 2172 × 724 wordmark exports, usage notes, and per-file SHA-256 records.

The selected deliverables are **opaque PNGs**. Transparent cream exports showed background-removal residue and were rejected. Do not use the rejected alpha drafts as app resources. The opaque light/dark backgrounds also have slight raster variation; check surface edges before integrating, or prepare clean alpha/vector artwork first.

Generated renditions preserve the selected Signature direction but can vary slightly in letter contours; the included owner-approved reference remains the fidelity anchor. Native work still needs adaptive-icon framing and mask checks, launch branding, onboarding/header integration, and actual Android review. PNG decoding, unchanged-copy hashes, and ZIP CRC integrity were verified. No application source or APK was changed in this artwork-only pass.

## Colors

Follow the latest owner visual direction above and keep the implemented palette in sync with [`CourtQuestDesign.kt`](app/src/main/java/com/courtquestpro/CourtQuestDesign.kt). Use deep black as the dominant canvas and very dark neutral surfaces for supporting content. Use brighter saturated orange for the primary action and important active states, with readable dark ink on filled orange controls. Use crisp white or ivory for primary text and adequately contrasting secondary text. Keep boundaries and states clear on black, and label status with text, iconography, or semantics in addition to color. The front-matter values match the implemented shared tokens; acceptance is tracked in the current pass ledger.

## Typography

Build 3 bundles Barlow Condensed ExtraBold for display typography and Barlow Regular/SemiBold/Bold for body and controls. The font files carry the SIL Open Font License in `app/src/main/res/raw/barlow_ofl.txt`. They were selected from the directly matched UI/UX Pro Max Sports/Fitness guidance. Actual phone-sized, enlarged-text, and owner review are tracked separately in the current pass ledger. Use a distinct display title, a clear screen or section heading, readable body copy, and a compact label style. Apply all-caps to short supporting labels. Favor sentence case for guidance and actions that need to scan quickly. Give scores, timers, and counts stable alignment as digits change. Keep body guidance readable at large font settings and allow content to wrap without moving the primary action out of reach.

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

Follow the owner-selected polished glass direction above: rich smoked-black translucent surfaces, deliberate glossy reflections, crisp edge highlights, layered depth, and reflected bright-orange accents. Use existing court linework as a quiet brand cue behind selected welcome or Court glass surfaces. Maintain a consistent light direction and precise clipping across shared components. Let spacing, typography, dividers, and grouping keep the hierarchy clear within the layers. Keep the active drill and scorekeeper visually stable while controls change state. Use motion to explain a transition, confirm a result, or respond to a tap. Keep entry and exit transitions short, support the system animation setting, and preserve immediate feedback on scoring and recording actions. Validate glass contrast and rendering performance on actual Android captures.

## Shapes

Use 12 dp control, 16 dp standard surface, and 24 dp hero corner radii as starting tokens. Use pill shapes for compact status labels or true selection chips. Keep the selected state readable at enlarged text sizes. Give actionable controls at least a 48 by 48 dp target. Check text contrast and accessibility semantics against [Android accessibility guidance](https://developer.android.com/guide/topics/ui/accessibility/apps).

## Components

- `CourtQuestDesign.kt` owns shared native color and spacing tokens.
- `AppHeader.kt` and `CourtOnboardingWelcome.kt` provide existing editorial identity and court artwork.
- `CourtQuestPrimaryButton.kt` and `CourtQuestPremiumSurface.kt` provide shared action and surface primitives. Compose Material 3 supplies semantic, focusable interaction building blocks.
- `CourtQuestTheme.kt` owns the shared Material 3 typography and color scheme; `CourtPresentation.kt` owns section, metric and status primitives.
- `CourtHomeScreen.kt`, `CourtDrillScreen.kt`, `CourtProgressScreen.kt`, and `CourtSettingsScreen.kt` own the daily UI. `MainActivity.kt` coordinates saved state, navigation, onboarding and scorekeeping. Preserve normal navigation, scoring, saved state, history, and training rules through each visual pass. Keep names dynamic and preserve the existing generic labels for blank names.

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

## Owner-approved premium flow toolkit

On November 11, 2026 the owner approved adding the complete researched toolkit. Its active guide is [PREMIUM_FLOW_TOOLKIT.md](docs/tooling/PREMIUM_FLOW_TOOLKIT.md), with immutable upstream pins in [references.lock.json](tools/premium-flow/references.lock.json).

Use Product Design and Figma for the actual journey and editable screen specifications; Context7 for version-correct native APIs; Compose Samples and Now in Android for native patterns; the vendored Impeccable guides for structured review; Maestro for repeatable dedicated-emulator journeys; Intro Showcase and Lottie for purposeful guidance and feedback; and compatible Haze plus native drawing for the glass material work. Verify the readiness and evidence in the toolkit guide before claiming a tool has been exercised.

The source baseline is the verified build-6 lineage, preserving all custom-drill, daily-training and game-flow work. The owner's newer Signature, black/orange and glass preferences above are carried forward from the original design branch. No runtime color or layout change is claimed by this setup checkpoint.

## First daily-use design pass

Build 2's Court implementation places the branded header and player performance card before the daily training section. Its workout panel shows the same completion state as a count, percentage, progress bar, and six-segment bar. The first candidate should lift the state-aware training action above secondary stats and keep one clear drill completion indicator. Carry the same type, action, and status hierarchy through the drill and result so the flow feels like one product. Keep Progress as the home for evidence and Setup as the home for editing preferences. Apply the same system to the rest of the app after reviewing the first complete journey.

| Evidence to keep | Acceptance check |
| --- | --- |
| Build 2 and candidate screenshots at matching size and state | Compare Court, active drill, recorded result, and Progress. Use a normal phone width at default text plus 320 dp at 130% text. Exercise 200% text on critical controls. |
| User journey recorded on Android | Open Court on an existing profile, start or resume a drill, record successful and missed attempts, save once, then find that result in Progress. Check Back and relaunch. |
| Related states | Check a fresh profile, a blank and long player name, a completed workout, no progress history, and active scorekeeping when shared components change. Check TalkBack labels and system animation settings where available. |
| Technical result | With JDK 17 and Android SDK 35, run `./gradlew --no-daemon --console=plain :app:testDebugUnitTest :app:assembleDebug :app:lintDebug`. For changes to repository tooling, run `python3 -m unittest discover -s tests -v`. |
| Review result | Record the exact APK and source, the reference and tools used, the issue fixed, any blocked checks, and the owner's visual decision. Count a premium refinement as accepted only after actual Android evidence and owner review. |

## Build 3 implementation checkpoint

The owner authorized implementation and an in-place APK update. The daily flow, actual receipt and dated session history are implemented in native Compose, with saved profile names and measurements. The UI branch is `dev/premium-daily-flow`; the authorized APK release branch remains `dev/scorekeeper-undo-history`. [Current pass and verification evidence](docs/plans/2026-11-10-daily-flow-visual-pass.md).

The published build 3 has the preserved package and certificate, and its downloaded SHA-256 is `1056c917720dd7b5a2265e4a0069db6d0a3f8c78befde19d94b143090981bbda`. Build, unit tests, lint and packaging checks passed. Actual Android screen review and owner visual acceptance must be read from the pass ledger; those are separate claims from a green build. The native workflow uses the pinned build-2 source so replacing the shared release asset does not destroy the comparison baseline.

## Verified visual build 4 checkpoint

Build 4 is published and its downloaded bytes match the exact APK that passed the full Android journey. Source app/src tree: `600e809779b58d9416834050a2a27a1391386cde`. Candidate: `d7cb1aa9cb56f1548b9ccd139894078815fa3310`; release: `44cc62d363e488d8ca0d20311fe50923ee903be3`. The implementation has a single drill heading, dated records before activity summaries, virtualized full history with native Back, explicit warm Material selection roles, readable control outlines and an original native launcher mark.

Verified: 25 Android tests, 53 repository tests, build, lint (0 errors, 10 warnings), APK identity/signature/ZIP/alignment and an actual Android update/walkthrough. Captures cover matching 360 dp baseline/candidate, 320 dp at 130%, and critical Setup/Drill/Score controls at 200%. The published checksum is `e6003261be06548f82d1aee916a48101ce5d97958c006c9a5f10178b8ebf5b89`.

[Native flow evidence](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/42948474947) · [Downloaded release verification](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/42949879530) · [Repository tests](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/42949653284). The same release retains Court, Drill, Result, Progress, Setup and enlarged-text PNGs, using synthetic profiles. Continue to use UI/UX Pro Max's native guidance and the actual rendered-review workflow. Owner visual acceptance, physical-phone operation, TalkBack traversal, landscape and tablet review remain open.

## One-on-one visual build 5

Continue the established native design through game setup, live scoring, the saved game receipt and dated game history. Use a compact game header, a unified scoreboard, 48 dp native controls and an adaptive stacked layout when enlarged text needs it. Keep names, score, Undo order and the same-day resume route bound to actual saved state. Receipts show the XP actually added by the existing award keys. The full game list uses individual LazyColumn items. No new assets or dependencies.

Verified reference searches and the exact RED resume regression are recorded in [the one-on-one pass ledger](docs/plans/2026-11-10-one-on-one-visual-pass.md). Build 5 is published after the complete Android walkthrough, matched capture review and downloaded-file verification. The active candidate branch is `dev/premium-game-flow`; preserve the independent `main` branch.

## Current verified visual build: 5

Source `67e9776c7983e77ef870146aede6db28927be51b`, app/src tree `c33f5a258704f520f1e95872661134a09bc14aa3`. [Native run 43043445045](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43043445045) passed both the existing daily flow and the expanded one-on-one flow on the exact APK SHA-256 `d2e6eb32d3d90d93a3be3cbbc08684e5090202746294e2ea0bf3583b5817339b`. Android unit tests: 31 passed; lint: 0 errors, 10 warnings.

The game setup, scoreboard, receipt and dated/virtualized game history now use the shared native visual system. Same-day games resume with saved score and Undo order; finished and expired games do not resume. Repeat games report actual awarded XP, and identical names do not change which side won. Normal, compact/130%, critical 200%, landscape and tablet control captures are retained. Physical-phone operation, owner visual acceptance and full TalkBack traversal remain separate. The exact tested APK was published in release commit `64a86fb96b1d182e2bda2e126a5c7570e3a0a434`. [Publication and downloaded APK verification](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43045952485) passed. The public GitHub download was independently downloaded again and matched the tested binary byte for byte. The existing development URL and signer are preserved.

Next open work: owner review on the phone and the next app-wide refinement informed by that review. An optimized landscape composition and full TalkBack traversal remain explicit follow-up checks; do not treat successful control discovery as visual acceptance.

## Verified Drill Library build 6

Owner feedback on build 5: “Its looking amazing.” Owner approved custom drills, suggestions, protected randomization and extra practice. Keep the approved dark/orange native visual system. Drills is a fourth navigation destination, using keyed lazy lists, search, skill/filter chips, short recommendation reasons, a native detail screen and a keyboard-aware editor footer. The successful-action target is a real completion requirement; planned minutes remain the training clock. Custom revisions preserve queued instructions and historical comparisons. Extra practice has its own persisted session and labeled receipts while existing daily XP rules stay intact.

Implementation and test ledger: [Drill Library plan](docs/plans/2026-11-11-drill-library.md). UI/UX Pro Max matched Fitness/Gym App/Sports-Fitness and explicit empty-state/native-list guidance. Web-only suggestions were adapted to local search and existing Compose primitives. New API usage was checked against official Android interoperability guidance; the installed BOM remains unchanged. Source branch: `dev/drill-library`; build 6 is published after complete native flows and exact downloaded-binary verification. The release branch remains `dev/scorekeeper-undo-history`, with main preserved.

Actual build-6 captures at 320 dp/200% exposed a split brand word and a split Progress navigation label. The follow-up uses a wrapping brand group so the complete mark stays together, and the existing Barlow Condensed face for enlarged navigation labels. The labels retain their system-scaled 12 sp size and whole words. The normal-size typography and destinations stay the same. Final capture verification is recorded in the Drill Library ledger.

Build-6 native acceptance is GREEN on source `0ff5e6c5d78ba8b958deef01478475fe5c78f4a0`, app/src tree `57fcba4679745dae0a1b85dff0bd93235b2bc757`, [run 43060242201](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43060242201). The same APK passed 42 Android tests, lint 0 errors/10 warnings and all library/daily/game journeys, including the actual saved-Activity score rollover regression. Final 320 dp/200% captures confirm whole brand/navigation words and a reachable editor footer above the keyboard. The exact artifact's SHA-256 is `00bcd7d1296ac2a71efadad8beb42834e3f9897ff7b5af668c020c731507a3d4`; publication and downloaded-byte verification are recorded in the pass ledger.

Build 6 is published in release commit `e28a28680e397474584c80cd68bb991221cbda8d`. [Release run 43063480763](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43063480763) passed exact-source/artifact checks and verified the downloaded APK's package, version 6, original certificate, v2 signature and alignment. A further independent public download matched the tested 17,047,484-byte APK byte for byte: `00bcd7d1296ac2a71efadad8beb42834e3f9897ff7b5af668c020c731507a3d4`. Normal/compact/enlarged Android screenshots are retained on the existing release. Physical-phone operation, owner visual acceptance of build 6 and full TalkBack traversal remain separate.


## Signature journey build 7 — implementation checkpoint

Owner authority: the November 11 request to implement and deliver the entire native Court → start/resume → record attempts → save once → receipt/next drill → Progress journey, starting at `f8089ac614250472c6190ce3fcbd3f67c7455ff1` on `dev/premium-flow-toolkit`. [Plan and live verification ledger](docs/plans/2026-11-11-signature-training-journey.md).

Implemented components: `CourtQuestScene` owns the quiet native court backdrop and shared Haze source; `CourtQuestPremiumSurface` supplies smoked tint, a broad directional reflection and illuminated rim, with optional Haze 1.1.1 child blur and its supported-platform fallback; `CourtQuestPrimaryButton` supplies the orange action treatment; `AppHeader` uses the clean Signature artwork; `CourtQuestNavigation` owns native labeled selection and safe insets. Foreground text and controls are outside the blur. `CourtMetricStrip` reflows three-value summaries at enlarged text sizes. Recording remains immediate; the result has a fixed action area, a real saved-result summary and the actual next drill.

`nextDailySlot` now gives Court and receipts one stable route: a valid current draft first, otherwise the first unfinished slot. The explicit protected shuffle/swap features still choose the workout. This implementation call replaces the old random selection on every Continue tap so the preview cannot disagree with the next screen. The existing result/XP guards, save schema, custom revision handling and game/Undo code remain in place. Six new routing tests failed against the empty implementation and the full suite subsequently passed 48 tests.

Brand production: [source hashes and output hashes](tools/branding/signature-assets.json), [reproduction tool](tools/branding/prepare_signature_vectors.py), and vector masters in `tools/branding/vectors/`. The original owner-approved reference is the contour anchor, including its precise letter shapes, underline and basketball seams. Transparent generative cutouts still showed residue and were rejected. Exact contour masters produce clean transparent xxxhdpi PNG renders for the Android header and launcher; native orange supplies the adaptive-icon background. Adaptive foreground scale is 0.65, preserving safe mask space. Dense brush contours are rasterized once during authoring to avoid runtime path parsing. Normal phone rendering and launcher masks remain part of native review, not inferred acceptance.

References applied in this pass:

- UI/UX Pro Max `Fitness/Gym App` matched Sports/Fitness Barlow/Barlow Condensed and tracking/workout constraints. Keep those relevant results; do not adopt its web landing-page structure or green CTA palette over the owner's direction.
- Its `bottom navigation back behavior` mobile result informs stable Back and retained drafts. `Scaffold window insets semantics` native results inform shared components and semantics, checked against BOM 2024.12.01 rather than the dataset's newer Compose version.
- [Pinned Jetsnack Surface](https://github.com/android/compose-samples/blob/25631e8f544a9a1e3f01e296eaf1b4c28428196c/Jetsnack/app/src/main/java/com/example/jetsnack/ui/components/Surface.kt) informs the shared native surface boundary; [pinned Now in Android Navigation](https://github.com/android/nowinandroid/blob/2982e63cb691ad49cd009aab43dc206fee6699c2/core/designsystem/src/main/kotlin/com/google/samples/apps/nowinandroid/core/designsystem/component/Navigation.kt) informs native icon/label/selection slots. No newer adaptive navigation APIs are imported.
- Context7 returned newer Haze migration material, so actual API use was verified against pinned [Haze 1.1.1 source](https://github.com/chrisbanes/haze/blob/0cd75baaeb728f45a97abbb4e134a96905066528/haze/src/commonMain/kotlin/dev/chrisbanes/haze/HazeChild.kt): `haze`, `hazeChild`, `HazeTint`, fallback tint and an 18 dp blur. No 2.x glass APIs.
- Impeccable shape/clarify/onboard guidance informs one next action, an explained save requirement, a short saved-state confirmation and contextual hints. A one-shot original Lottie check acknowledges a save; animation scale zero uses a static check. Intro Showcase remains available; no forced tutorial is added over immediate recording controls.
- Product Design's screenshot-first critique is the rendered-review method. Figma flow-board generation was attempted; it requires a team selection, so the editable source-grounded flow specification remains in the repository and native implementation continues.

Current native acceptance: source `375a607fd7be33bf30ad56c2337cf9acf3ae2253`, app/src `839a30e17949d0b7d9dbe9284999637e2117aa34`, [run 43136062719](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43136062719) passed the exact-binary library/daily/game journeys and guarded Maestro. All 48 Android unit tests and 3 selector regressions passed; lint is 0 errors/11 warnings (six structural/resource warnings plus five availability advisories; the pinned stack is retained). Actual normal, compact/130%, critical 200%, keyboard and game captures were reviewed. [Master tracker](docs/MASTER_SPEC_IMPLEMENTATION.md), [native ledger](docs/tooling/evidence/signature-build7-native.json) and [rendered review](docs/tooling/SIGNATURE_RENDERED_REVIEW.md) record scope and limits. Build 7 was published in release commit `a431a69254b9d896d2a76640f5af363530f0dfa6`; [run 43138905997](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43138905997) passed publication/download verification. An additional independent public download matched the tested 20,988,666-byte APK byte for byte and passed package/version 7/original-signer/v2-signature/ZIP/16 KB alignment checks. Owner visual acceptance, physical-phone performance, full TalkBack traversal, API 26–30 fallback and alternate launcher masks remain separate.

## Mission Builder (CQ-PLAYER-002, candidate)

The saved Player Mode sequence adds one solo-workout flow inside the existing Signature world. Court and Drills open **Build a Mission**; a resumable Mission takes priority on Court. The builder shows title, honest planned minutes, an ordered lineup, explicit move/remove/keep actions, and a separate searchable drill picker. All edits persist as a builder draft. **Save Mission** freezes a numbered version; **Start Mission** starts that exact saved version. Resume/finish/discard choices protect unfinished training. Each saved drill identifies its Mission version; a completion receipt lists its exact results and can be reopened from Progress. Planned duration never implies measured elapsed time.

UI/UX Pro Max searches: `basketball workout builder dark --design-system`, `reorder buttons accessible --domain ux`, `LazyColumn state rememberSaveable --stack jetpack-compose`. The system search's Barlow/Barlow Condensed sports typography fits the installed identity. Its marketing-site layout, alternate palette and web hover rules are rejected: native task-focused shared surfaces and the canonical black/orange tokens remain authoritative. Accessible control naming and double-submission prevention apply to move/remove/save/start actions. Stack matches describe a newer Compose release; only established LazyColumn/stable-key and hoisted-state/rememberSaveable APIs supported by the pinned BOM are used. Context7's official Android state-saving, state-hoisting, lists and performance documentation corroborates these established APIs; persistent Mission data lives in the existing durable save, not only rememberSaveable.

Impeccable shape/clarify guidance maps the primary action to the current state and makes consequences explicit. Existing rendered build-7 Court/result captures ground the shared header, smoked surface and foreground controls; they are baseline references, not evidence for this candidate. New normal/compact/large-text/keyboard Android captures and owner acceptance remain required. No new art, font, dependency or signing material is introduced.


Mission Builder native acceptance: source `f6db205d3b8d7a10621acd3b39bc6fda0f576ecc`, app/src `826dc7c4f6eecbd0147b04e2cbc50a107ed4d089`; [run 43147187632](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43147187632) succeeds. The existing Signature art, black-glass surfaces and focused recorder remain. Actual builder, resumed 2/2, recorded 4/5, named next drill, two-drill/nine-attempt summary and reopened receipt captures were reviewed; compact/large text and IME review limits are recorded in [Mission rendered review](docs/tooling/MISSION_BUILDER_RENDERED_REVIEW.md). All same-binary native journeys and Maestro pass; 61 Android unit tests and seven Python checks pass; lint 0 errors/11 warnings. Exact-binary delivery and owner/physical-phone acceptance are separate gates.

Build-8 delivery passed in [publication run 43233289181](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43233289181). The independently downloaded public APK matches the native-tested binary byte for byte and retains version8/package/original certificate/v2 signature/alignment. Sixteen native build-8 captures were added to the existing development release. This completes CQ-PLAYER-002 engineering delivery; owner visual/physical-phone review remains separate.


## CQ-PLAYER-003 — five player destinations (November 12, 2026)

Court / Train / Quests / Progress / Me reuse the Signature vectors, native theme and glass primitives. Train retains the working library; Me summarizes current local identity/routine and opens profile editing. Quests displays today's actual daily results and existing Mission receipts, without adding award rules or claiming proficiency. New achievements remain CQ-PLAYER-004.

UI/UX Pro Max searches: sports/fitness profile design-system matched Barlow Condensed/Barlow and energetic sports hierarchy. Retain the owner-approved black/orange palette instead of the suggested red web palette; web hero/hover guidance is not applied. UX navigation search matched predictable mobile Back. Compose state guidance supports saved UI state; its 1.11.4 examples exceed this app's BOM, so use the already compatible rememberSaveable/SaveableStateHolder APIs. Official Android state-saving and navigation-bar documentation were checked on November 12. Navigation history contains only route IDs; training data remains in durable saves.

Five native NavigationBarItems expose label/selected semantics without duplicate icon descriptions. At 150%+ font scale, the bar uses two rows so all five labels stay reachable and readable. At compact widths the existing condensed face keeps labels legible without shrinking system text. Foreground content stays outside blur. Root Back returns to Court; builder/settings/receipt Back returns to its opener; completed recorder/result routes replace one another. Destination-local filters, scroll and name draft survive tab switching. Native rendered acceptance remains pending until the new emulator captures are reviewed.

Review corrections: retain registries only for player roots and profile editing; transient custom/drill editors use their existing Activity restoration and clear when left. Library drill selection is owned explicitly by the app, preventing a saved revision from reopening an archived drill. Progress filters use saveable state. The local quest date refreshes on resume and at midnight, with 30-second bounds for clock/time-zone changes. Native regressions cover discard→new drill, saved-revision selection, Games filter retention, confirmed process death and receipt Back.

## Build 9 accepted native checkpoint

CQ-PLAYER-003 is implemented and published. App/src `6edca69928e7ff9e66b53a6349f6ab1fcd6190a4`; frozen APK SHA-256 `68b3b1d1f2cd08084b65973ae07544b74c2a795db27994f2b9a2b975a7785c5a`. [Native completion 43251979732](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43251979732) passes 72 unit tests, all five journeys, full 200%-text keyboard Save click and guarded Maestro; lint 0 errors/11 warnings. [Publication 43253825911](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43253825911) and byte-exact unauthenticated public-download verification pass. [Rendered review](docs/tooling/PLAYER_NAVIGATION_RENDERED_REVIEW.md) records normal/320dp-130%/200% captures, two-row enlarged navigation and bounds-verified keyboard interaction. Signature resources and existing saves/signer remain intact. Owner visual acceptance, phone/TalkBack/older-platform/launcher checks remain separate. The current Quest screen displays real activity; versioned award rules are CQ-PLAYER-004.


## Owner correction — low-profile navigation (November 12, 2026)

The owner rejected build 9's enlarged-text two-row bar in phone screenshot 1794499692190.jpeg: all five destinations must fit while the dock stays low profile. This supersedes the earlier two-row fallback. The frozen build-9 emulator reproduces the exact issue at 150% text; the single-row geometry assertion fails on that baseline.

The correction uses a 56dp smoked-glass dock with 4dp outer vertical spacing, one row at every supported text size and 48dp tap targets. Court/Train/Quests/Progress/Me order and routes stay fixed. Inactive destinations display crisp vector icons; the selected destination displays its scalable name in an orange pill, with an icon only when measured text leaves room. Full names and selected Tab semantics remain available to screen readers; native Material3 long-press tooltips name inactive icons. Foreground content stays sharp. Android's system navigation/gesture area is separate from dock height.

Applied UI/UX Pro Max touch/navigation/semantics guidance: preserve generous native targets, understandable names, selected state and Scaffold clearance. The data set's newer Compose APIs are not imported. Existing Material3 tooltip and Foundation selectable/semantics APIs follow official Android documentation and compile against the pinned BOM. No font-scale cap, second row, new dependency or brand asset is introduced. Actual 360dp/100%, 360dp/150%, 320dp/130%, 320dp/200% and landscape captures/bounds are required before delivery.

Baseline reproducibility: build9 is pinned to the immutable successful native-run artifact 43251979732 and SHA-256 68b3b1d1f2cd08084b65973ae07544b74c2a795db27994f2b9a2b975a7785c5a, so replacing the public development APK cannot break future regression runs.

## Approved future navigation direction — Edge Navigation v1 (2026-11-12)

The owner approved replacing the oversized persistent bottom navigation with a compact, **left-edge collapsible navigation rail** based on their hand-drawn sketch. This is a design requirement, not evidence that the Android implementation has changed.

- **Collapsed:** Only a slim, clearly visible pull tab protrudes from the middle-left screen edge. Its visible surface is **bright glowing orange** using the existing saturated CourtQuest orange (`#FF650E`), with a controlled luminous halo, glossy highlight, sharp chevron, and premium smoked-glass material. Keep the touch target accessible despite the narrow visual footprint.
- **Open:** Tapping the tab expands a vertical rail with all five existing destinations in their existing order: Court, Train, Quests, Progress, Me. Preserve current icons, routes, selection semantics and app state; indicate the current tab.
- **Close:** Automatically collapse immediately after a destination is chosen, including the currently selected destination. Also dismiss on outside tap, Back, or closing swipe. Avoid trapping focus and provide clear accessibility labels.
- **Motion:** Short approximately 180–220ms transition; respect reduced-motion settings. Glow should be vivid but localized to the pull tab, not a distracting screen-wide bloom.
- **Placement:** Keep the handle near the middle-left edge while respecting system gesture regions, screen sizes, orientation and active drill/scorekeeper controls. Do not obstruct critical recording actions.
- **Preservation:** Do not redesign other screens, alter Signature branding, change the black/orange palette, erase player data, modify signing, or break existing workflows. Maintain responsive behavior at enlarged text and narrow widths.
- **Acceptance:** Verify collapsed/expanded layout on device, accessible touch target, all five routes, auto-dismiss, outside tap/Back, resume behavior, gesture compatibility, visual glow, and build/lint/tests before an APK is presented as verified.

## v42 player progression screens — November 17, 2026

Progress, Quests, and Me are implemented as native Compose screens, using the existing `#050505` / `#FF5B0A` palette, Barlow typography, smoked-glass surfaces, and original CourtQuest art. Progress has 7-day, 30-day, and all-time filters; summaries count only valid dated saved results, with the all-time activity chart labeled as the last 12 months. XP remains an activity/participation measure, not a skill score. Personal bests require a repeat of the same frozen drill definition, measurement schema, and attempt count.

Daily and weekly quest rewards are versioned and idempotent. A one-time saved activation timestamp marks when new quest rules begin, so prior training remains visible without earning retroactive quest XP. Drafts and abandoned sessions do not count. Achievements are derived from saved records and valid Mission receipts; unverifiable receipts are retained and shown without claiming completion. No team/school/coach route is exposed, and no dormant team data, app identity, signing material, artwork, or saved progress is changed. The views use no new network services or animation, so reduced-motion settings need no special handling.

This pass follows the existing native design and the local Impeccable shape/clarify/critique guidance; a UI/UX Pro Max package was not available in the workspace at implementation time. The existing guarded Android workflow captures empty and returning-player states at normal, compact, and 200% text sizes, and its combined scope exercises saved daily rewards, restart persistence, achievements, and extra-practice regression. No v42 APK is published by this work.

## v48 walkthrough candidate

The v47 release places the guide in a full-window Scaffold content layer while
the higher Scaffold bottom-bar layer owns the dock. The guide therefore needs
the measured content padding even though it floats over each destination.
The new guide and the underlying screen share the same safe content bounds.
The guide is capped at 392 dp wide, separated from the dock, and uses a smoked
surface, five progress segments and one primary action. The footer stays pinned
while the coaching copy scrolls in a short viewport. Actions retain at least
48 dp targets. Enlarged text switches Back to a labeled arrow so Skip and
Next or Finish remain legible. The selected dock artwork keeps the orange
outline. No modal scrim, additional animation library or new artwork is needed.

The visible step follows the active top-level destination. Entering a secondary
screen pauses the guide until the player returns to a destination. Android
Back returns to the prior guided destination or closes the first step. Skip
and Finish share the existing walkthrough-only preference. The other saved
state is owned by the existing application paths.

UI/UX Pro Max mobile onboarding guidance supported a visible Skip action and a
predictable Back action. Its focus-obscured rule is web-labeled, so apply the
same observable geometry to the Android controls without importing web CSS
or a new Compose API. Context7 and the Android Material 3 Scaffold inset
documentation supported constraining both siblings with measured padding.
The local Impeccable onboarding and clarity review supported one instruction
per step, short copy and a single primary action. The Figma draft models all
five destinations and a compact large-text state using editable guide components.
It is a layout reference. The Android screenshots are the visual evidence.
[Walkthrough layout draft](https://www.figma.com/design/JKwMaV5jiJp7qSEmbvZSKa)

The installed Compose Intro Showcase was reviewed for target-centric coach marks.
Native Scaffold padding already supplies the precise dock boundary and the
existing route and tab semantics. No extra runtime dependency is needed.
[Compose Intro Showcase](https://github.com/canopas/compose-intro-showcase)
[Android Scaffold insets](https://developer.android.com/develop/ui/compose/system/material-insets)

The release review uses the exact v47 APK, a guarded Android 35 AVD, and the
same synthetic player fixture. The required evidence includes the v47 overlap,
all five native candidate captures, Back, Next, Skip, Finish, replay, direct
tab selection, 320 dp at 130% and 200% text, Compose semantics and bounds,
original signing identity, and install-over preservation. Unit tests, lint and
the signed build are independent checks.

The [native v48 run](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/44372265881)
passed all five stops at 360 dp/100%, 320 dp/130% and 320 dp/200% text.
Across the 15 reviewed captures the guide cleared the dock by 12 dp, and
the actions retained at least 48 dp targets. Back, Next, Skip, Finish, replay,
direct dock taps and Android Back passed. The first native run exposed a test
synchronization failure after the physical taps and upgrade had passed. The
rerun uses the exact same APK bytes, checks unmerged Compose semantics, and
passes both suites. The frozen v48 APK upgraded the real published v47 APK
and preserved the synthetic player preferences byte-for-byte during installation.
[Verification record](docs/tooling/evidence/walkthrough-v48.json)

Physical phone, TalkBack speech, and landscape review remain separate checks.
The v47 release is the rollback reference until a phone-test release is approved.

## Jersey Studio

The Studio uses the shipped black/orange palette, Barlow family and smoked
surfaces. Identity, Style and Colors share one front/back preview and a fixed
Save jersey action. Color roles are named, swatches have readable accessibility
labels, and the editor reports low lettering contrast in text. Saved uniforms
and draft form state have separate lifecycles. The personal practice uniform
uses the existing profile save, with additive design and double-zero fields.
Older saves retain their numbers and default to the Classic fabric treatment.
Team home and away presets are represented separately by the local domain model.

UI/UX Pro Max matched Barlow/Barlow Condensed and high contrast in its sports
search. Its web layout and spacing do not apply to this native form. Its UX
results support named color controls, contrast warnings and save feedback.
The compatible Compose primitives are Canvas paths, selectable semantics and
`rememberSaveable` with a `listSaver`, using the checked-in BOM. Android's
graphics and accessibility guidance and the existing LL-PES native surface
recipe support reusing the app's own premium surface and touch target patterns.
The Figma walkthrough reference confirms the dock's established dimensions.

### Arena Studio reference candidate

The solo-player Studio [design](https://www.figma.com/design/tgMiLviIw1x8c7Votx1VT8)
uses one large jersey in portrait and matching front and back views on wide
landscape screens. Portrait color editing opens a bottom sheet. The landscape
editor lists all seven color zones, and the style picker has named thumbnails.
Existing saved designs retain their styles and colors. Profiles without a saved
jersey design start with Arena. The existing profile name, number, and save
format still supply the live lettering.

The Arena garment and court plate were authored for CourtQuest Pro. The garment
assets pair photographed luminance with an alpha matte and five cloth-region IDs
for each side. Android applies selected cloth colors to those regions off the
main thread. The two lettering colors remain separate. The older styles continue
to draw with their existing renderer. This adds no runtime dependencies.

UI/UX Pro Max supported a dominant equipment preview, labeled swatches, mobile
touch spacing, and a single scroll container per surface. The provided reference
and existing Barlow black/orange theme determine the colors and typography.
The Android candidate stays on the repository's pinned Compose versions and
uses platform sheet and selectable semantics.

## Jersey Studio material editor

The Studio keeps the photographed garment above the active tool on a phone. Landscape places the tools beside it. Style thumbnails use the live dye palette. Front/back and detail inspection stay on the garment stage. The header owns Save, and the inspector scrolls independently at enlarged text sizes or with the keyboard open. Selected swatches carry a check and an accessible name. The native choices follow the existing Barlow, charcoal and orange system. The Studio uses the Compose selectable and saveable-state APIs available in this repository's pinned BOM.

Each cut has a separate front and back material plate. Arena retains its approved source lighting. Classic uses a scoop neck with straight taped sides, Fastbreak has a split V with curved performance panels, and Retro uses a squared collar with heritage side tape. The five textile zones are lossless material masks. Lettering uses the fabric shadow and is fitted uniformly to measured glyph bounds. A compact print treatment carries the player's actual number and inks into the dock.

## Card Studio v59

The editor gives the collectible 62% of the portrait viewport, with compact wrapped selectors below it. Hair and shoe inspection are explicit zoom actions. A localized, shaded skin surface, registered hair layer and full-foot material use the same athlete coordinate system. The four original material masters and native Barlow faces remain in use. The athlete occupies a 78% high aperture, with a separate edition line and nameplate. At large type the card yields space to the accessible inspector. A native, scrollable player record is available in Card options.

The selected mobile interaction guidance supported a 48 dp target, visible draft status, saved side state and a non-animated way to read the record. The pinned Compose graphics layer handles the flip and the Android animator scale. The collector lettering and reflective metal treatment are specific to the four authored materials. Native Android captures and foreground-only comparisons are produced by the Card Studio workflow.
