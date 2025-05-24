# SpringPlay

A basic CRUD application using Spring MVC REST, JdbcTemplate, Docker/Postgresql, and TestContainers.

## Project Overview

A small demo project that showcases the following:
- Spring Boot Rest Controllers using Spring MVC and taking advantage of ResponseStatus exception handling.
- Local Postgresql docker container with ConfigurationProperties loaded at runtime and initialised via Flyway.
- JDBCTemplate repository using RowMapper to map to model objects.
- Postgresql TestContainer with test data loaded via Flyway.
- Full e2e integration test using TestContainer.
- Slice tests for RestController and Jdbc.

## Technologies

- Java 21
- Spring Boot
- Spring Data JDBC
- Flyway
- Testcontainers
- Docker
- Spring MVC

## Getting Started

### Prerequisites

- JDK 21 or later
- Gradle 8.x or later (or use the included Gradle wrapper).
- Docker Desktop or equivalent.

### Building the Project

To build the project and run the test suite, run:

```bash
./gradlew build
```
## Running the Application
To run the application, use:

```bash
./gradlew bootRun
```

## Docker Support
This project includes Docker support through the [compose.yml](compose.yml) file, allowing you to containerise the application.
Running the above command will start the docker database container, but if you would like to run it independently, 
you can run:
```bash 
docker compose up -d
```
NB: The test suite uses testcontainers, so this container is only used while running the application and not for
running the tests.

## Test Requests
If you are using IntelliJ IDEA, you can test the API by executing the HTTP methods in the [test-requests.http](test-requests.http) file. 
