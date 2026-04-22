Update the migration guide for breaking changes.

## Instructions

1. Run `./gradlew apiCheck` to detect binary-incompatible changes. If it fails, run `./gradlew apiDump` to see the updated `.api` files and diff them against the committed versions to identify exactly what changed.

2. Also look at the recent git diff (staged + unstaged) and recent commits on the current branch to identify breaking changes in the public API of any Wiretap module:
   - **OkHttp:** `wiretap-okhttp/` and `wiretap-okhttp-noop/` — public extensions in `okhttp/sse/`, `okhttp/ws/`, `okhttp/http/`
   - **Ktor:** `wiretap-ktor/` and `wiretap-ktor-noop/` — public plugins and extensions in `plugin/sse/`, `plugin/ws/`, `plugin/http/`
   - **URLSession:** `wiretap-urlsession/` — public Swift API

3. Breaking changes include:
   - Removed or renamed public classes, functions, or extensions
   - Changed function signatures (parameters, return types)
   - Package moves (import changes)
   - New required annotations (`@OptIn`, `@ExperimentalWiretapSseApi`)
   - Visibility changes (public → internal)
   - Deprecated APIs with replacements
   - Config class changes

4. For each affected module, update the corresponding migration guide:
   - `docs/okhttp/migration.md`
   - `docs/ktor/migration.md`
   - `docs/urlsession/migration.md`

5. Follow this format for each version section:
   ```markdown
   ## OLD_VERSION → NEW_VERSION

   ### Short description of change

   Brief explanation of what changed and why.

   ```diff
   - old code
   + new code
   ```
   ```

6. Check the current version in `gradle.properties` (`wiretap.version`) to determine the target version. Use the previous version as the "from" version.

7. If no breaking changes are found for a module, skip it — do not add empty sections.
