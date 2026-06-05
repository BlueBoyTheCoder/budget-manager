# Budget Manager API - Setup Instructions

A Spring Boot RESTful API designed to manage personal finances.

## What it does
This project provides a comprehensive REST API to handle personal financial data, automatically managing balance calculations and budget tracking. The system is split into specialized modules, allowing you to fully manage and query your resources through the following endpoint groups:

### 1. Accounts (`/api/accounts`)
* **Get All Accounts (`GET`):** Retrieves a list of all bank accounts along with their current balances.
* **Get Account by ID (`GET`):** Fetches detailed information for a specific account using its unique identifier.
* **Create Account (`POST`):** Registers a new account (e.g., Main, Savings) with an initial balance.
* **Delete Account (`DELETE`):** Removes an account completely from the system.
* **Export Transactions (`GET`):** Generates and downloads a custom CSV file containing the full transaction history for a specific account.

### 2. Categories (`/api/categories`)
* **Get All Categories (`GET`):** Lists all available financial categories used to classify budgets.
* **Create Category (`POST`):** Adds a new custom category (including configuration for monthly spending thresholds or budget limits).

### 3. Transactions (`/api/transactions`)
* **Get Filtered Transactions (`GET`):** Dynamically searches and filters through financial logs using parameters such as start date (`from`), end date (`to`), or a specific `categoryName`.
* **Create Transaction (`POST`):** Logs a new income or expense, which automatically triggers balance recalculations across the affected account.
* **Delete Transaction (`DELETE`):** Removes an individual transaction record and reverts its impact on the account balance.

### 4. Summary (`/api/summary`)
* **Get Summary (`GET`):** Generates a real-time financial dashboard overview, aggregating data across accounts and flagging active budget warnings.
## Prerequisites

* **Java 21** or higher
* **Docker** & **Docker Compose**

---

## Running the Database (PostgreSQL)

Before starting the application, spin up the database inside a Docker container:
```bash
docker-compose up -d
```

---

## Running the Application

Choose one of the following commands in your terminal based on how you want to initialize your data:

### Option A: Standard Run
Starts the application while keeping your existing database data intact:
```bash
./mvnw clean spring-boot:run
```

### Option B: Run and Seed Demo Data
Populates the database with sample accounts and transactions **only if the database is currently empty**:
```bash
./mvnw clean spring-boot:run -Dspring-boot.run.arguments="--app.seed-data=true"
```

### Option C: Clear Database and Keep it Empty
Drops all existing tables and recreates a completely fresh, empty database schema with zero data:
```bash
./mvnw clean spring-boot:run -Dspring-boot.run.arguments="--spring.jpa.hibernate.ddl-auto=create --app.seed-data=false"
```

### Option D: Clear Database, Run and Seed Demo Data
Drops all existing tables, recreates the database schema from scratch, and automatically populates it with fresh test data:
```bash
./mvnw clean spring-boot:run -Dspring-boot.run.arguments="--spring.jpa.hibernate.ddl-auto=create --app.seed-data=true"
```

---

## API Testing & Documentation

Once started, the application is available at: 'http://localhost:8080'

* **Swagger UI (Interactive API Testing):** 'http://localhost:8080/swagger-ui/index.html'

---

## Running the Test Suite

To execute the entire suite of unit and integration tests, run:
```bash
./mvnw clean test
```

*Note: All tests (including Repository Integration Slice tests using \'@DataJpaTest\') use an isolated, in-memory embedded database context. This means you do NOT need to have the Docker container running to execute and pass the test suite.*