# Installation Manual

This document details the step-by-step setup guides to launch MoodFlix.

## System Prerequisites
Ensure you have the following installed on your host system:
1. **Java JDK 21 (LTS)**
2. **PostgreSQL 15+**
3. **Maven 3.9+** (MoodFlix ships with a local Maven binary under `tools/` for easy execution)

---

## Step 1: Clone the Repository
```bash
git clone https://github.com/abhaykharat-bit/MoodFlix.git
cd MoodFlix
```

---

## Step 2: Database Creation
Log in to your PostgreSQL server and execute the initialization script:
```bash
psql -U postgres -f sql/database.sql
```
Connect to the `moodflix` database and apply the tables and seed logs:
```bash
psql -U postgres -d moodflix -f sql/schema.sql
psql -U postgres -d moodflix -f sql/seed.sql
```

---

## Step 3: Application Configuration
Edit the configuration settings in `src/main/resources/application.properties` or set environment variables:
```properties
db.host=localhost
db.port=5432
db.name=moodflix
db.user=postgres
db.password=YOUR_PASSWORD_HERE
```
Alternatively, set the database password via env property `MOODFLIX_DB_PASSWORD`.

---

## Step 4: Build & Execution

Choose one of the following methods to launch the application:

### Option A: The Simplified Batch Script (Recommended for Windows)
Simply double-click or run [run-moodflix.bat](file:///c:/College+Study/Projects/MoodFlix/Final_MoodFlix/run-moodflix.bat). This script compiles the project and generates a fully portable shaded JAR, launching it automatically:
```powershell
.\run-moodflix.bat
```
*(No external JavaFX SDK installation or path configuration is required since all dependencies are bundled inside the shaded JAR).*

### Option B: Run via Maven command line
Execute the Maven plugin command directly in your VS Code terminal to clean, compile, and run the project:
```powershell
.\tools\apache-maven-3.9.9\bin\mvn clean javafx:run
```

For testing:
```powershell
.\tools\apache-maven-3.9.9\bin\mvn test
```

