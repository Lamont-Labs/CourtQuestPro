# Native premium customization and quality audit

## Scope and unchanged contracts

Work extends the existing Player Card V2 on `dev/player-card-profile-v2`.
No new app, tracking, online feature, dependency or navigation destination is
introduced. Package, signing configuration, version, target SDK, approved logos,
original athlete plates, drill illustrations, solo-player flows and canonical
training persistence remain unchanged. Play release/AAB/declarations are the
separately queued readiness work; this document is not a compliance approval.

## Delivered changes

* **46 avatar choices / seven dimensions**, rather than the previous 30/six.
  Hair: 9; headwear: 5; build: 4; skin: 10; shoe color: 7; equipment: 6;
  footwear construction: 4. Five poses, four anonymous faces and four card
  editions bring the separate choice inventory to **59**.
* Original ready-stance and follow-through athletes, detailed hair materials,
  mesh low-tops, padded high-tops, molded strap shoes and Obsidian Chrome frame.
  Shoe construction is independent of shoe color.
* Pose-specific cloth, skin, print and accessory anchors for all five athletes.
  Existing templates, leading-zero numbers, chest branding and saved colors
  remain live, not painted permanently into the source uniforms.
* Full-resolution Hair/Gear inspections; physical-model thumbnails; larger
  horizontally scrolling card thumbnails; preserved pose on material changes;
  short touch-triggered material reflections with system motion scaling.
* Full-detail maps only for the worn uniform template, bounded body/head caches
  and background-only asset decoding.
* Missing non-action section counts now render; headings expose their role and
  reflow instead of squeezing titles at enlarged text. Settings subpage Back
  returns to All settings. The Quests hero no longer fixes enlarged content to
  112dp. Custom jersey copy describes chest text rather than a team experience,
  without renaming or dropping the saved field.
* Me sizes the collectible to the actual available viewport. Compact/enlarged
  layouts keep its Studio action reachable initially; landscape places the
  compact identity card beside its readable label/action and folds tabs into
  the heading. No duplicate avatar or second profile identity is restored.
* Native screenshot review found floating/cropped generic shoe sprites and
  artificial hair-cap seams. Hair now uses 45 pose-matched transparent head
  plates; shoes use 15 pose/model cutouts at authored source coordinates, with
  no generic runtime resize or rotation. The alternate base clears only the
  shoe silhouette plus its one-pixel antialias edge, preserving socks and
  ankles. The skin/shoe recolor matrix now preserves each source pixel's
  luminance and texture.

## Whole-app review and repeatable evidence

Source review uses the approved design tokens, existing Compose behavior and
bundled Impeccable critique. UI/UX Pro Max was not available. Runtime gates below
are implemented in the existing candidate workflow, not duplicated workflows.
Their **actual pass/fail** is the exact-commit Actions outcome and attached logs,
not the existence of this checklist.

| Destination / behavior | Native evidence or check |
|---|---|
| Welcome, five-stop onboarding guide | Real-frame welcome/guide captures; NavigationTourTest; existing v42 guide-next/back/skip and unchanged-data assertions |
| Court and Train first viewport | Paired baseline/candidate captures; focus/start/resume; compact dock and readable plan assertions |
| Drill Library, detail, artwork | Search, detail, actual art zoom, existing drill references and launch controls |
| Recorder and receipts | Real attempts, Undo, save-once receipt, extra practice, restart, no duplicate records/rewards |
| Progress, skills, history and milestones | Empty/returning/completed-player journeys against persisted training evidence; milestone counts shown in Me |
| Daily/weekly Quests and achievements | Existing v42 completed-drill/quest/unlock assertions and captures |
| Me, Player Card Studio | Primary live selected card; all 80 edition/pose/face combinations per display configuration |
| Hair / Body / Gear | Every option on every pose at normal size; selected combined options on compact, landscape and 200% text |
| Jersey Studio | Existing customization/recreation/cold-start test; template/number/color sync without card re-save |
| Settings and preferences | Player, preferences, help, how-to, about and notices captures; hardware Back from a subpage |
| Custom drill editor and Mission builder | Existing v42 reachability and resume journey; no replacement custom-training model |
| Solo 1-on-1 score/history | Existing real scoring, Undo, opponent win, repeat-game no-extra-XP, restart and daily-reset assertions |

`PremiumJourneyAuditTest` captures the same routes against the pinned verified
baseline and candidate at normal 720×1600, compact 640×1280/130% text,
640×1280/200% text and landscape 1280×720, density 320. Baseline and candidate
app captures use the same disposable saved identity and training fixture.
Hardware Back and formerly missing count assertions apply to the repaired
candidate; baseline uses its existing on-screen Back control.

`PlayerCardStudioTest` hashes **isolated artwork pixels**, not option controls,
to reject invisible or duplicate customization choices. It records decoded
memory diagnostics and checks the 48 MiB cache bound. Legacy six-dimension
cosmetics are restored from their original saved keys, upgraded atomically
with only the new footwear default, and checked after recreation/cold start
without changing canonical training records.
An additional normal-size matrix saves all four real Jersey Studio templates,
renders each on all five athletes, requires distinct worn-uniform pixels for
each pose and preserves non-jersey player/training data and card selections.

The build gate runs JVM tests and Android lint before building. Python checks
validate source digests, real alpha, dimensions, garment/skin separation,
five athlete plates, 20 template maps and distinct footwear silhouettes.
APK identity/signature and in-place v56-to-candidate data checks remain in the
same guarded disposable emulator pipeline. The prior verified v57 is also
installed in place for matched before/after and cosmetic migration testing.
The v56 fixture explicitly includes its existing `DrillResult.practice=false`
primitive default. Previously a sparse fixture passed strict equality only
before v56's asynchronous reserialization added that field. Strict value/list
equality remains unchanged; timeout evidence now names the actual differences.

## Evidence locations

Existing Actions candidate artifacts contain `before/<configuration>/` and
`<configuration>/` app PNGs, the 80-combination galleries, per-option PNGs,
`avatar-rendered-differences.json`, `avatar-memory.json`, cosmetic-migration,
deep-journey, game-journey and original install-over evidence. The APK is
uploaded only if the workflow succeeds. PR #9 records verified run URLs.

## Honest limitations

This is an authored layered **2D** character system, not a 3D skeleton or
physical cloth/shoe simulation. Footwear is fitted to per-pose regions and
garments adapt the saved template to the illustrated perspective. Choices
are cosmetic, not measurements or claims about basketball performance.
Automated rendered evidence does not replace physical-phone GPU, battery,
TalkBack usability or prolonged-session testing. The entire first-run account/
setup wizard is source-reviewed; the runtime journey focuses on the actual
welcome, solo guide, established training paths and settings rather than
claiming every possible setup input combination was tested. No release,
Play approval, final AAB or physical-device verification is claimed here.
