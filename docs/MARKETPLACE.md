<div align="center">
  <img src="images/po-editor-logo.svg" width="96" height="96" alt="PO Editor logo">
</div>

# JetBrains Marketplace publishing

The project is configured for signed releases through the IntelliJ Platform Gradle Plugin and
GitHub Actions.

## One-time Marketplace setup

1. Sign in to the [JetBrains Marketplace](https://plugins.jetbrains.com/).
2. Check that the name **PO Editor** and the stable ID `ru.aksenov.poeditor` are available.
3. Build the initial archive with `./gradlew clean buildPlugin`.
4. Create the plugin entry and upload the ZIP from `build/distributions/` manually.
5. Complete the Marketplace listing:
   - license: MIT;
   - source code: `https://github.com/nawinds/poeditor`;
   - issue tracker: `https://github.com/nawinds/poeditor/issues`;
   - vendor website: `https://nawinds.dev`;
   - support email: `support@nawinds.dev`.
6. Generate a permanent token under **My Tokens** in the Marketplace profile.

The first upload must be manual so Marketplace-specific listing options can be configured.
Subsequent releases can be automated.

## GitHub Actions secrets

Add these repository secrets under **Settings → Secrets and variables → Actions**:

| Secret | Purpose |
| --- | --- |
| `PUBLISH_TOKEN` | Permanent JetBrains Marketplace token |
| `PRIVATE_KEY` | PEM private key used to sign the plugin |
| `PRIVATE_KEY_PASSWORD` | Password protecting the private key |
| `CERTIFICATE_CHAIN` | PEM X.509 certificate chain |

The Gradle plugin consumes these names directly. Never store their values in the repository.

An optional `PUBLISH_CHANNEL` repository variable can select a Marketplace channel such as
`eap`. If omitted, releases go to `default`.

## Signing certificate

Follow the official
[JetBrains plugin signing guide](https://plugins.jetbrains.com/docs/intellij/plugin-signing.html)
to create the private key and certificate chain. Store only the public documentation in the
repository; private signing material belongs in GitHub Actions secrets or a secure local vault.

## Release checklist

1. Move user-visible changes from **Unreleased** into a version section in `CHANGELOG.md`.
2. Update `pluginVersion` in `gradle.properties` using Semantic Versioning.
3. Run:

   ```bash
   ./gradlew clean test buildPlugin verifyPlugin
   ```

4. Commit and push the release changes.
5. Create a GitHub release with tag `v<pluginVersion>`.
6. The release workflow validates that the tag matches the Gradle version, signs the archive,
   publishes it to Marketplace, and attaches the ZIP to the GitHub release.
7. Review the upload in the Marketplace dashboard and submit it for approval if requested.

## Useful official references

- [Publishing a plugin](https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html)
- [IntelliJ Platform Gradle Plugin 2.x](https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html)
- [Plugin configuration file](https://plugins.jetbrains.com/docs/intellij/plugin-configuration-file.html)
- [Marketplace listing best practices](https://plugins.jetbrains.com/docs/marketplace/best-practices-for-listing.html)
- [Plugin signing](https://plugins.jetbrains.com/docs/intellij/plugin-signing.html)
