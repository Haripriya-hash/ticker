# Ticker

Ticker is a Spring Boot and Thymeleaf web application built for the RTS Labs coding demonstration.

Users can sign up, log in, and log out. Once authenticated, they can search for a stock symbol and view the current day's opening price retrieved from Finnhub.

## Stack

- Java 17, Spring Boot 3.3
- Spring Security (form login, BCrypt password hashing)
- Spring Data JPA + PostgreSQL (H2 for tests)
- Thymeleaf for server-rendered views
- Finnhub `/quote` API for stock data

## Features

- User registration, login, and logout
- Secure password storage with BCrypt
- Authenticated stock lookup dashboard
- Real-time stock opening prices via Finnhub
- Password change functionality
- PostgreSQL-backed persistence
- Integration and security testing
- AWS Elastic Beanstalk deployment

## Project Layout

```text
src/main/java/com/rts/ticker
├── config/SecurityConfig.java
├── controller/
│   ├── AuthController.java
│   ├── DashboardController.java
│   ├── ChangePasswordController.java
│   └── HomeController.java
├── dto/
├── model/User.java
├── repository/UserRepository.java
└── service/
    ├── SignupService.java
    ├── CustomUserDetailsService.java
    └── FinnhubService.java

src/main/resources/templates/
├── login.html
├── signup.html
├── dashboard.html
└── change-password.html

src/main/resources/static/css/
└── style.css

src/test/java/com/rts/ticker/
├── AuthFlowIntegrationTest.java
├── StockLookupIntegrationTest.java
└── ChangePasswordIntegrationTest.java
```

## Running Locally

### 1. Get a Finnhub API Key

Create a free account at https://finnhub.io and generate an API key.

### 2. Start PostgreSQL

```bash
docker run --name ticker-db \
  -e POSTGRES_USER=ticker \
  -e POSTGRES_PASSWORD=your_password \
  -e POSTGRES_DB=ticker \
  -p 5432:5432 \
  -d postgres:16
```

### 3. Configure Environment Variables and Run

#### macOS / Linux

```bash
export FINNHUB_API_KEY=your_finnhub_api_key
export DATABASE_URL=jdbc:postgresql://localhost:5432/ticker
export DATABASE_USERNAME=ticker
export DATABASE_PASSWORD=your_password

mvn spring-boot:run
```

#### Windows PowerShell

```powershell
$env:FINNHUB_API_KEY="your_finnhub_api_key"
$env:DATABASE_URL="jdbc:postgresql://localhost:5432/ticker"
$env:DATABASE_USERNAME="ticker"
$env:DATABASE_PASSWORD="your_password"

mvn spring-boot:run
```

#### Windows Command Prompt

```cmd
set FINNHUB_API_KEY=your_finnhub_api_key
set DATABASE_URL=jdbc:postgresql://localhost:5432/ticker
set DATABASE_USERNAME=ticker
set DATABASE_PASSWORD=your_password

mvn spring-boot:run
```

> Note: Use your own Finnhub API key and database credentials. Do not commit secrets to source control.

The application starts at:

```text
http://localhost:8080
```

After launching the application, create an account, sign in, search for a stock symbol such as `AAPL` or `MSFT`, and test the password change functionality.

## Running Tests

```bash
mvn test
```

Tests use an in-memory H2 database, so no Postgres or network access is needed.

Covered happy paths:

- Signing up creates a user with a hashed (not plaintext) password
- Signing up with a duplicate username is rejected with a friendly error
- A signed-up user can log in and gets redirected to the dashboard
- An anonymous user is redirected to `/login` if they try to reach the dashboard or submit a stock lookup
- A logged-in user can submit a symbol and see the opening price rendered (Finnhub call is mocked so tests don't depend on the real API or a valid key)
- An unrecognized symbol shows a friendly error instead of crashing
- A logged-in user can change their password given the correct current password
- A password change is rejected if the current password is wrong, or if the new password and confirmation don't match
- An anonymous user cannot reach the change-password page

## Deploying to AWS Elastic Beanstalk

### Build the Application

```bash
mvn clean package -DskipTests
```

### Deployment Steps

1. Create an Elastic Beanstalk environment using the **Java (Corretto 17)** platform.
2. Create or attach a PostgreSQL database.
3. Upload the generated JAR file.
4. Configure the following environment variables:

```text
FINNHUB_API_KEY
DATABASE_URL (format: jdbc:postgresql://<rds-endpoint>:5432/ebdb)
DATABASE_USERNAME
DATABASE_PASSWORD
PORT (set to 5000)
```

5. Deploy the application.

## Summary

Ticker is a Spring Boot web application that demonstrates user authentication, secure password management, PostgreSQL persistence, external API integration, automated testing, and deployment to AWS Elastic Beanstalk. Authenticated users can search for stock symbols and view opening-price data retrieved from Finnhub.
