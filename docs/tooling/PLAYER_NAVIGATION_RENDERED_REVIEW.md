# Player navigation build 9 — rendered Android review

Runtime app/src: `6edca69928e7ff9e66b53a6349f6ab1fcd6190a4`. Frozen APK SHA-256: `68b3b1d1f2cd08084b65973ae07544b74c2a795db27994f2b9a2b975a7785c5a`. Completion [run 43251979732](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43251979732), artifact 13960571780. Original completed daily/library/Mission/navigation evidence is retained from [run 43244651522](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43244651522); that run was cancelled at its 35-minute limit, never reclassified as successful. Completion lineage and all report hashes were checked independently after artifact download.

## Native observations

- Normal 360dp: all five labels are readable in the shared smoked-glass bar. Train exposes the existing library/builder controls; Quests puts a real daily action and recorded count above its checklist; Me displays the actual saved identity/routine and profile action.
- 320dp / 130%: Me identity and Edit profile remain visible; five condensed labels remain distinct. Long profile/focus copy wraps without truncating controls.
- 320dp / 200%: navigation uses two rows, with all five destinations visible. Large headings and profile content require ordinary scrolling; the native journey reaches Edit profile and all destinations. Recording/game screens keep their focused controls.
- Keyboard / 200%: an initial partial capture only exposed part of Save name. The stronger completion test scrolls the entire enabled clickable button above the IME, verifies its bounds against the scroll viewport, taps it and verifies the saved name. The actual full-button capture was viewed and shows a complete bright-orange Save name control above the keyboard. No runtime source change was needed.
- Saved-state checks prove library filters, profile draft and Progress selection survive tab changes; actual process death/restore retains the name draft. Discarded custom-editor data does not return in a new editor. A saved drill revision opens the new definition while queued/history definitions remain frozen.
- Mission and daily receipts return to their owning screen; completed recorders are replaced. Legacy daily/scoring/Undo and the build-5 update retain saved data. Every accepted report names the same APK hash.

## Applied review

Native Impeccable review: task hierarchy and visible next action, clear save feedback, recognisable labels, reversible local editing, retained drafts, plain evidence wording, consistent material/type/color, scrolling/insets and enlarged text were reviewed against real captures. UI/UX Pro Max mobile Back/state and sports typography guidance was adapted to the pinned Compose stack; web palettes/motion were not imported. Signature resources and signing files were unchanged. No browser render was used as native acceptance.

## Limits

Owner visual acceptance and physical-phone performance remain separate. Full TalkBack traversal, Android26–30 glass fallback and alternate launcher masks remain unverified. Minor deferred: the recording-screen Back wording can still say Court even when actual routing returns to Train or the Mission Builder. New Quest achievement rules and school/server capabilities are future issues.
