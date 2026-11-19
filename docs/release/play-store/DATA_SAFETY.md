# CourtQuest Pro — candidate data inventory

| Data or access | Processing | Transmission or independent service |
| --- | --- | --- |
| Optional player name, jersey number, jersey style, and color choices | Private app SharedPreferences profile/state. | No source-level upload or sharing path found. |
| Difficulty, training focus, routine days, drill favorites, custom drills and missions | Private device state, used to build practice guides and plans. | No source-level upload or sharing path found. |
| Drill attempts, results, dates, XP, quests, scores and match nicknames | Private device state, used for progress, history and recommendations. | No source-level upload or sharing path found. |
| In-progress drill, mission and score state | Private device state for pause/resume. | No source-level upload or sharing path found. |
| Optional demonstration query | Launches an external Android VIEW intent to a YouTube search built from a bundled drill phrase or an allowlisted skill category, without a profile or history payload. | The browser/video provider receives the URL and handles network/account data according to its own policy. |
| Contact, precise location, camera, microphone, files and device advertising identifiers | No active first-party access path identified. | No advertising, analytics or publisher crash telemetry added. Final merged manifest and dependency tree are separate gates. |
| Account, cloud sync and Android app backup | No CourtQuest account or sync. Backup disabled in the manifest. | No Lamont Labs account records to delete. |
| Google Play distribution | Google Play handles installation, update, and its own store account. | Owner must check Play services and any configuration outside source. |

The draft assessment is that profile and training records are on-device processing, with no identified direct first-party collection or sharing. Confirm the exact build, all uploaded variants, the external handoff, and any owner/operator practices before selecting the Data Safety declarations. The declaration must match actual behavior. Android clearing storage or uninstall removes local data. Current source does not provide cloud recovery.

[Google Play Data Safety guidance](https://support.google.com/googleplay/android-developer/answer/10787469) · [Google Play User Data policy](https://support.google.com/googleplay/android-developer/answer/10144311)
