# Database Schema Design & Tuning

MoodFlix uses PostgreSQL to store user profiles, watchlist links, activity logs, and movie metadata.

## Table Structure & Relationships

```mermaid
erDiagram
    USERS ||--o{ WATCHLIST : manages
    CONTENT ||--o{ WATCHLIST : contains
    USERS ||--o{ ACTIVITIES : logs
    USERS ||--o{ FEEDBACK : reviews
    USERS ||--o{ MOOD_ENTRIES : tracks
    USERS ||--o{ FRIENDS : references

    USERS {
        int id PK
        string email UK
        string password_hash
        string role
        string display_name
    }
    CONTENT {
        int id PK
        string title
        string mood
        string type
        string link
        string description
    }
    ACTIVITIES {
        int id PK
        int user_id FK
        string title
        string mood
        timestamp activity_date
    }
    WATCHLIST {
        int id PK
        int user_id FK
        int content_id FK
        timestamp added_at
    }
```

---

## SQL Performance Indexes

Aggregated views (such as user analytics) and real-time partial lookups are optimized using the following indices:

1. **`idx_content_mood_type`**: Evaluates `mood` and `type` queries together, speeding up recommendation searches.
2. **`idx_content_title`**: Optimizes content name checks and duplicate watchlist validation queries.
3. **`idx_activities_analytics`**: Speeds up statistics aggregation queries.
4. **`idx_users_email`**: Accelerates credential lookups during authentication.
