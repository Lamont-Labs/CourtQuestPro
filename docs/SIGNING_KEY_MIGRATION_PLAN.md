# CourtQuest Pro — signing key migration gate

Status: BLOCKED pending secure backup and repository secret configuration. Do not make this repository public.

## Verified current state

- The existing development APK is signed using `signing/debug.keystore`.
- `app/build.gradle.kts` currently references that tracked keystore and includes debug signing credentials.
- The tracked keystore was added in commit `395c2bbb12d4d7a203178e4c54b9f67b5dd4f54e` (2026-11-01).
- The `.gitignore` exception permitting the keystore was removed in commit `7b9cd808945014ba86cad7363b2aed3025595479`, but the file remains tracked.
- A normal deletion does not remove historical copies.

## Required gates before any removal

1. Preserve the exact existing keystore in a secure, independently accessible backup outside the repository. Do not generate a replacement.
2. Verify the backup can be opened and record only the public signing certificate fingerprint. Never print or commit private key bytes, passwords, or base64-encoded keystores.
3. Configure encrypted repository Actions secrets for the preserved keystore and passwords using GitHub's authenticated settings interface.
4. Update Gradle and Actions to consume those secrets, without changing the application ID or signing certificate.
5. Build and verify a signed APK: same package ID, matching signing certificate, incremented version code, and successful update installation on the existing phone.
6. Only after the above verification, remove the tracked key and embedded passwords from the current tree.
7. Before public visibility, purge sensitive key material from *all* Git history, tags, forks and release assets as applicable; rotate any other exposed credentials; confirm the remote has no reachable copy. Coordinate history rewrite and force-push impacts.
8. Independently review proprietary source and third-party asset licensing before making the repository public.

## Fail-closed rules

- No deletion of the current keystore until a verified backup and replacement signing path exist.
- No temporary or regenerated signing keys.
- No publication of the repository while historical key exposure remains.
- No assumption that `.gitignore` removes tracked or historical files.
- No claims that a GitHub secret has been configured without verification.
