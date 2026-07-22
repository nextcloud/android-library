<!--
 ~ SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 ~ SPDX-License-Identifier: MIT
-->

# AGENTS.md

This file provides guidance to AI agents (Claude, Codex, Gemini, etc.) working with the
`nextcloud-android-library` repository. **This library only does networking** — it has no UI, no screens, and no
persistence layer.

## Your Role

You implement and change **remote/network endpoints** — the `RemoteOperation`s that talk to the
Nextcloud/OCS APIs/WebDAV APIs and that the client apps consumes. Write documentation and code that a
less-experienced contributor can follow. Fix bugs and add endpoints without inventing new patterns
where an existing one fits.

## Project Overview

`nextcloud-android-library` is the networking/SDK layer for the Nextcloud Android client. It issues
HTTP requests against Nextcloud Server, OCS APIs and WebDAV APIs (de)serializes the responses, and returns typed
`RemoteOperationResult<T>` values. It is a mixed **Java + Kotlin** codebase; **all new code is
Kotlin**. There is no user-facing surface here — no Activities, no Compose, no XML layouts, no
Material Design concerns. Everything is request → response → typed result.

## Project Structure: AI Agent Handling Guidelines

- **Legacy package (`owncloud`):** `library/src/main/java/com/owncloud/android/lib/`
  - Shared OCS types live here: `com.owncloud.android.lib.ocs` — `OcsResponse<T>` / `Ocs<T>`
    envelope, `ocsJson` (the strict shared `Json`), `SEPARATOR` (`"/"`).
- **Modern package (`nextcloud`):** `library/src/main/java/com/nextcloud/android/lib/`
  - Remote operations: `…/resources/<feature>/`
  - Wire models/enums: `…/resources/<feature>/model/`
- **Canonical reference implementation:**
  `library/src/main/java/com/nextcloud/android/lib/resources/governance/`
- **Tests:**
  - Unit tests: `library/src/test/java/…`
  - Instrumented / integration tests (real API calls): `library/src/androidTest/java/…`

**Before writing anything, read an existing endpoint and copy its shape.** Copy the *architecture*,
not the text. Do not invent new patterns unless a requirement genuinely cannot be met with the
existing one.

## General Guidance

- **One public type per source file.**
- **License header** on every new file (this library is MIT):

  ```kotlin
  /*
   * Nextcloud Android Library
   *
   * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
   * SPDX-License-Identifier: MIT
   */
  ```

- Keep diffs minimal and focused; do not reformat or refactor unrelated code.

## Language

- **Kotlin only** for new code. Do **not** create new Java classes. Existing Java may be edited only
  when required for compatibility (the project targets Java 17).
- Prefer `data class`, `val` over `var`, nullable types (`?`), expression bodies, and stdlib utilities.
- Do **not** translate Java idioms into Kotlin (manual getters/setters, needless mutable state).

## Serialization

- Use **`kotlinx.serialization`** for all (de)serialization. Do **not** add Gson usage, converters,
  or `@SerializedName`.
- The compiler plugin is already applied in `library/build.gradle`
  (`org.jetbrains.kotlin.plugin.serialization`). Every model that crosses the wire is `@Serializable`.
- Enum wire values use `@SerialName`.
- Decode with the shared strict parser, never a new `Json { }`:

```kotlin
import com.owncloud.android.lib.ocs.OcsResponse   // { "ocs": { "data": T } } envelope
import com.owncloud.android.lib.ocs.ocsJson        // Json { ignoreUnknownKeys = true }

val response = ocsJson.decodeFromString<OcsResponse<EntityLabels>>(method.getResponseBodyAsString())
val data = response.ocs.data
```

`ocsJson` is strict on purpose (fail fast): unknown keys are ignored, but a missing/mismatched
required field throws instead of yielding a half-populated object.

## Package structure

New endpoints live under `com.nextcloud.android.lib.resources.<feature>`. **Models used by remote
operations go in a `<feature>.model` subpackage** — never beside the operations, never in an unrelated
package. Reusable, cross-endpoint types (the OCS envelope, `ocsJson`, `SEPARATOR`) live in
`com.owncloud.android.lib.ocs`.

```
resources/governance/
 ├── GetEntityLabelsRemoteOperation.kt      # operations
 ├── SetLabelRemoteOperation.kt
 ├── …
 └── model/                                 # data classes + enums
     ├── EntityLabels.kt
     ├── HoldLabelInfo.kt
     ├── LabelType.kt
     └── …
```

Create dedicated `Request` / `Response` / model / enum classes when appropriate. Do not reuse
unrelated models just to avoid a new file, and do not expose internal models as API contracts.

## Remote operation pattern

Every operation extends `OCSRemoteOperation<T>` and follows this exact shape (this is
`GetAvailableHoldLabelsRemoteOperation`, the canonical template):

```kotlin
package com.nextcloud.android.lib.resources.governance

import com.nextcloud.android.lib.resources.governance.model.HoldLabelInfo
import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.GetMethod
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.ocs.OcsResponse
import com.owncloud.android.lib.ocs.SEPARATOR
import com.owncloud.android.lib.ocs.ocsJson
import com.owncloud.android.lib.resources.OCSRemoteOperation
import org.apache.commons.httpclient.HttpStatus

class GetAvailableHoldLabelsRemoteOperation(
    private val entityType: String,
    private val entityId: Long
) : OCSRemoteOperation<List<HoldLabelInfo>>() {
    @Suppress("TooGenericExceptionCaught")
    override fun run(client: NextcloudClient): RemoteOperationResult<List<HoldLabelInfo>> {
        val getMethod =
            GetMethod(
                client.baseUri.toString() + ENDPOINT + entityType + SEPARATOR + entityId +
                    HOLD_AVAILABLE + JSON_FORMAT,
                true
            )
        return try {
            val status = client.execute(getMethod)
            if (status != HttpStatus.SC_OK) {
                return RemoteOperationResult(false, getMethod)   // guard clause — fail fast
            }
            val response = ocsJson.decodeFromString<OcsResponse<List<HoldLabelInfo>>>(
                getMethod.getResponseBodyAsString()
            )
            val data = response.ocs.data
            RemoteOperationResult<List<HoldLabelInfo>>(true, getMethod).apply { resultData = data }
        } catch (e: Exception) {
            failure(e)
        } finally {
            getMethod.releaseConnection()
        }
    }

    @Suppress("DEPRECATION")
    private fun failure(e: Exception): RemoteOperationResult<List<HoldLabelInfo>> =
        RemoteOperationResult<List<HoldLabelInfo>>(e).also {
            Log_OC.e(TAG, "Get available hold labels failed: " + it.logMessage, it.exception)
        }

    companion object {
        private val TAG = GetAvailableHoldLabelsRemoteOperation::class.java.simpleName
        private const val ENDPOINT = "/ocs/v2.php/apps/governance/v1/labels/"
        private const val HOLD_AVAILABLE = "/hold/available"
    }
}
```

Why it is written this way — keep all of it:

- **Fail fast, read top to bottom.** A guard clause returns early on non-OK status. No nested
  `if/else`, no `else` branch.
- **Return count ≤ 2.** The structure has exactly two `return` keywords: the guard `return` and the
  outer `return try`. The success value and `failure(e)` are *expressions* of the `try`/`catch`, not
  extra returns. Do not add a third `return` (detekt `ReturnCount: max: 2`).
- **One catch.** Catch `Exception` once and delegate to `failure(...)`; do not stack
  `catch (IOException)` + `catch (Exception)`. Add `@Suppress("TooGenericExceptionCaught")` on `run()`.
  Split into a specific catch only when an exception genuinely needs different handling.
- **`finally { method.releaseConnection() }`** always runs, including on the early `return`.
- **Property access**, not setters: `apply { resultData = data }` — `RemoteOperationResult` exposes
  `resultData` as a Kotlin property. Use a `set…()` method only when Java interop / an existing API
  contract forces it.
- **`failure(...)` helper** carries `@Suppress("DEPRECATION")` because `RemoteOperationResult.logMessage`
  is a deprecated Java accessor. Suppress at the usage only — never globally.

## Constants — no hardcoded strings

- Path separators: reuse the shared `com.owncloud.android.lib.ocs.SEPARATOR` (`"/"`). Never inline `"/"`.
- Endpoint base paths and per-endpoint path segments: `private const val` in the operation's
  `companion object` (`ENDPOINT`, `HOLD_AVAILABLE`, …).
- The same applies to parameter keys, headers, and repeated log/event strings.

## Class design

Class names describe responsibility (`GetAvailableHoldLabelsRemoteOperation`). **Do not add class-level
KDoc that just restates the name.** Comment only non-obvious business logic, deliberate decisions, or
external limitations.

## Testing

### Unit tests

Small, isolated tests with no live server. Add them beside the existing ones in
`library/src/test/java/com/nextcloud/android/lib/resources/<feature>/`, mirroring the governance
parsing tests — feed a sample OCS JSON string through `ocsJson.decodeFromString<OcsResponse<…>>(json)`
and assert the decoded fields.

```bash
./gradlew :library:testDebugUnitTest --tests "com.nextcloud.android.lib.resources.<feature>.*"
```

### Integration / end-to-end tests

Also add an integration test that makes the actual API call**. Add an instrumented test under
`library/src/androidTest/java/com/nextcloud/android/lib/resources/<feature>/` that runs the operation
against a live Nextcloud server and asserts the `RemoteOperationResult` (`isSuccess`, `resultData`,
error codes). Mirror the existing `*IT` tests for server setup and teardown.

## Build & verify

CI runs three checks in parallel — **`detekt`, `spotlessKotlinCheck`, and `lint`** (see
`.github/workflows/check.yml`). Run the same ones locally, plus compile and tests, before finishing.
All must pass.

```bash
./gradlew :library:compileDebugKotlin                                   # compiles
./gradlew :library:testDebugUnitTest --tests "com.<pkg>.*"             # unit tests for your package
./gradlew :library:detekt                                              # static analysis (see thresholds below)
./gradlew :library:spotlessKotlinCheck                                 # ktlint formatting
./gradlew :library:lint                                                # Android lint
```

- **detekt** config: `library/detekt.yml`. The rules that bite endpoint code:
  - `ReturnCount: max: 2` — a `run()` may contain at most **two** `return` keywords.
  - `TooGenericExceptionCaught: active` — catching `Exception` requires `@Suppress("TooGenericExceptionCaught")`.
  - `ThrowsCount: max: 2`.
- **Line length: 120** (`.editorconfig`). Import ordering is disabled — do not reorder imports to "fix" lint.
- **Formatting** is ktlint via spotless. `:library:spotlessKotlinCheck` may fail at *configuration*
  time locally (a spotbugs/AGP incompatibility unrelated to your code); it still runs in CI, so
  verify formatting by hand against the 120-char limit and the patterns above when it can't run.

## Nextcloud Contribution Policy

Follow the
[Nextcloud AI contribution policy](https://github.com/nextcloud/.github/blob/master/CONTRIBUTING.md).

**Must do:**

- Add an `Assisted-by: AGENT_NAME:MODEL_VERSION` git trailer when an agent contributed to a commit.
- Disclose AI use in the PR description.
- Keep pull requests focused and scoped.
- Verify any dependency against its actual registry before adding it.
- Explicitly warn the human when a change would violate policy, and when a PR is getting large.

**Must never do:**

- Submit contributions autonomously or open/merge PRs without a human in the loop.
- Add `Signed-off-by` tags on behalf of a human.
- Generate security reports without human verification.
- Write PR descriptions as if you were the contributor.
- Submit unreviewed code.

## Commit and Pull Request Guidelines

- Only commit or push **when asked**. If you are on the default branch, create a branch first.
- Sign commits (`git commit -s`) and follow Conventional Commits v1.0.0 (e.g.
  `feat(governance): add hold-label endpoint`).
- Reference the relevant issue in the PR body (e.g. "Closes #123").

## Definition of done

- [ ] No Gson introduced; all wire models `@Serializable`, decoded via `ocsJson` + `OcsResponse<T>`.
- [ ] No new Java classes.
- [ ] Models/enums under `<feature>.model`; reusable types in `com.owncloud.android.lib.ocs`.
- [ ] `run()` is a guard-clause + single `return try/catch/finally`; ≤ 2 returns; no nested if/else.
- [ ] Exactly one `catch (e: Exception)` → `failure(e)`, with `@Suppress("TooGenericExceptionCaught")`.
- [ ] No hardcoded `"/"` or endpoint strings; constants used.
- [ ] `resultData = data` (property), not a setter, unless interop demands otherwise.
- [ ] No name-restating class comments.
- [ ] Lines ≤ 120.
- [ ] Unit tests **and** an integration test (`*IT`) added.
- [ ] `compileDebugKotlin`, `testDebugUnitTest`, `detekt`, `spotlessKotlinCheck`, and `lint` all pass.
- [ ] Diff is minimal and focused — no drive-by changes.

## Workflow

1. Open the closest existing endpoint in `resources/governance/` and mirror it.
2. Add request/response/model/enum classes under `<feature>.model`.
3. Add endpoint path constants to the companion object.
4. Implement the operation using the template above.
5. Add unit tests next to the existing governance parsing tests
   (`library/src/test/java/com/nextcloud/android/lib/resources/<feature>/`).
6. Add an integration test (`*IT`) under
   `library/src/androidTest/java/com/nextcloud/android/lib/resources/<feature>/`.
7. Run the CI checks: compile + tests + `detekt` + `spotlessKotlinCheck` + `lint`.
8. Review the diff; remove anything unrelated.
