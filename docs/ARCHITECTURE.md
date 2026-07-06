# Application Architecture Overview

This document describes the design patterns and components powering MoodFlix.

## MVC Separation

MoodFlix adheres strictly to the Model-View-Controller design pattern to cleanly isolate GUI updates from database and recommendation logic.

```
       [ JavaFX GUI Views ]
             ^        |
             |        | Action Event / Key Binding
      Redraw |        v
       [ UserDashboardController ]
             |        |
   Data push |        | Async Task / Service Invocation
             |        v
       [ Recommendation / Auth Services ]
             ^        |
             |        | Hikari JDBC Query
      Row Map |        v
       [ PostgreSQL Database ]
```

- **Views**: Programmatic view classes construct layouts dynamically without blocking on background operations.
- **Controllers**: Coordinate data-binding and listen for keyboard controls (e.g. search fields, watchlist additions).
- **Services**: Execute database transactions, perform password hashing, and calculate recommendation priority scores.

---

## Performance Enhancements

- **Daemon Startup Pool**: App initialization runs database migration checks concurrently on a separate daemon thread, letting JavaFX boot the landing screen immediately.
- **Translucent Card Scaling**: Hover transformations use lightweight ScaleTransitions cached in hardware rendering threads.
- **JDBC Connection Pool**: Integrates HikariCP to reuse connection handles and eliminate bootstrapping overhead.
