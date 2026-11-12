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
  - Shared OCS types live here: `com.owncloud.android.lib.ocs` — `OcsKotlinResponse<T>` /
    `OcsKotlin<T>` envelope, `ocsJson` (the strict shared `Json`), `SEPARATOR` (`"/"`).
- **Modern package (`nextcloud`):** `library/src/main/java/com/nextcloud/android/lib/`
  - Remote operations: `…/resources/<feature>/<verb>/<subfeature>/`
  - Wire models/enums: next to the operation that uses them (see [Package structure](#package-structure))
- **Base class for OCS operations:** `com.nextcloud.operations.OcsKotlinRemoteOperation<T>`
- **Canonical reference implementation:**
  `library/src/main/java/com/nextcloud/android/lib/resources/teams/`
  - `resources/governance/` predates the base class. Do not copy its hand-written `run()` bodies.
- **Tests:** instrumented / integration tests (real API calls): `library/src/androidTest/java/…`

**Before writing anything, read `resources/teams/` and copy its shape.** Copy the *architecture*,
not the text. Do not invent new patterns unless a requirement genuinely cannot be met with the
existing one.

## Scope: build only what the flow needs

Endpoints are added for a concrete client flow (an issue, a screen list, a user story), not to mirror
every call a server or web-frontend API file exposes.

1. Write down the flow's steps (e.g. "see my teams", "create a team", "open the team folder").
2. Map each step to the **one** server call that serves it. Read the server controller to confirm the
   route, the HTTP method, the request body and the exact response shape.
3. Implement only those calls and only the model fields and helpers those steps use.
4. Skip admin-only endpoints, other apps' APIs, WebDAV calls the library already has, and anything
   "nice to have". Mention possible follow-ups to the human instead of implementing them.

Example — the Teams flow (list teams, create team, see what's shared, manage members, open settings,
open team folder, open Talk chat) needs exactly six operations: `GetTeams`, `GetTeamMembers`,
`GetTeamResources`, `GetTeamFolder`, `CreateTeam`, `AddTeamMembers`. Favourites, tab order, dashboard
previews, join/leave/delete and folder linking were deliberately left out.

**Stay inside the feature.** An endpoint task does not change CI workflows (`.github/`),
`NextcloudVersion`, or other shared files as a side effect. If the feature genuinely needs such a
change, ask the human first.

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
- Never create a new `Json { }`. `OcsKotlinRemoteOperation` decodes and encodes with the shared strict
  `ocsJson` through its `decode(...)` and `jsonBody(...)` helpers.
- Request bodies are dedicated `internal @Serializable` request classes (`CreateTeamRequest`), never
  hand-built JSON strings or maps. Do **not** give request fields default values: `ocsJson` does not
  encode defaults, so a defaulted field silently disappears from the body.
- Response model fields that the server always sends are required. Give a default (or make nullable)
  only when the server can omit the field or send `null`.

`ocsJson` is strict on purpose (fail fast): unknown keys are ignored, but a missing/mismatched
required field throws instead of yielding a half-populated object.

## Package structure

New endpoints live under `com.nextcloud.android.lib.resources.<feature>`. Inside a feature,
operations are grouped **by HTTP verb, then by sub-feature**, and every model lives in the
**narrowest package that uses it**:

```
resources/teams/
 ├── model/                                   # shared: used by more than one inner package
 │   ├── Team.kt                              #   (GetTeams + CreateTeam)
 │   ├── TeamMember.kt                        #   (Team.initiator + GetTeamMembers)
 │   ├── TeamMemberType.kt
 │   └── TeamRole.kt
 ├── get/
 │   ├── team/GetTeamsRemoteOperation.kt
 │   ├── members/GetTeamMembersRemoteOperation.kt
 │   ├── folder/
 │   │   ├── GetTeamFolderRemoteOperation.kt
 │   │   └── model/TeamFolder.kt              # only used by GetTeamFolder
 │   └── resources/
 │       ├── GetTeamResourcesRemoteOperation.kt
 │       └── model/
 │           ├── TeamResource.kt
 │           ├── TeamResourceProvider.kt
 │           └── TeamResourcesResponse.kt     # internal wrapper, unwrapped in parse()
 └── post/
     ├── team/
     │   ├── CreateTeamRemoteOperation.kt
     │   └── model/CreateTeamRequest.kt
     └── members/
         ├── AddTeamMembersRemoteOperation.kt
         └── model/
             ├── AddTeamMemberRequest.kt
             ├── AddTeamMembersRequest.kt
             └── NewTeamMember.kt
```

Rules:

- Verb packages: `get`, `post`, `put`, `delete`. Sub-feature packages are short nouns (`team`,
  `members`, `folder`, `resources`).
- A model used by **one** sub-feature goes in `<feature>/<verb>/<subfeature>/model/`.
- A model used by **more than one** inner package goes in `<feature>/model/`. Never put every model
  into one big `<feature>/model` package "just in case".
- Request bodies and response wrappers that exist only to match the wire format are `internal`.
  Expose clean public types (`List<TeamResource>`, not `TeamResourcesResponse`).
- Small derived helpers that encode server semantics belong on the model as computed properties
  (`Team.isMember`, `TeamMember.role`, `TeamMemberType.fromShareType(...)`) so every client app does
  not re-implement them. Only add the ones the flow uses.
- Reusable cross-feature types (the OCS envelope, `ocsJson`, `SEPARATOR`) stay in
  `com.owncloud.android.lib.ocs`.

## Remote operation pattern

Every OCS operation extends **`com.nextcloud.operations.OcsKotlinRemoteOperation<T>`**. The base class
owns the whole call: execute, non-200 guard, decode of the `{ "ocs": { "data": … } }` envelope,
`RemoteOperationResult` creation, the single `catch` + logging (tagged with the subclass name) and
`releaseConnection()`. A subclass only says **what to send** and **how to read the answer**:

| Member | Purpose |
|---|---|
| `createMethod(baseUrl)` | Build the `GetMethod` / `PostMethod` / `PutMethod` / `DeleteMethod` with the full URL. |
| `parse(body)` | Turn the response body into `T` (usually one `decode(...)` call). |
| `decode(body, serializer)` | Helper: decode `ocs.data` with `ocsJson`. |
| `jsonBody(value, serializer)` | Helper: encode a request object as an `application/json` body. |

GET returning a model (`GetTeamFolderRemoteOperation`):

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

Variations:

- **List response:** `decode(body, ListSerializer(TeamMember.serializer()))`.
- **Wrapped response** (`{ "resources": [...] }`): decode the internal wrapper and unwrap it —
  `decode(body, TeamResourcesResponse.serializer()).resources`.
- **Request body:**

  ```kotlin
  override fun createMethod(baseUrl: String) =
      PostMethod(
          baseUrl + ENDPOINT + JSON_FORMAT,
          true,
          jsonBody(CreateTeamRequest(name, createTeamFolder), CreateTeamRequest.serializer())
      )
  ```

- **No useful response data:** type the operation `OcsKotlinRemoteOperation<Void>` and
  `override fun parse(body: String): Void? = null`. Don't decode a loosely shaped response just to
  throw it away.

Do **not** override `run()` and do not copy the base class body into an operation. If an endpoint needs
something the base class cannot express (e.g. a different success status), extend the base class in
one place so every operation benefits — do not fork the call logic.

Why the base class is written the way it is (keep this when changing it):

- **Fail fast, read top to bottom.** A guard clause returns early on non-OK status. No nested
  `if/else`, no `else` branch.
- **Return count ≤ 2.** Exactly two `return` keywords: the guard `return` and the outer `return try`
  (detekt `ReturnCount: max: 2`).
- **One catch.** `catch (e: Exception)` once, delegated to `failure(e)`, with
  `@Suppress("TooGenericExceptionCaught")` on `run()`.
- **`finally { method.releaseConnection() }`** always runs, including on the early `return`.
- **Property access**, not setters: `apply { resultData = data }`.
- **`failure(...)`** carries `@Suppress("DEPRECATION")` because `RemoteOperationResult.logMessage` is a
  deprecated Java accessor. Suppress at the usage only — never globally.

## Constants — no hardcoded strings

- Path separators: reuse the shared `com.owncloud.android.lib.ocs.SEPARATOR` (`"/"`). Never inline `"/"`.
- Endpoint base paths and per-endpoint path segments: `private const val` in the operation's
  `companion object` (`ENDPOINT`, `FOLDER`, `MEMBERS`, …). `JSON_FORMAT` is inherited.
- The same applies to parameter keys, headers, and repeated strings.
- Enum wire values that are numbers (detekt `MagicNumber` does not ignore enums): declare them as
  file-level `private const val`s and pass those to the enum entries (see `TeamMemberType`).

## Class design and comments

Class names describe responsibility (`GetTeamFolderRemoteOperation`). **Do not add comments or KDoc**
to new code — names, types and small functions carry the meaning. Explain non-obvious server behaviour
(e.g. "fails with 404 when no team folder exists") to the human in your summary, not in the source.

## Testing

Endpoints are verified with **integration tests only**. Do **not** add JSON-parsing unit tests for
operations or models.

- One `*IT` class per operation, named after it (`GetTeamFolderRemoteOperationIT`), in the **same
  package layout** under `library/src/androidTest/java/…/resources/<feature>/<verb>/<subfeature>/`.
- Extend `AbstractIT` and run the operation against the live server with `nextcloudClient`; assert
  `isSuccess`, the relevant `resultData` fields and, for expected failures, `httpCode`.
- Each test creates the data it needs through the library's own operations (e.g.
  `CreateTeamRemoteOperation` before `GetTeamMembersRemoteOperation`); use unique names
  (`"Team " + System.currentTimeMillis()`).
- Gate on server version with `testOnlyOnServer(NextcloudVersion.nextcloud_XX)` using an existing
  constant.
- `client` / `nextcloudClient` are the **non-admin** `test` user on CI; `client2` is admin. Endpoints
  that need admin must use `client2`.
- The CI server only has the apps installed by `.github/workflows/configNC.sh`. If a test needs an app
  that is not there, tell the human — don't edit the workflow as part of the endpoint task.

## Build & verify

CI runs three checks in parallel — **`detekt`, `spotlessKotlinCheck`, and `lint`** (see
`.github/workflows/check.yml`). Compile main and test sources while working; run the static checks
**once, at the end**, after the human has reviewed the result and agreed — not after every iteration.

```bash
./gradlew :library:compileDebugKotlin                 # main sources
./gradlew :library:compileDebugAndroidTestKotlin      # integration tests
./gradlew :library:detekt                             # static analysis (see thresholds below)
./gradlew :library:spotlessKotlinCheck                # ktlint formatting
./gradlew :library:lint                               # Android lint
```

- **detekt** config: `library/detekt.yml`. The rules that bite endpoint code:
  - `ReturnCount: max: 2`, `TooGenericExceptionCaught: active`, `ThrowsCount: max: 2` — handled by
    `OcsKotlinRemoteOperation`; another reason not to hand-write `run()`.
  - `MagicNumber` (enums are not ignored), `LongParameterList` (6), `TooManyFunctions` (15).
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
  `feat(teams): add team folder endpoint`).
- Reference the relevant issue in the PR body (e.g. "Closes #123").

## Definition of done

- [ ] Only the calls the client flow needs; each one verified against the server controller.
- [ ] Operations extend `OcsKotlinRemoteOperation<T>`; no hand-written `run()`.
- [ ] Layout is `<feature>/<verb>/<subfeature>/`; each model in the narrowest package that uses it,
      shared ones in `<feature>/model/`.
- [ ] No Gson; all wire models `@Serializable`; request classes `internal` with no default values.
- [ ] No new Java classes.
- [ ] No hardcoded `"/"` or endpoint strings; constants used.
- [ ] No comments or KDoc in new code; license header with your own name.
- [ ] Lines ≤ 120.
- [ ] One `*IT` per operation, mirroring the package layout; no parsing unit tests.
- [ ] No changes to workflows, `NextcloudVersion` or other shared files unless the human asked.
- [ ] Compiles (`compileDebugKotlin`, `compileDebugAndroidTestKotlin`); `detekt`,
      `spotlessKotlinCheck` and `lint` run once at the end and pass.
- [ ] Diff is minimal and focused — no drive-by changes.

## Workflow

1. Write down the client flow and map each step to one server call (see [Scope](#scope-build-only-what-the-flow-needs)).
2. Read the server controller for each call: route, verb, body, response shape.
3. Decide the package of every model: one user → `<verb>/<subfeature>/model/`, several → `<feature>/model/`.
4. Add the models (`@Serializable`; request/wrapper classes `internal`).
5. Add each operation on top of `OcsKotlinRemoteOperation<T>`: `createMethod`, `parse`, companion constants.
6. Add one `*IT` per operation in the mirrored `androidTest` package.
7. Compile main + androidTest sources; present the result to the human for review.
8. After review, run `detekt`, `spotlessKotlinCheck` and `lint` once; fix what they report.
9. Review the diff; remove anything unrelated.
