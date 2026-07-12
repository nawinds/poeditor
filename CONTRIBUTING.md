# Contributing

Thank you for helping improve PO Editor.

## Development setup

1. Install JDK 21.
2. Clone the repository.
3. Run `./gradlew test`.
4. Run `./gradlew runIde` to start a sandbox IDE with the plugin installed.

## Before opening a pull request

- Keep changes focused and avoid unrelated formatting.
- Add or update tests for parser, scanner, and synchronization behavior.
- Run `./gradlew clean test buildPlugin`.
- Run `./gradlew verifyPlugin` for changes that touch IntelliJ APIs or plugin metadata.
- Update `CHANGELOG.md` under **Unreleased** for user-visible changes.
- Do not commit certificates, private keys, Marketplace tokens, IDE sandboxes, or build output.

## Code style

- Follow the existing Kotlin style and use four spaces for indentation.
- Prefer small, explicit functions over hidden side effects.
- Keep PO parsing and synchronization logic independent of Swing where possible.
- User-facing text and Marketplace metadata should be clear English.

## Issues

For bugs, include the IDE version, plugin version, operating system, reproduction steps, expected
behavior, and a minimal PO/POT example when possible. Never attach confidential catalogs without
redacting them first.

By contributing, you agree that your contribution is licensed under the MIT License.

