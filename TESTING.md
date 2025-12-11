# Testing Guide

This guide shows you how to test your changes before committing them.

## 🧪 Quick Test Checklist

### 1. Run Unit/Integration Tests (Safe - Uses H2 in-memory DB)

Tests use the `test` profile with H2 database, so they won't touch your Neon databases:

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=HabitsIntegrationTest

# Run with verbose output
./mvnw test -X
```

**Expected:** All tests should pass ✅

---

### 2. Test Development Profile (Local Development)

Test that the app connects to Neon Development DB:

```bash
# Start the app with dev profile
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

**What to check:**
- ✅ Application starts without errors
- ✅ No database connection errors in logs
- ✅ Look for: `Started HabitTrackerApplication in X seconds`
- ✅ Check logs for: `HikariPool-1 - Starting...` and `HikariPool-1 - Start completed`

**Test API endpoints (without creating users):**

**Option 1: Simple HTTP check (Recommended)**
```bash
# Just verify the server is responding (401 is fine - means server is up!)
curl -i http://localhost:8080/api/habits
# Expected: HTTP/1.1 401 Unauthorized (this confirms server is running)
```

**Option 2: Check server response without hitting database**
```bash
# Test any protected endpoint - 401 means server is working
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/users/me
# Expected: 401 (server is up and security is working)
```

**Option 3: If you need to test a real endpoint**
```bash
# Only use this if you need to test actual functionality
# First signup (creates user), then test other endpoints
curl http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'
```

**Stop the app:** Press `Ctrl+C`

---

### 3. Test Production Profile (Verify Configuration Only)

⚠️ **Warning:** Only test that the app *starts* with prod profile. Don't run full tests against production DB.

```bash
# Start with prod profile (just to verify config loads)
SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run
```

**What to check:**
- ✅ Application starts without errors
- ✅ No configuration errors
- ✅ Logs show production database connection
- ✅ Look for: `HikariPool-1 - Start completed`
- ✅ Look for: `Started HabitTrackerApplication`

**Note:** The prod profile uses `ddl-auto=validate` as the default (safe for production).
- ✅ **Safe**: `validate` only checks schema, never modifies it
- ✅ **Safe**: Won't drop tables, add columns, or modify data
- ⚠️ **Requires**: Tables must already exist (created during initial setup)
- 📚 See `PRODUCTION_SETUP.md` for initial setup instructions if tables don't exist yet

**Stop immediately after verifying startup:** Press `Ctrl+C`

---

### 4. Verify Configuration Files

Check that profiles are correctly configured:

```bash
# Check dev profile config
cat src/main/resources/application-dev.properties

# Check prod profile config  
cat src/main/resources/application-prod.properties

# Check base config
cat src/main/resources/application.properties
```

---

### 5. Quick Database Connectivity Test

Test database connection without starting full app:

```bash
# Test dev DB connection (using psql if you have it)
psql 'postgresql://neondb_owner:npg_b0LGhVcz3tym@ep-falling-dawn-a47u4kob-pooler.us-east-1.aws.neon.tech:5432/neondb?sslmode=require' -c "SELECT version();"

# Test prod DB connection (be careful!)
psql 'postgresql://neondb_owner:npg_b0LGhVcz3tym@ep-bitter-math-a4mqlu38-pooler.us-east-1.aws.neon.tech:5432/neondb?sslmode=require' -c "SELECT version();"
```

---

## 🚀 Recommended Testing Workflow

Before committing, run this sequence:

```bash
# 1. Run all tests (safe - uses H2)
./mvnw test

# 2. Test dev profile startup
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
# Wait for "Started HabitTrackerApplication", then Ctrl+C

# 3. Quick API test (optional - verifies server without creating users)
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run &
sleep 10
# Just check if server responds (401 is expected - means server is up!)
curl -s -o /dev/null -w "HTTP Status: %{http_code}\n" http://localhost:8080/api/habits
# Expected: HTTP Status: 401 (confirms server is running and security works)
pkill -f "spring-boot:run"
```

---

## 🐛 Troubleshooting

### Connection Refused / Timeout
- Check your internet connection
- Verify Neon database is accessible
- Check firewall settings

### Profile Not Active
- Verify: `SPRING_PROFILES_ACTIVE=dev` is set correctly
- Check logs for: `The following profiles are active: dev`

### Configuration Errors
- Ensure `application-dev.properties` and `application-prod.properties` exist
- Check for typos in database URLs
- Verify credentials are correct

---

## 📝 Notes

- **Tests are safe:** All tests use H2 in-memory database (`@ActiveProfiles("test")`)
- **Dev profile:** Safe to test fully - uses Development Neon DB
- **Prod profile:** Only verify startup, don't run full integration tests
- **Always test locally first:** Use dev profile for development work

