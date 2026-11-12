<!--
 ~ SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 ~ SPDX-License-Identifier: MIT
-->
# CLAUDE.md

Nextcloud Android **library** — the networking/SDK layer (`RemoteOperation`s against Nextcloud/OCS
APIs) consumed by the Nextcloud Android app. Mixed Java + Kotlin; new code is Kotlin.

## Read first

**[AGENTS.md](AGENTS.md) is the source of truth for adding or changing network endpoints** — package
layout, the `RemoteOperation` template, error handling, serialization, constants, and the detekt
thresholds that gate CI. Follow it for any endpoint work. The canonical reference implementation is
`library/src/main/java/com/nextcloud/android/lib/resources/teams/`, built on the base class
`com.nextcloud.operations.OcsKotlinRemoteOperation`. Use the `add-remote-endpoint` skill for new endpoints.

## Where things live

- Remote operations: `library/src/main/java/com/nextcloud/android/lib/resources/<feature>/<verb>/<subfeature>/`
- Wire models/enums: `…/<verb>/<subfeature>/model/` when only that operation uses them,
  `…/<feature>/model/` when shared
- Shared OCS types: `com.owncloud.android.lib.ocs` — `OcsKotlinResponse<T>` envelope,
  `ocsJson` (strict `Json`), `SEPARATOR` (`"/"`)
- Integration tests (one `*IT` per operation, mirrored packages): `library/src/androidTest/java/…`

## Commands

```bash
./gradlew :library:compileDebugKotlin
./gradlew :library:compileDebugAndroidTestKotlin
./gradlew :library:detekt          # with spotlessKotlinCheck + lint: once, at the end, after review
```

## Gotchas

- **kotlinx.serialization, not Gson.** The compiler plugin is applied in `library/build.gradle`; the
  `kotlinx-serialization-json` runtime dependency alone is not enough — both are required.
- **detekt (`library/detekt.yml`) is strict on operations:** `ReturnCount: max 2`,
  `TooGenericExceptionCaught` active (needs `@Suppress` on `run()`), `ThrowsCount: max 2`.
  `OcsKotlinRemoteOperation` already satisfies these — never hand-write `run()` in an operation.
- **Line length 120** (`.editorconfig`); import ordering is disabled — don't reorder imports.
- **`:library:spotlessKotlinCheck` currently fails at configuration time** (a spotbugs/AGP issue,
  not your code). Verify Kotlin formatting by hand against the 120-char limit and the AGENTS.md patterns.
- **`RemoteOperationResult.logMessage`** is a deprecated Java accessor — suppress at the call site
  only (`@Suppress("DEPRECATION")` on the `failure(...)` helper), never globally.

## House rules

- Kotlin only for new classes; don't add Java classes. Java 17 is available for editing existing Java.
- Keep diffs minimal and focused; don't reformat or refactor unrelated code.
- Implement only the calls the client flow needs; don't touch workflows or `NextcloudVersion` as a side effect.
- No comments or KDoc in new code; no JSON-parsing unit tests (integration tests only).
- Only commit or push when asked.
