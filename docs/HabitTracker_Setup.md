Step 1: Ensure Java is Installed

Since you installed Homebrew, make sure you have OpenJDK 17:

brew install openjdk@17
echo 'export PATH="/usr/local/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
java -version


You should see something like:

openjdk version "17.0.x" ...

Step 2: Create a Spring Boot Project via Spring Initializr

We’ll generate the project externally since Community Edition doesn’t have the Spring Initializr wizard.

Open your browser and go to: https://start.spring.io/

Configure the project:

Project: Maven

Language: Java

Spring Boot: 3.x (latest stable)

Group: com.mahajan

Artifact: habit-tracker

Packaging: Jar

Java: 17

Dependencies (for basic Habit Tracker):

Spring Web

Spring Data JPA

H2 Database

Spring Boot DevTools (optional)

Click Generate, and a .zip file will download.

Extract the .zip file to a folder you want to work in.

Step 3: Open the Project in IntelliJ Community

Open IntelliJ Community Edition.

Click File → Open and select the extracted project folder.

IntelliJ will detect it as a Maven project and load dependencies. Wait until it finishes indexing.

Step 4: Verify Project Structure

You should see:

habit-tracker
├─ src
│  ├─ main
│  │  ├─ java/com/example/habittracker
│  │  │  └─ HabitTrackerApplication.java
│  │  └─ resources
│  │     └─ application.properties
└─ pom.xml

Step 5: Configure Database Connection

The project uses Neon PostgreSQL databases with Spring profiles:
- **Development**: Use `SPRING_PROFILES_ACTIVE=dev` for local development (connects to Neon Development DB)
- **Production**: Use `SPRING_PROFILES_ACTIVE=prod` for deployed/production (connects to Neon Production DB)

Database configurations are in:
- `src/main/resources/application-dev.properties` (Development)
- `src/main/resources/application-prod.properties` (Production)

Step 6: Run Your Spring Boot App

**Option 1: Via Cursor**
1. Open HabitTrackerApplication.java
2. Right-click → Run → Edit Configurations
3. Add VM options or Environment variables:
   - Environment variables: `SPRING_PROFILES_ACTIVE=dev`
4. Run HabitTrackerApplication.main()

**Option 2: Via Command Line**
```bash
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

Check the console for:

Started HabitTrackerApplication in X seconds


✅ At this point, you have a working Spring Boot project connected to Neon PostgreSQL.