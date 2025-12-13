# Habit Tracker

A simple Spring Boot application for tracking habits.  
This backend provides REST APIs to add, list, and delete habits.  

---

## 🚀 Features
- Add a new habit
- List all habits
- Delete a habit
- Uses **Spring Boot**, **Spring Data JPA**, and **PostgreSQL (Neon)**

---

## 🛠️ Tech Stack
- Java 17
- Spring Boot 3
- Maven
- PostgreSQL (Neon - cloud database)
- Cursor

---

## ⚙️ Running the Project

### Prerequisites
- Java 17+
- Maven (or use `./mvnw`)
- Neon database credentials (get from [Neon Dashboard](https://console.neon.tech/))

### Quick Start

1. **Create `.env` file** with your Neon database credentials:
   ```bash
   cp env.example .env
   # Edit .env with your actual credentials
   ```

2. **Run the application:**
   ```bash
   ./run-local.sh
   ```
   
   This script automatically loads your `.env` file and starts the app with the correct profile.

3. Access the app:
   - API base URL: `http://localhost:8080/api/habits`

### Environment Variables

The app uses environment variables for configuration. Create a `.env` file:

```bash
cp env.example .env
# Edit .env with your Neon database credentials
```

**Required variables:**
- `SPRING_DATASOURCE_URL` - Your Neon database connection string
- `SPRING_DATASOURCE_USERNAME` - Database username
- `SPRING_DATASOURCE_PASSWORD` - Database password
- `SPRING_PROFILES_ACTIVE=dev` - Set to `dev` for local development

**Security:** Never commit your `.env` file - it's gitignored. See [RENDER_DEPLOYMENT.md](RENDER_DEPLOYMENT.md) for production deployment.

---

## 📌 API Endpoints

| Method | Endpoint           | Description     | Example Body                                         |
|--------|--------------------|-----------------|------------------------------------------------------|
| GET    | `/api/habits`      | List all habits | —                                                    |
| POST   | `/api/habits`      | Add a new habit | `{ "name": "Exercise", "description": "Run daily" }` |
| DELETE | `/api/habits/{id}` | Delete by ID    | —                                                    |

---

## 📝 License
This project is licensed under the [MIT License](LICENSE) © 2025 Revati Mahajan.
