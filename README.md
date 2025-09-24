# Habit Tracker

A simple Spring Boot application for tracking habits.  
This backend provides REST APIs to add, list, and delete habits.  

---

## 🚀 Features
- Add a new habit
- List all habits
- Delete a habit
- Uses **Spring Boot**, **Spring Data JPA**, and **H2 database**

---

## 🛠️ Tech Stack
- Java 17
- Spring Boot 3
- Maven
- H2 (in-memory database)
- IntelliJ IDEA (Community Edition)

---

## ⚙️ Running the Project

### Prerequisites
- Java 17+
- Maven (bundled with IntelliJ)

### Steps
1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/habit-tracker.git
   cd habit-tracker
   ```

2. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

3. Access the app:
   - API base URL: `http://localhost:8080/api/habits`
   - H2 Console: `http://localhost:8080/h2-console`

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
