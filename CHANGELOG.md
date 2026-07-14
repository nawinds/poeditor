# Changelog

All notable changes to PO Editor are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project follows [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.0] - 2026-07-14

### Added

- Support for builds since 243

## [0.1.0] - 2026-07-12

### Added

- Table editor for PO translations and POT templates.
- PO/POT creation actions in the IDE's New menu.
- POT generation from static translation calls in project sources.
- Jinja2 gettext shorthand scanning for expressions such as `{{ _('page_title') }}`.
- Synchronization with standard Babel/gettext POT catalogs and context-based catalogs.
- Syntax checking through `msgfmt` and PO-to-MO compilation.
- Status coloring, translation progress, whitespace highlighting, and row management.
- Automated GitHub build and JetBrains Marketplace release workflows.

[Unreleased]: https://github.com/nawinds/poeditor/compare/v0.1.0...HEAD
[1.0.0]: https://github.com/nawinds/poeditor/releases/tag/v1.0.0
[0.1.0]: https://github.com/nawinds/poeditor/releases/tag/v0.1.0

