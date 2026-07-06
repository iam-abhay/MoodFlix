# Changelog

All notable changes to the MoodFlix project will be documented in this file.

## [1.0.0] - 2026-07-06

### Added
- Created `DesignSystem` holding centralized JavaFX UI color and spacing variables.
- Created `ComponentFactory` implementing the Factory pattern to instantiate standard, styled controls.
- Configured PostgreSQL indexes for user details searches and compound mood/type queries.
- Written automated testing suite using JUnit 5 and Mockito to mock database connections.
- Integrated keyboard shortcuts for search focus (`Ctrl+S`), watchlist navigation (`Ctrl+W`), profile view (`Ctrl+P`), home page redirects (`Ctrl+H`), and safe logout confirmations (`Ctrl+L`).

### Changed
- Refactored `UserDashboardController` to route DB queries and suggestions on asynchronous thread pools, separating View and Controller logic.
- Restructured repository layout to place `src/` and `pom.xml` at the repository root level.
- Modernized user analytics panel to render within translucent card layouts.
- Updated database configurations to gracefully catch disconnection states instead of crashing JavaFX.
