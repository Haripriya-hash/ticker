# Ticker

A Spring Boot + Thymeleaf app built for the RTS Labs coding demonstration.

Users can sign up, log in, and log out. Once logged in, they can look up a stock
symbol and see today's opening price, pulled live from [Finnhub](https://finnhub.io).
The lookup form is only reachable when authenticated.

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
- Docker deployment support

## Project layout

```
src/main/java/com/rts/ticker
├── config/SecurityConfig.java        Spring Security rules (what's public vs protected)
├── controller/                       AuthController (signup/login), DashboardController (lookup),
│                                      ChangePasswordController, HomeController
├── dto/                              Form-backing objects + Finnhub response mapping
├── model/User.java                   JPA entity
├── repository/UserRepository.java
└── service/                          SignupService (signup + password change),
                                       CustomUserDetailsService, FinnhubService
src/main/resources/templates/         login.html, signup.html, dashboard.html, change-password.html
src/main/resources/static/css/        style.css
src/test/java/com/rts/ticker/         AuthFlowIntegrationTest, StockLookupIntegrationTest,
                                       ChangePasswordIntegrationTest
Dockerfile                            Used for deployment (see "Deploying" below)
```

## Running locally

> This uses plain `mvn` (assumes Maven is installed locally). If you'd rather
> commit a wrapper so CI/Render doesn't need Maven pre-installed, run
> `mvn -N wrapper:wrapper` once and commit the generated `mvnw`, `mvnw.cmd`,
> and `.mvn/` folder — then swap `mvn` for `./mvnw` in the commands below.

1. **Get a Finnhub API key** — sign up free at [finnhub.io](https://finnhub.io), copy your API key.

2. **Start Postgres** :

   ```bash
   docker run --name ticker-db -e POSTGRES_USER=ticker -e POSTGRES_PASSWORD=choose_your_own_password \
     -e POSTGRES_DB=ticker -p 5432:5432 -d postgres:16
   ```

   No Docker? Install Postgres directly (`brew install postgresql@16` on Mac,
   or the installer at [postgresql.org/download](https://www.postgresql.org/download/)
   on Windows) and create a `ticker` database/user manually instead.

3. **Set environment variables and run.** The values are the same everywhere 
   only the syntax for setting them differs by shell:

   **macOS / Linux (bash or zsh):**
   ```bash
   export FINNHUB_API_KEY=your_finnhub_api_key
   export DATABASE_URL=jdbc:postgresql://localhost:5432/ticker
   export DATABASE_USERNAME=ticker
   export DATABASE_PASSWORD=your_db_password

   mvn spring-boot:run
   ```

   **Windows (PowerShell):**
   ```powershell
   $env:FINNHUB_API_KEY="your_finnhub_api_key"
   $env:DATABASE_URL="jdbc:postgresql://localhost:5432/ticker"
   $env:DATABASE_USERNAME="ticker"
   $env:DATABASE_PASSWORD="your_db_password"

   mvn spring-boot:run
   ```

   **Windows (Command Prompt / cmd.exe):**
   ```cmd
   set FINNHUB_API_KEY=your_finnhub_api_key
   set DATABASE_URL=jdbc:postgresql://localhost:5432/ticker
   set DATABASE_USERNAME=ticker
   set DATABASE_PASSWORD=your_db_password

   mvn spring-boot:run
   ```

   > **Note:** all three of these only set the variables for the current
   > terminal session. Close the window and they're gone — you'll need to set
   > them again next time, or use the IDE approach below for something more
   > permanent.

   > **Security note:** the values above (`your_finnhub_api_key`,
   > `your_db_password`) are placeholders — pick your own real values and
   > never commit them to the repo. `application.properties` only reads these
   > from environment variables at runtime; nothing sensitive is hardcoded in
   > source, and `.gitignore` already excludes local `.env` files if you use one.


   The app starts on `http://localhost:8080`.

4. Visit `http://localhost:8080`, sign up, log in, try looking up a symbol
   like `AAPL` or `MSFT`, and try changing your password from the dashboard.

## Running the tests

```bash
mvn test
```

Tests use an in-memory H2 database, so no Postgres or network access is needed.
Covered happy paths:

- Signing up creates a user with a hashed (not plaintext) password
- Signing up with a duplicate username is rejected with a friendly error
- A signed-up user can log in and gets redirected to the dashboard
- An anonymous user is redirected to `/login` if they try to reach the dashboard
  or submit a stock lookup
- A logged-in user can submit a symbol and see the opening price rendered
  (Finnhub call is mocked so tests don't depend on the real API or a valid key)
- An unrecognized symbol shows a friendly error instead of crashing
- A logged-in user can change their password given the correct current password
- A password change is rejected if the current password is wrong, or if the
  new password and confirmation don't match
- An anonymous user cannot reach the change-password page

## Deploying (Render)


## Deploying (Render)

1. Push the repository to GitHub.
2. Create a PostgreSQL instance in Render.
3. Create a Docker-based Web Service from the repository.
4. Configure the following environment variables:

   - FINNHUB_API_KEY
   - DATABASE_URL
   - DATABASE_USERNAME
   - DATABASE_PASSWORD

5. Deploy the application.

## Summary

Ticker is a Spring Boot web application that demonstrates user authentication, secure password management, PostgreSQL persistence, external API integration, automated testing, and Docker-based deployment. Authenticated users can search for stock symbols and view opening-price data retrieved from Finnhub.



