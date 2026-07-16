# CLAUDE.md

Nextcloud Android **library** — the networking/SDK layer (`RemoteOperation`s against Nextcloud/OCS
APIs) consumed by the Nextcloud Android app. Mixed Java + Kotlin; new code is Kotlin.

## Read first

**[AGENTS.md](AGENTS.md) is the source of truth for adding or changing network endpoints** — package
layout, the `RemoteOperation` template, error handling, serialization, constants, and the detekt
thresholds that gate CI. Follow it for any endpoint work. The canonical reference implementation is
`library/src/main/java/com/nextcloud/android/lib/resources/governance/`.

## Where things live

- Remote operations: `library/src/main/java/com/nextcloud/android/lib/resources/<feature>/`
- Wire models/enums: `…/resources/<feature>/model/`
- Shared OCS types: `com.owncloud.android.lib.ocs` — `OcsResponse<T>` / `Ocs<T>` envelope,
  `ocsJson` (strict `Json`), `SEPARATOR` (`"/"`)
- Unit tests: `library/src/test/java/…` &nbsp;·&nbsp; instrumented tests: `library/src/androidTest/java/…`

## Commands

```bash
./gradlew :library:compileDebugKotlin
./gradlew :library:testDebugUnitTest --tests "com.nextcloud.android.lib.resources.governance.*"
./gradlew :library:detekt
```

## Gotchas

- **kotlinx.serialization, not Gson.** The compiler plugin is applied in `library/build.gradle`; the
  `kotlinx-serialization-json` runtime dependency alone is not enough — both are required.
- **detekt (`library/detekt.yml`) is strict on operations:** `ReturnCount: max 2`,
  `TooGenericExceptionCaught` active (needs `@Suppress` on `run()`), `ThrowsCount: max 2`. AGENTS.md
  shows the two-return / single-catch pattern that satisfies these.
- **Line length 120** (`.editorconfig`); import ordering is disabled — don't reorder imports.
- **`:library:spotlessKotlinCheck` currently fails at configuration time** (a spotbugs/AGP issue,
  not your code). Verify Kotlin formatting by hand against the 120-char limit and the AGENTS.md patterns.
- **`RemoteOperationResult.logMessage`** is a deprecated Java accessor — suppress at the call site
  only (`@Suppress("DEPRECATION")` on the `failure(...)` helper), never globally.

## House rules

- Kotlin only for new classes; don't add Java classes. Java 17 is available for editing existing Java.
- Keep diffs minimal and focused; don't reformat or refactor unrelated code.
- Only commit or push when asked.
