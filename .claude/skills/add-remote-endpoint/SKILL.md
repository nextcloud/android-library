---
name: add-remote-endpoint
description: Use when adding a new remote/network endpoint (a RemoteOperation / OCSRemoteOperation) to this Nextcloud Android library, or when the user says "add an endpoint", "new remote operation", "implement this API call", or "wire up <OCS/ocs> API". Scaffolds the models, constants, operation, and tests following the governance package conventions and the standards in AGENTS.md.
---

# Add a remote endpoint

Implement a new `RemoteOperation` for this library the way the existing ones are written. The full,
authoritative standards live in **[AGENTS.md](../../../AGENTS.md)** — this skill is the step-by-step
procedure. When they disagree, AGENTS.md wins.

## 0. Ground yourself in the reference

Read the canonical package before writing anything:

- `library/src/main/java/com/nextcloud/android/lib/resources/governance/` — operations (GET/POST/DELETE)
- `library/src/main/java/com/nextcloud/android/lib/resources/governance/model/` — `@Serializable` models + enums
- `library/src/main/java/com/owncloud/android/lib/ocs/OcsResponse.kt` — shared `OcsResponse<T>`, `ocsJson`, `SEPARATOR`

Pick the existing operation closest to the one you're adding (list-returning GET, single-object GET,
POST with body, DELETE) and mirror its structure.

## 1. Confirm the contract

Establish, from the user or the API: HTTP method, path (base + segments), path/query params, request
body shape (if any), and the JSON response shape. If any of these is unclear, ask before coding.

## 2. Models — under `<feature>.model`

For each new wire type create a Kotlin `@Serializable data class` (or `enum` with `@SerialName`) in
`com.nextcloud.android.lib.resources.<feature>.model`. Never put models beside the operation or in an
unrelated package. Reuse the shared `OcsResponse<T>` envelope — do not model the `ocs`/`data`/`meta`
wrapper yourself.

## 3. Constants

- Reuse `com.owncloud.android.lib.ocs.SEPARATOR` for `"/"` — never inline it.
- Put the base path and each path segment in the operation's `companion object` as `private const val`
  (`ENDPOINT`, `AVAILABLE`, …). No hardcoded endpoint strings, param keys, or headers.

## 4. Operation

Extend `OCSRemoteOperation<T>` and follow the template in AGENTS.md exactly:

- `@Suppress("TooGenericExceptionCaught")` on `run()`.
- Build the method (`GetMethod`/`PostMethod`/`DeleteMethod`), then `return try { … } catch (e: Exception) { failure(e) } finally { method.releaseConnection() }`.
- Guard clause first: `if (status != HttpStatus.SC_OK) return RemoteOperationResult(false, method)`.
  No nested if/else; keep the body linear.
- Decode with `ocsJson.decodeFromString<OcsResponse<T>>(method.getResponseBodyAsString()).ocs.data`.
- Success: `RemoteOperationResult<T>(true, method).apply { resultData = data }` (property, not a setter).
- Keep **≤ 2 `return` keywords** total (guard + `return try`). The success and `failure` values are
  expressions, not returns.
- Add the `private fun failure(e: Exception)` helper with `@Suppress("DEPRECATION")` (for `logMessage`)
  that logs via `Log_OC.e` and returns `RemoteOperationResult(e)`.
- No class-level KDoc that just restates the class name.

## 5. Tests

Add unit tests beside the existing ones in
`library/src/test/java/com/nextcloud/android/lib/resources/<feature>/`, mirroring the governance
parsing tests — feed a sample OCS JSON string through `ocsJson.decodeFromString<OcsResponse<…>>(json)`
and assert the decoded fields.

## 6. Verify

```bash
./gradlew :library:compileDebugKotlin
./gradlew :library:testDebugUnitTest --tests "com.nextcloud.android.lib.resources.<feature>.*"
./gradlew :library:detekt
```

All three must pass. Check lines are ≤ 120 (spotless check can't be relied on — see AGENTS.md). Then
review the diff against the "Definition of done" checklist in AGENTS.md and remove anything unrelated.
