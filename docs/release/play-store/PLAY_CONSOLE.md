# CourtQuest Pro — Play Console submission worksheet

## Owner-confirmed fields

| Console item | Source-supported draft | Owner confirmation |
| --- | --- | --- |
| Listing contact | Lamont Labs publisher name, public support email required | Supply the monitored public support email and confirm the publisher/contact identity. |
| Privacy policy | In-app disclosure plus a separate public policy required | Approve the draft, confirm effective date/entity, and provide the live public URL. |
| App access | All active solo features are available without a login. | Confirm no hidden server, credentials, or regional access gates. |
| Ads | Source has no ads, analytics, tracking, or billing integration. | Reconfirm after reviewing the exact uploaded build and SDK inventory. |
| Target audience | Basketball practice guidance is relevant to teenage and adult players. | Select actual intended ages. Review Families requirements, discovery assets, and the optional YouTube handoff before including any relevant child age groups. |
| Content rating | Solo sports training and local scorekeeping. No in-app messaging, public UGC, cash stakes, commerce, or accounts. YouTube links leave the app. | Answer the current IARC questionnaire and disclose external access where asked. Google assigns the rating. |
| Account deletion | There is no app account. Local records can be erased by clearing storage or uninstalling. | Declare based on the exact submitted build and any operator-side services. |
| Permissions | Candidate removes direct Internet permission and app backup. Final manifest gate checks exported components and permissions. | Confirm the final merged-manifest receipt before answering. |
| Data Safety | Use the separate inventory worksheet. | Confirm source and final build, all distribution variants, publisher practices, and external-video treatment before submitting. |
| Testing eligibility | Account type/creation date were unavailable. | Check Dashboard for identity/device verification and production track requirements. |

## Audience and schools

The solo release has no school roster, coach portal, account, cloud, or student record disclosure. A child-directed Play listing still needs a Families review of the in-app content, imagery, external demonstration results, and every included SDK. Choose age groups based on intended audience and presentation. For future U.S. school use, review COPPA if an online service collects personal information from children under 13. A school authorization has limited educational use conditions. Plan any disclosure of school-maintained education records around the applicable FERPA route and district approval, data-use restrictions, access control, and retention.

[Google Play Families policy](https://support.google.com/googleplay/android-developer/answer/9893335) · [FTC COPPA guidance for schools](https://www.ftc.gov/business-guidance/resources/complying-coppa-frequently-asked-questions) · [U.S. Department of Education FERPA vendor guidance](https://studentprivacy.ed.gov/faq/who-school-official-under-ferpa)

## Signing continuity

The GitHub v54 APK is signed by a public Android Debug key. CourtQuest Pro can continue same-signature GitHub phone updates, such as the v55 development APK. Keep that channel available to existing testers who need their saves.

The Play release AAB must use an owner-authorized upload signer. Check **Test and release → Setup → App signing** (also available under **Protected with Play → Play Store protection**) for existing record and certificate fingerprints. If this package is new to Play, decide on Play App Signing with a protected distribution key. A Play-signed install using a different certificate cannot update the existing debug install in place. Confirm a tested migration strategy before asking existing phone testers to move channels. Android's Settings clear-data or uninstall actions erase local progress unless it has been migrated separately. Do not use the public debug certificate as an upload or app signing identity.

### Build an upload bundle after signer approval

Configure the authorized upload key in secure build settings. The GitHub candidate defines `COURTQUEST_PLAY_KEYSTORE_BASE64`, `COURTQUEST_PLAY_STORE_PASSWORD`, `COURTQUEST_PLAY_KEY_ALIAS`, and `COURTQUEST_PLAY_KEY_PASSWORD` as secrets and `COURTQUEST_PLAY_UPLOAD_CERT_SHA256` as a public certificate fingerprint variable. GitHub manual runs require the workflow to be available from the default branch. Its signing option creates and validates an artifact without uploading it to Play.

For a local build, the repository reads `COURTQUEST_UPLOAD_KEYSTORE`, `COURTQUEST_UPLOAD_STORE_PASSWORD`, `COURTQUEST_UPLOAD_KEY_ALIAS`, and `COURTQUEST_UPLOAD_KEY_PASSWORD` from the approved signing environment. Run `./gradlew :app:bundleRelease` and use `scripts/verify_play_bundle.py` with the registered upload certificate fingerprint. Retain the package, version, build SHA-256, upload certificate receipt, and the separate Play app signing certificate receipt.

## Internal, closed, and production tracks

1. Enter Play Console under the Lamont Labs account. Confirm the account, package `com.courtquestpro`, availability of version code 55, and verified upload/app signing fingerprints before uploading.
2. Add the approved privacy URL, required public support contact, listing copy, icon, feature graphic, real screenshots and owner-approved App Content responses.
3. Create an internal testing draft under **Test and release → Testing → Internal testing** and attach the signed candidate AAB. Compare package, version, signing, warnings, permissions, and device coverage against its build receipt.
4. Use the reviewer script below. Collect issues and run Play's pre-launch report on the Play-distributed build. Confirm same-channel updates retain data.
5. When required for the account, create a closed testing release and recruit at least 12 opted-in participants for 14 continuous days. Applicable personal accounts are those created after November 13, 2023. Apply for production access after meeting the console requirement.
6. Resolve pre-launch defects, app content warnings, signing/migration gaps, listing omissions, and any unresolved age-targeting issue. Prepare the production draft with the same reviewed AAB where eligible. Account owner approves all binding declarations and public submission.

## Reviewer and tester instructions

Open the app and tap **Get Started**. Enter a fictional name, select your level and focus, and choose your training days. Complete or skip the walkthrough. Open **Train**, choose a drill, record attempts, leave and resume, then save one result and confirm it appears in **Progress**. Visit **Quests**. In **Me → Jersey Studio**, preview all four styles, change the number and fabric, view the back, save, then reopen the app. Confirm Court and the Me dock show the saved jersey. Repeat a core drill in airplane mode. Repeat with larger system text and look for truncated controls. No reviewer account or credentials are required.

On a device already used for the GitHub phone-test build, use only a verified same-certificate candidate for install-over testing. Keep the original installation and its data until the Play migration decision is made.

[Play App Signing](https://support.google.com/googleplay/android-developer/answer/9842756) · [Play testing tracks](https://support.google.com/googleplay/android-developer/answer/9845334) · [New personal developer accounts](https://support.google.com/googleplay/android-developer/answer/14151465) · [Target audience](https://support.google.com/googleplay/android-developer/answer/9867159)
