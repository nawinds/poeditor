<div align="center">
  <img src="docs/images/po-editor-logo.svg" width="112" height="112" alt="PO Editor logo">

  # PO Editor

  **A JetBrains IDE plugin for editing gettext `.po` translations and `.pot` templates in a table.**

  [![Build](https://github.com/nawinds/poeditor/actions/workflows/build.yml/badge.svg)](https://github.com/nawinds/poeditor/actions/workflows/build.yml)
  [![License: MIT](https://img.shields.io/badge/license-MIT-6B57FF.svg)](LICENSE)
  [![Kotlin](https://img.shields.io/badge/Kotlin-2.3.20-7F52FF.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
  [![IntelliJ Platform](https://img.shields.io/badge/IntelliJ_Platform-2026.1%2B-3C99CC.svg?logo=intellijidea&logoColor=white)](https://plugins.jetbrains.com/docs/intellij/)
</div>

## What is PO Editor?

**PO Editor** is a localization plugin for JetBrains IDEs. It helps developers create, edit, validate, and compile GNU gettext translation files directly in the IDE.

Instead of editing `.po` and `.pot` files as plain text, you can work with a simple table:

| Identifier | Source text | Translation |
| --- | --- | --- |
| `button.save` | `Save` | `Сохранить` |
| `page.title` | `Home page` | `Главная страница` |

The plugin is useful for projects that use **gettext**, **Babel**, **Flask-Babel**, **Jinja2**, or any other localization workflow based on `.po`, `.pot`, and `.mo` files.

## Features

- Edit `.po` translation files in a three-column table.
- Edit `.pot` translation templates in the same table interface.
- Create new `.po` and `.pot` files from the **New** menu.
- Add and delete translation rows from the table editor.
- Attach a `.pot` template to a `.po` file.
- Update `.po` files from a `.pot` template without overwriting existing non-empty translations.
- Mark translations that were removed from the template.
- Scan project source code and update a `.pot` template with new translation keys.
- Validate `.po` syntax with `msgfmt`.
- Compile `.po` files to `.mo` files.
- Highlight untranslated, fuzzy, completed, source-equal, and removed entries.
- Highlight repeated spaces, tabs, and line breaks in the text fields.

## Supported file types

| File type | Purpose | Plugin support |
| --- | --- | --- |
| `.pot` | Translation template | Table editor, source scan, template update |
| `.po` | Translation catalog | Table editor, template sync, syntax check, `.mo` compilation |
| `.mo` | Compiled binary catalog | Custom file icon |

## Recommended gettext format

PO Editor works best with gettext context-based entries:

```po
msgctxt "button.save"
msgid "Save"
msgstr "Сохранить"
```

In this format:

- `msgctxt` is the stable translation identifier.
- `msgid` is the source text.
- `msgstr` is the translated text.

This keeps translation keys stable even when the source text changes.

## Installation

### Option 1: JetBrains Marketplace

After the plugin is published on JetBrains Marketplace:

1. Open your JetBrains IDE.
2. Go to **Settings / Preferences → Plugins → Marketplace**.
3. Search for **PO Editor**.
4. Click **Install**.
5. Restart the IDE if requested.

### Option 2: Install from a GitHub Release ZIP

You can also install the plugin manually from GitHub Releases.

1. Open the repository **Releases** page.
2. Download the plugin ZIP from the latest release assets.
3. Do not unzip the archive.
4. Open your JetBrains IDE.
5. Go to **Settings / Preferences → Plugins**.
6. Click the gear icon.
7. Choose **Install Plugin from Disk...**.
8. Select the downloaded `.zip` file.
9. Restart the IDE if requested.

This is useful when the plugin is not yet available on Marketplace, when you need a specific version, or when you want to test a pre-release build.

### Option 3: Build from source

```bash
./gradlew buildPlugin
```

The installable ZIP will be created in:

```text
build/distributions/
```

Install it with **Settings / Preferences → Plugins → Install Plugin from Disk...**.

## Quick start

### Create a translation template

1. Right-click a directory in the Project view.
2. Choose **New → POT Translation Template**.
3. Open the created `.pot` file.
4. Add rows manually or click **Update Template** to scan the project for translation keys.

### Create a translation file

1. Right-click a directory in the Project view.
2. Choose **New → PO Translation File**.
3. Open the created `.po` file.
4. Click **Choose Template** and select a `.pot` file.
5. Click **Update Translations** to add missing rows from the template.
6. Fill the translations in the table or in the large text fields below it.

### Update translations after source code changes

A common workflow:

1. Add new translation calls in the source code.
2. Open the `.pot` file.
3. Click **Update Template**.
4. Open a `.po` file.
5. Click **Update Translations**.
6. Translate the new rows.

Existing non-empty translations are preserved.

## Source code scanning

PO Editor can scan project files and collect static translation keys from calls like:

```text
t("button.save")
i18n("button.save")
translate("button.save")
gettext("button.save")
$t("button.save")
tr("button.save")
_("button.save")
```

Jinja2 examples:

```jinja2
<title>{{ _('page_title') }}</title>
<p>{{ gettext("page_description") }}</p>
```

Only static string literals are collected. Dynamic keys are ignored because they cannot be safely written to a gettext template.

## Syntax validation and MO compilation

PO Editor uses `msgfmt` for syntax validation and `.mo` compilation.

Install GNU gettext first:

```bash
# macOS
brew install gettext

# Debian / Ubuntu
sudo apt install gettext
```

The plugin looks for `msgfmt` in:

```text
/opt/homebrew/bin/msgfmt
/usr/local/bin/msgfmt
PATH
```

## Development

Requirements:

- JDK 21
- Gradle Wrapper from this repository
- IntelliJ Platform 2026.1 or newer

Useful commands:

```bash
./gradlew test
./gradlew runIde
./gradlew buildPlugin
./gradlew verifyPlugin
```

What these commands do:

| Command | Purpose |
| --- | --- |
| `./gradlew test` | Run tests |
| `./gradlew runIde` | Start a sandbox IDE with the plugin installed |
| `./gradlew buildPlugin` | Build the installable plugin ZIP |
| `./gradlew verifyPlugin` | Check plugin compatibility with JetBrains IDEs |

Project structure:

```text
src/main/kotlin/ru/aksenov/poeditor/
  actions/   Toolbar and New-file actions
  editor/    Table editor and UI model
  parser/    PO parsing and writing
  scanner/   Source scanning and template synchronization
```

## Release process

1. Update `pluginVersion` in `gradle.properties`.
2. Update `CHANGELOG.md`.
3. Update `<change-notes>` in `src/main/resources/META-INF/plugin.xml`.
4. Run local checks:

```bash
./gradlew clean test buildPlugin verifyPlugin
```

5. Commit the release changes.
6. Create a Git tag, for example:

```bash
git tag v1.0.0
git push origin v1.0.0
```

7. Create a GitHub Release for the same tag.
8. The release workflow signs and publishes the plugin, then attaches the signed ZIP archive to the GitHub Release.

## Changing the plugin version

The main plugin version is defined in:

```properties
# gradle.properties
pluginVersion=1.0.0
```

The Gradle build uses this value as the project version:

```kotlin
version = providers.gradleProperty("pluginVersion").get()
```

For a `1.0.0` release, use:

```properties
pluginVersion=1.0.0
```

Then create the matching Git tag:

```bash
git tag v1.0.0
git push origin v1.0.0
```

The release workflow expects the tag version and `pluginVersion` to match.

## Publishing

The repository includes GitHub Actions for build checks and release publishing.

See [docs/MARKETPLACE.md](docs/MARKETPLACE.md) for Marketplace setup, signing secrets, and the release checklist.

## Contributing

Contributions are welcome. Please read [CONTRIBUTING.md](CONTRIBUTING.md) before opening a pull request.

For security issues, follow [SECURITY.md](SECURITY.md).

## License

PO Editor is released under the [MIT License](LICENSE).
