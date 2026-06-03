# Budget Manager API

A Spring Boot RESTful API designed to manage personal finances, track account balances, categorize transactions, and generate real-time financial summaries.

## Prerequisites

Before running the application, ensure you have the following installed:
* **Java 21** or higher
* **Maven 3.9+**
* **Docker** & **Docker Compose** 

---

## Initial Configuration

### 1. Database Setup
The application uses PostgreSQL running inside a Docker container. Spin up the database instantly using the provided Docker Compose file:

```bash
docker-compose up -d
```

### 2. Application Properties
Database credentials and settings are pre-configured in `src/main/resources/application.yml`.

*Note: If you want to enable or disable the automatic generation of demo data on startup, toggle the `app.seed-data` property (`true`/`false`) inside that file.*

---

## Running the Application

You can build and start the Spring Boot application using the Maven wrapper.

### Option 1: Run with Default Settings
Starts the API with the default configuration from `application.yml`:

```bash
./mvnw clean spring-boot:run
```

### Option 2: Run and Seed Demo Data Automatically
If you want to automatically populate the database with sample accounts, categories, and transactions on startup without modifying the configuration file, run:

```bash
./mvnw clean spring-boot:run -Dspring-boot.run.arguments="--app.seed-data=true"
```

The server will start locally at: `http://localhost:8080`

---

## API Documentation & Testing

Once the application is running, you can explore and test all REST endpoints via the Swagger UI interface:

👉 **Swagger UI URL:** `http://localhost:8080/swagger-ui/index.html`

*Note: When demo data seeding is enabled, the database will be automatically populated with sample accounts, categories, and transactions so you can start testing immediately.*
