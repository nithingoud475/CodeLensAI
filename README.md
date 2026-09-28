# CodeLens AI

CodeLens AI is a full-stack Java Spring Boot application that provides automated, AI-powered code reviews. It uses a local Ollama Large Language Model to securely analyze submitted source code for bugs, security vulnerabilities, performance bottlenecks, and code quality issues.

## Features
- **Local AI Privacy**: Code is sent to a locally running Ollama LLM, ensuring privacy.
- **Secure Authentication**: User registration and login protected by JWT and Spring Security.
- **Rich Editor**: Integrated Monaco Editor (from VS Code) for easy code formatting and submission.
- **Detailed Analytics**: A personalized dashboard tracking all your reviews and the severity of found issues.
- **Code Improvements**: AI provides suggested fixes and complete rewritten code blocks for improvements.

## Architecture & Tech Stack
- **Backend**: Java 21, Spring Boot 3.3.4, Spring Security, Spring Data JPA
- **Database**: MySQL 
- **Frontend**: HTML5, CSS3, JavaScript (Vanilla), Monaco Editor
- **AI Integration**: Ollama (`llama3` model by default)

## Prerequisites
1. **Java 21**: Ensure Java 21 JDK is installed.
2. **Maven**: Maven 3.8+ or use the included Maven wrapper.
3. **MySQL Server**: Ensure a MySQL server is running.
4. **Ollama**: Install Ollama from [ollama.com](https://ollama.com).

## Setup Instructions

### 1. Database Setup
Ensure MySQL is running on `localhost:3306`. By default, the application uses:
- Username: `root`
- Password: `password`

If your credentials differ, open `src/main/resources/application.yml` and update the `spring.datasource.password` field. The database `codelens_db` will be auto-created.

### 2. Ollama & LLM Setup
Start Ollama and pull the required model in your terminal:
```bash
ollama run llama3
```
Make sure Ollama is accessible at `http://localhost:11434`.

### 3. Application Startup
Compile and run the Spring Boot application using Maven:
```bash
mvn clean spring-boot:run
```
*(If you do not have maven installed globally, you can download it or run it from your IDE).*

### 4. Access the App
Open your web browser and navigate to:
```
http://localhost:8080
```
Register a new account, login, and start submitting code for review!

## API Overview
- `POST /api/auth/register`: Register a new user account.
- `POST /api/auth/login`: Authenticate and receive a JWT.
- `POST /api/reviews`: Submit code (Language & Source Code) for an AI review.
- `GET /api/reviews`: Fetch review history.
- `GET /api/reviews/{id}`: Fetch a specific review by ID.
- `GET /api/dashboard`: Fetch user dashboard statistics.
