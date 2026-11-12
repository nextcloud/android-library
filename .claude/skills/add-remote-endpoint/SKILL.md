---
name: add-remote-endpoint
description: Use when adding a new remote/network endpoint (a RemoteOperation on OcsKotlinRemoteOperation) to this Nextcloud Android library, or when the user says "add an endpoint", "new remote operation", "implement this API call", "create endpoints for <feature>", or "wire up <OCS/ocs> API". Scopes the calls to the client flow, then scaffolds verb/sub-feature packages, models, operations and integration tests following the teams package conventions and the standards in AGENTS.md.
---

<!--
 ~ SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 ~ SPDX-License-Identifier: MIT
-->

# Add a remote endpoint

Implement new `RemoteOperation`s for this library the way the existing ones are written. The full,
authoritative standards live in **[AGENTS.md](../../../AGENTS.md)** — this skill is the step-by-step
procedure. When they disagree, AGENTS.md wins.

## 0. Ground yourself in the reference

Read before writing anything:

- `library/src/main/java/com/nextcloud/operations/OcsKotlinRemoteOperation.kt` — the base class every
  OCS operation extends (call, guard, decode, logging, release).
- `library/src/main/java/com/nextcloud/android/lib/resources/teams/` — the canonical feature:
  verb/sub-feature packages, model placement, `createMethod` + `parse` operations.
- `library/src/androidTest/java/com/nextcloud/android/lib/resources/teams/` — one `*IT` per operation.

Do **not** use `resources/governance/` as a template; it predates the base class.

## 1. Scope the calls to the flow

Ask for (or extract from the issue) the client flow: the screens and user actions. List each step and
map it to the one server call it needs. Implement only those calls.

- Server/web-frontend API files (e.g. a `api.ts`) list far more than a mobile flow needs. Don't port
  them wholesale.
- Leave out admin-only endpoints, other apps' APIs and calls the library already has (WebDAV, sharees).
- Name skipped-but-related calls to the human as possible follow-ups instead of implementing them.

If the flow is unclear, ask before coding.

## 2. Confirm each contract against the server

For every call, read the server controller (and `routes.php` / `#[ApiRoute]`) at the relevant commit:

- HTTP verb and full path (`/ocs/v2.php/apps/<app>/…` or core `/ocs/v2.php/<area>/…`)
- request parameters / JSON body field names and types
- the exact `DataResponse` shape: which fields always exist, which can be `null`, whether `data` is an
  object, a list, or an object wrapping a list
- non-200 cases the client should know about (e.g. 404 when nothing is linked)

## 3. Plan the packages

```
resources/<feature>/
  model/                          shared by more than one inner package
  <verb>/<subfeature>/            the operation
  <verb>/<subfeature>/model/      models only that operation uses
```

`<verb>` is `get` / `post` / `put` / `delete`; `<subfeature>` is a short noun (`team`, `members`,
`folder`). For each model, list which operations use it, then place it in the narrowest package that
covers all of them. Never one big `<feature>/model` package.

## 4. Models

- `@Serializable data class` per wire type, one public type per file; enums with `@SerialName`.
- Required fields for what the server always sends; defaults/nullable only for what it can omit or
  null.
- Request bodies: `internal @Serializable` classes, **no default values** (`ocsJson` drops defaults).
- Response wrappers that only exist to match the wire (`{ "resources": [...] }`): `internal`.
- Computed helpers for server semantics the flow needs (`Team.isMember`, `TeamMember.role`).
- Numeric enum wire values: file-level `private const val`s (detekt `MagicNumber`).
- No comments or KDoc.

## 5. Operation

Extend `OcsKotlinRemoteOperation<T>`; implement only `createMethod` and `parse`, plus companion
constants:

```kotlin
class GetTeamFolderRemoteOperation(
    private val teamId: String
) : OcsKotlinRemoteOperation<TeamFolder>() {
    override fun createMethod(baseUrl: String) =
        GetMethod(baseUrl + ENDPOINT + SEPARATOR + teamId + FOLDER + JSON_FORMAT, true)

    override fun parse(body: String) = decode(body, TeamFolder.serializer())

    companion object {
        private const val ENDPOINT = "/ocs/v2.php/apps/circles/teams"
        private const val FOLDER = "/folder"
    }
}
```

- List: `decode(body, ListSerializer(X.serializer()))`.
- Wrapper: `decode(body, XResponse.serializer()).items`.
- Body: `PostMethod(url, true, jsonBody(XRequest(...), XRequest.serializer()))`.
- Nothing to return: `OcsKotlinRemoteOperation<Void>` with `override fun parse(body: String): Void? = null`.
- Reuse `SEPARATOR`; every path segment is a `private const val`; `JSON_FORMAT` is inherited.
- Never override `run()`. If the base class can't express something, extend the base class once.
- No comments or KDoc. License header with the author's own name and e-mail.

## 6. Integration tests

No parsing unit tests. One `*IT` per operation, same package layout under
`library/src/androidTest/java/com/nextcloud/android/lib/resources/<feature>/<verb>/<subfeature>/`:

- `class XRemoteOperationIT : AbstractIT()`, gated with `testOnlyOnServer(NextcloudVersion.nextcloud_XX)`
  (existing constant).
- Create the data the test needs with the library's own operations, using unique names.
- Assert `isSuccess`, the important `resultData` fields, and `httpCode` for expected failures.
- `nextcloudClient` is the non-admin `test` user on CI; use `client2` only for admin-only endpoints.

## 7. Stay inside the feature

Don't touch `.github/` workflows, `NextcloudVersion` or other shared files as part of the endpoint
task. If CI lacks an app the tests need, tell the human.

## 8. Verify

While working, compile only:

```bash
./gradlew :library:compileDebugKotlin
./gradlew :library:compileDebugAndroidTestKotlin
```

Present the result for review. After the human agrees, run the CI checks once
(`.github/workflows/check.yml`):

```bash
./gradlew :library:detekt
./gradlew :library:spotlessKotlinCheck
./gradlew :library:lint
```

Check lines are ≤ 120 by hand (spotless can't always run locally — see AGENTS.md). Then review the
diff against the "Definition of done" checklist in AGENTS.md and remove anything unrelated.
