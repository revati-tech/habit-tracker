# Production Database Setup Guide

## Initial Setup (First Time Only)

When setting up production for the first time, you need to create the database tables.

### Option 1: One-Time Setup with `update` (Recommended)

1. **Temporarily change** `application-prod.properties`:
   ```properties
   spring.jpa.hibernate.ddl-auto=update
   ```

2. **Run the app once** to create tables:
   ```bash
   SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run
   ```
   Wait for "Started HabitTrackerApplication", then stop (Ctrl+C)

3. **Change back** to `validate` in `application-prod.properties`:
   ```properties
   spring.jpa.hibernate.ddl-auto=validate
   ```

4. **Now production is safe** - it will only validate schema, never modify it.

### Option 2: Manual SQL Setup

Run the SQL schema creation manually in your Neon production database, then keep `validate` mode.

---

## Understanding `ddl-auto` Modes

| Mode | What It Does | Safe for Production? |
|------|--------------|----------------------|
| `validate` | Checks that tables exist and match entities. **Won't modify anything.** | ✅ **YES - Use this** |
| `update` | Adds new columns/tables if schema changed. **Won't drop data or tables.** | ⚠️ Use only for initial setup |
| `create` | **DROPS and recreates tables** - **WILL WIPE DATA!** | ❌ **NEVER use in production** |
| `create-drop` | Creates on startup, drops on shutdown - **WILL WIPE DATA!** | ❌ **NEVER use in production** |
| `none` | No schema management | ✅ Safe, but use with migrations |

---

## Current Production Configuration

Your production profile now uses `validate` mode, which means:
- ✅ **Safe**: Won't modify your database schema
- ✅ **Safe**: Won't drop tables or data
- ✅ **Safe**: Only validates that schema matches your entities
- ⚠️ **Requires**: Tables to already exist (created during initial setup)

---

## Schema Changes in Production

If you need to add new columns/tables in production:

1. **Option A**: Temporarily switch to `update`, run once, switch back to `validate`
   - ⚠️ **Note**: `update` won't wipe existing data - it only adds new columns/tables if schema changes
2. **Option B**: Use database migrations (Flyway/Liquibase) - **Recommended for production**
3. **Option C**: Run SQL migrations manually

---

## Quick Reference

**Initial Production Setup:**
```bash
# 1. Set ddl-auto=update in application-prod.properties
# 2. Run once
SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run
# 3. Change back to ddl-auto=validate
# 4. Done! Production is now safe.
```

**Normal Production Runs:**
```bash
# With ddl-auto=validate, this is safe - won't modify anything
SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run
```

