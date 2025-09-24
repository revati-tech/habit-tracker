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

Step 5: Run Your Spring Boot App

Open HabitTrackerApplication.java.

Right-click → Run HabitTrackerApplication.main()

Check the console for:

Started HabitTrackerApplication in X seconds


✅ At this point, you have a working Spring Boot project in IntelliJ Community Edition.