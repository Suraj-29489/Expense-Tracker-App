# Changelog

All notable changes to the Personal Expense Tracker project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Established project foundation documentation and development roadmap.
- Initialized core configuration and context documents (`PROJECT_CONTEXT.md`, `DEVELOPMENT_PLAN.md`, `CHANGELOG.md`, `README.md`).
- Created 19 sequential development phase specification files in `phases/` (Phases 00 through 18).
- Implemented Phase 0 project foundation with Kotlin, Jetpack Compose, Material 3, and Gradle Kotlin DSL.
- Built minimal `MainActivity` application shell and verified build and deployment on Pixel 9 emulator.
- Implemented Phase 1 local persistence layer with Room SQLite, KSP, and Clean Architecture data/domain separation.
- Added `Expense` domain model (using `BigDecimal` for money and `Instant` for dates) and `ExpenseEntity` with minor currency unit integer storage (`amountInCents`).
- Implemented `ExpenseDao` with full CRUD, category filtering, date range queries, and total spending aggregation.
- Implemented thread-safe singleton `AppDatabase` and `ExpenseRepositoryImpl`.
- Added automated unit and Robolectric database integration tests with 100% passing results.
