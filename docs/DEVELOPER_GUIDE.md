# Developer Guide

Welcome to the developer guide for MoodFlix! This document outlines code compilation, styling guidelines, and automated testing procedures.

## Project Layout

- **`src/main/java/com/moodflix/view`**: GUI pages built programmatically using clean JavaFX layout components.
- **`src/main/java/com/moodflix/controller`**: Controllers coordinating inputs, live search debouncing, and back stack history.
- **`src/main/java/com/moodflix/service`**: DB operations and scored Intelligent recommendation algorithms.
- **`src/main/java/com/moodflix/util`**: Helper caches, styling tokens, and dialog templates.

---

## Styling Architecture & Design Tokens
MoodFlix follows a central design system. Avoid defining hardcoded hex values or padding sizes directly in new JavaFX layout scripts. Instead, use the tokens declared inside `com.moodflix.util.DesignSystem`:
- Fonts: `DesignSystem.FONT_FAMILY`, `FONT_SIZE_LG`, `FONT_SIZE_SM`
- Layout spacing: `DesignSystem.SPACING_SM`, `SPACING_MD`
- Borders & Radius: `DesignSystem.RADIUS_SM`, `RADIUS_MD`

---

## Running Automated Tests
MoodFlix runs tests headlessly using JUnit 5 and Mockito. To execute the tests, run:
```bash
.\mvnw test
```
The test coverage report will be generated under `target/site/jacoco/index.html`.

