# Agent Instructions — Implementing Network Endpoints

Standards for adding new remote/network endpoints to this mixed Java + Kotlin networking library
(`nextcloud-android-library`). These rules are enforced by the build (detekt + ktlint); code that
ignores them will fail CI.

**Before writing anything, read an existing endpoint and copy its shape.** The canonical, up-to-date
reference is the governance package:

```
library/src/main/java/com/nextcloud/android/lib/resources/governance/
```

Copy the *architecture*, not the text. Do not invent new patterns unless a requirement genuinely
cannot be met with the existing one.

---

## Build & verify

Run these from the repo root before finishing. All must pass.

```bash
./gradlew :library:compileDebugKotlin                                   # compiles
./gradlew :library:testDebugUnitTest --tests "com.<pkg>.*"             # unit tests for your package
./gradlew :library:detekt                                              # static analysis (see thresholds below)
```

- **detekt** config: `library/detekt.yml`. The rules that bite endpoint code:
  - `ReturnCount: max: 2` — a `run()` may contain at most **two** `return` keywords.
  - `TooGenericExceptionCaught: active` — catching `Exception` requires `@Suppress("TooGenericExceptionCaught")`.
  - `ThrowsCount: max: 2`.
- **Line length: 120** (`.editorconfig`). Import ordering is disabled — do not reorder imports to "fix" lint.
- **Formatting** is ktlint via spotless. The `:library:spotlessKotlinCheck` task currently fails at
  *configuration* time (a spotbugs/AGP incompatibility unrelated to your code), so verify formatting
  by hand against the 120-char limit and the patterns below.

---

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

---

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
- [ ] `compileDebugKotlin`, `testDebugUnitTest`, and `detekt` all pass.
- [ ] Diff is minimal and focused — no drive-by changes.

## Workflow

1. Open the closest existing endpoint in `resources/governance/` and mirror it.
2. Add request/response/model/enum classes under `<feature>.model`.
3. Add endpoint path constants to the companion object.
4. Implement the operation using the template above.
5. Add unit tests next to the existing governance parsing tests
   (`library/src/test/java/com/nextcloud/android/lib/resources/<feature>/`).
6. Run compile + tests + detekt.
7. Review the diff; remove anything unrelated.
