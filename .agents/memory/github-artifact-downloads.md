---
name: Downloading GitHub evidence through the workspace proxy
description: Read GitHub Actions artifact ZIPs without bypassing the connected GitHub proxy.
---

Use the relative API endpoint with `gh api repos/OWNER/REPO/actions/artifacts/ARTIFACT_ID/zip`
when downloading Actions evidence in this workspace.

**Why:** `gh run download` followed an absolute `api.github.com` artifact URL and returned
HTTP 401 despite working repository, log and metadata access. The relative `gh api` ZIP
request used the existing connected proxy correctly and succeeded. This was not evidence
that the repository needed new credentials or permissions.

**How to apply:** Save the binary output to an ignored artifact directory and extract the ZIP.
Do not print, retrieve or replace tokens, and do not treat a failed absolute-URL download as
an Android verification failure.
