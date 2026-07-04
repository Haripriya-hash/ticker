# Ticker

A small Spring Boot + Thymeleaf app built for the RTS Labs coding demonstration.

Users can sign up, log in, and log out. Once logged in, they can look up a stock
symbol and see today's opening price, pulled live from [Finnhub](https://finnhub.io).
The lookup form is only reachable when authenticated.

## Stack

- Java 17, Spring Boot 3.3
- Spring Security (form login, BCrypt password hashing)
- Spring Data JPA + PostgreSQL (H2 for tests)
- Thymeleaf for server-rendered views
- Finnhub `/quote` API for stock data

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

2. **Start Postgres** (or point at any Postgres instance you already have):

   ```bash
   docker run --name ticker-db -e POSTGRES_USER=ticker -e POSTGRES_PASSWORD=choose_your_own_password \
     -e POSTGRES_DB=ticker -p 5432:5432 -d postgres:16
   ```

   No Docker? Install Postgres directly (`brew install postgresql@16` on Mac,
   or the installer at [postgresql.org/download](https://www.postgresql.org/download/)
   on Windows) and create a `ticker` database/user manually instead.

3. **Set environment variables and run.** The values are the same everywhere —
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

   **Using an IDE instead (IntelliJ, VS Code, Eclipse):** most IDEs let you
   set environment variables directly in the run configuration rather than
   the terminal, which persists across sessions:
   - **IntelliJ**: Run → Edit Configurations → select the Spring Boot config →
     find "Environment variables" → click the folder icon → add each
     key/value pair
   - **VS Code** (with the Java/Spring extensions): add an `env` block to
     `.vscode/launch.json` with the same four key/value pairs
   - **Eclipse**: Run → Run Configurations → Environment tab → Add

   The app starts on `http://localhost:8080`. Tables are created automatically
   (`spring.jpa.hibernate.ddl-auto=update`).

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

This repo includes a `Dockerfile`, which is the recommended path — Render's
"auto-detect" build sometimes defaults to a Node.js environment for repos
without an obvious language marker, which fails immediately since there's no
`mvn` binary in that environment. Using Docker sidesteps that entirely.

1. Push this repo to GitHub (the `Dockerfile` needs to be at the repo root).
2. In Render, create a **PostgreSQL** instance first.
3. Create a **Web Service** from the repo:
   - **Environment**: **Docker** — Render should auto-detect the `Dockerfile`
     at the root. If it doesn't offer Docker as an option, delete the service
     and recreate it; the environment/runtime choice appears to be locked in
     at creation time, not editable afterward in Settings.
   - **Environment variables** — from your Render Postgres instance's
     **Connections** section, note the *Internal Database URL*. It looks like
     `postgresql://USERNAME:PASSWORD@HOST/DBNAME` — split it apart rather than
     pasting it in directly, since Spring Boot's JDBC driver needs the pieces
     separated and prefixed with `jdbc:`:
     - `FINNHUB_API_KEY` — your Finnhub key
     - `DATABASE_URL` — `jdbc:postgresql://HOST/DBNAME` (no username/password
       embedded in this string — that caused a `URL must start with 'jdbc'`
       error, and later a "Driver claims to not accept jdbcUrl" error, the
       first two times through)
     - `DATABASE_USERNAME` — just the username portion
     - `DATABASE_PASSWORD` — just the password portion
   - Render sets `PORT` automatically; `application.properties` already reads it.
4. Deploy. First build takes several minutes (downloading a full Maven+JDK
   image layer). Once live, visit the Render URL, sign up, and confirm the
   stock lookup and password change both work end to end.
5. **Free tier note**: Render spins the service down after 15 minutes of
   inactivity, so a cold visit can take 30-60 seconds to wake back up. A free
   uptime monitor (e.g. [UptimeRobot](https://uptimerobot.com), pinging every
   5 minutes) keeps it warm if that matters for your use case.

## Notes on design choices

- Passwords are hashed with BCrypt via Spring Security; nothing is ever stored
  or logged in plaintext.
- The Finnhub call is isolated in `FinnhubService` so it's easy to mock in
  tests and easy to swap for a different data source later.
- `spring.jpa.hibernate.ddl-auto=update` is fine for a demo; a real production
  setup would use a migration tool like Flyway instead.
- CSS is hand-written (no Bootstrap) with a small ticker-tape motif at the top
  of each page as a nod to the subject matter.
