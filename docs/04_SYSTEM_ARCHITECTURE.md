# 04. System Architecture

## 4.1 Architectural style

ReliefGrid will use a layered web application architecture based on
Spring MVC.

The primary request path is:

**Browser → Spring MVC Controller → Service → DAO → MySQL**

The response travels back through the controller to the selected JSP
view or, for data endpoints, as a structured response.

## 4.2 Architecture diagram

``` mermaid
flowchart TB
    U["Users: Affected Person / Donor / Officer / Volunteer"]
    V["Presentation: JSP, HTML, CSS, JavaScript"]
    C["Spring MVC Controllers"]
    S["Service Layer: Business Rules"]
    D["DAO Layer: Spring JDBC / JdbcTemplate"]
    DB[("MySQL Database")]

    U --> V
    V -->|"HTTP request"| C
    C --> S
    S --> D
    D --> DB
    DB --> D
    D --> S
    S --> C
    C -->|"JSP view or data response"| V
```

## 4.3 Presentation layer

**Technologies:** JSP, HTML, CSS, JavaScript.

Responsibilities: - Render pages and forms. - Display validation errors
and status messages. - Show dashboards and tables. - Submit user actions
to Spring MVC routes. - Render map markers using Leaflet. - Provide
client-side usability enhancements.

The frontend must not be the source of truth for quantities,
permissions, or workflow status. All critical validation belongs on the
backend.

## 4.4 Controller layer

**Technology:** Spring MVC.

Responsibilities: - Map URLs to application actions. - Receive form data
and request parameters. - Validate basic request input. - Call the
relevant service. - Select a JSP view or return structured data. -
Handle expected errors and user-facing messages.

Controllers should remain thin. They should not contain SQL queries or
complex allocation algorithms.

Example conceptual flow:

``` java
@PostMapping("/needs")
public String createNeed(
        @ModelAttribute NeedForm form,
        RedirectAttributes redirectAttributes) {

    needService.createNeed(form);
    redirectAttributes.addFlashAttribute(
        "message", "Need submitted successfully"
    );

    return "redirect:/needs/mine";
}
```

This is an illustrative Spring MVC example. Adapt it to the actual
project configuration and validation approach.

## 4.5 Service layer

Responsibilities: - Enforce business rules. - Coordinate multiple DAO
operations. - Apply authorization checks where appropriate. - Manage
transaction boundaries. - Calculate matching recommendations. - Validate
pledge and allocation quantities. - Coordinate state transitions.

Examples: - `NeedService` - `VerificationService` - `PledgeService` -
`AllocationService` - `MatchingService` - `VolunteerService`

Critical multi-step operations should be transactional.

## 4.6 DAO layer

**Technology:** Spring JDBC / JdbcTemplate.

Responsibilities: - Execute parameterized SQL. - Insert, update, delete,
and retrieve records. - Map result rows to Java model objects. - Keep
SQL and persistence logic separate from business rules.

Examples: - `UserDAO` - `NeedDAO` - `PledgeDAO` - `AllocationDAO` -
`DisasterDAO`

Use parameterized queries. Never concatenate untrusted user input into
SQL statements.

## 4.7 Model and form objects

Model classes represent persistent domain concepts, for example: -
`User` - `Disaster` - `Need` - `NeedVerification` - `Pledge` -
`Allocation` - `Message` - `Alert`

Form/request objects can be separate from database models when the
fields accepted from a browser differ from the fields stored internally.

## 4.8 Database layer

MySQL stores: - User accounts and roles. - Disaster records. - Needs and
verification history. - Pledges and allocations. - Messages and alerts.

Use primary keys, foreign keys, unique constraints, check constraints
where supported, and appropriate indexes.

## 4.9 Request lifecycle example: creating a need

1.  Browser submits the report form.
2.  `NeedController` receives the POST request.
3.  Controller validates the form.
4.  `NeedService` applies business rules and sets the initial status.
5.  `NeedDAO` inserts the record using JdbcTemplate.
6.  MySQL stores the need.
7.  Controller redirects to a confirmation or listing page.

## 4.10 Transaction boundary example: allocation

Allocation may involve: - Locking the need row. - Locking or validating
the pledge row. - Checking available quantities. - Inserting an
allocation. - Updating pledge totals. - Updating need totals.

These changes must commit together or roll back together.

A database transaction is not a substitute for validation. The service
must validate the requested operation while holding the appropriate
database locks.

## 4.11 Concurrency strategy

For the first implementation, use MySQL InnoDB transactions and
row-level locking with `SELECT ... FOR UPDATE` for critical allocation
operations.

Important rules: - Lock records in a consistent order to reduce deadlock
risk. - Perform quantity checks after acquiring the locks. - Update
totals and insert allocation records in the same transaction. - Keep
transactions short. - Handle transaction failures and retry only when
safe. - Add tests with simultaneous requests.

Optimistic locking with a version column is an alternative, but do not
mix multiple strategies without a clear reason.

## 4.12 Real-time architecture (planned)

Spring WebSocket / Spring Messaging may later publish events when: - A
need is verified. - A pledge is recorded. - An allocation changes. - A
need is fulfilled.

The database remains the source of truth. WebSocket messages notify
connected clients that data changed; clients should refresh or retrieve
the authoritative state.

## 4.13 Security architecture

-   Authenticate users before protected actions.
-   Enforce authorization on the server, not only by hiding UI elements.
-   Store password hashes, never plaintext passwords.
-   Use CSRF protection for browser form submissions when using Spring
    Security.
-   Validate all input.
-   Use parameterized SQL.
-   Avoid exposing sensitive personal data in public listings or map
    responses.
-   Keep secrets and database credentials out of Git.

## 4.14 Deployment view

For a traditional Spring MVC setup: - Build the web application as a WAR
where required. - Deploy to Apache Tomcat. - Configure the MySQL
connection. - Configure view resolution for JSP. - Keep
environment-specific settings outside committed source code.

If the team later chooses Spring Boot, document that as an explicit
architecture decision because packaging and configuration may change.
