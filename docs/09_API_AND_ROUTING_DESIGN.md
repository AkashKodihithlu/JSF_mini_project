# 09. Application Routing and Endpoint Design

## 9.1 Purpose

This document proposes URL routes and controller responsibilities. These
are design conventions, not a claim that endpoints already exist.

Because the application is planned around Spring MVC and JSP, routes may
return JSP views. Data endpoints used by JavaScript may return JSON.

## 9.2 Route conventions

-   Use nouns for resource paths.
-   Use GET for displaying or retrieving data.
-   Use POST for form submissions and state-changing operations in the
    initial server-rendered application.
-   Protect routes with authentication and role authorization.
-   Validate all data on the server.
-   Use redirect-after-post for successful form submissions to avoid
    accidental duplicate submissions.

## 9.3 Authentication routes

  Method   Route         Purpose                Access
  -------- ------------- ---------------------- ---------------
  GET      `/login`      Display login          Public
  POST     `/login`      Authenticate           Public
  GET      `/register`   Display registration   Public
  POST     `/register`   Create account         Public
  POST     `/logout`     End session            Authenticated

Exact login/logout routes may be managed by Spring Security if it is
selected.

## 9.4 Need routes

  ------------------------------------------------------------------------------------------------
  Method            Route                                      Purpose           Access
  ----------------- ------------------------------------------ ----------------- -----------------
  GET               `/needs/new`                               Display need form Affected Person /
                                                                                 Officer

  POST              `/needs`                                   Submit need       Affected Person /
                                                                                 Officer

  GET               `/needs/mine`                              List own          Affected Person
                                                               submitted needs   

  GET               `/needs/{id}`                              View permitted    Authorized user
                                                               need details      

  GET               `/officer/needs/pending`                   List pending      Officer
                                                               needs             

  POST              `/officer/needs/{id}/verify`               Verify need       Officer

  POST              `/officer/needs/{id}/reject`               Reject need       Officer

  POST              `/officer/needs/{id}/request-correction`   Request changes   Officer
  ------------------------------------------------------------------------------------------------

## 9.5 Donor and pledge routes

  ------------------------------------------------------------------------------------
  Method            Route                          Purpose           Access
  ----------------- ------------------------------ ----------------- -----------------
  GET               `/donor/needs`                 Browse verified   Donor
                                                   needs             

  GET               `/donor/needs/{id}`            View need details Donor

  POST              `/donor/needs/{id}/pledges`    Create pledge     Donor

  GET               `/donor/pledges`               List own pledges  Donor

  POST              `/donor/pledges/{id}/cancel`   Cancel eligible   Donor, subject to
                                                   pledge            rules
  ------------------------------------------------------------------------------------

## 9.6 Allocation routes

  --------------------------------------------------------------------------------------------
  Method            Route                                  Purpose           Access
  ----------------- -------------------------------------- ----------------- -----------------
  GET               `/officer/pledges`                     Review pledges    Officer

  POST              `/officer/allocations`                 Create allocation Officer

  GET               `/officer/allocations`                 View allocations  Officer

  POST              `/officer/allocations/{id}/delivery`   Record delivery   Officer
  --------------------------------------------------------------------------------------------

## 9.7 Map data route

  ------------------------------------------------------------------------
  Method            Route              Purpose           Access
  ----------------- ------------------ ----------------- -----------------
  GET               `/api/needs/map`   Return            Public or
                                       map-eligible      authenticated, as
                                       needs             decided

  ------------------------------------------------------------------------

Example response shape:

``` json
[
  {
    "needId": 101,
    "category": "FOOD",
    "description": "Food packets required",
    "urgency": "CRITICAL",
    "status": "VERIFIED_OPEN",
    "quantityRemaining": 40,
    "unit": "PACKET",
    "latitude": 11.6854,
    "longitude": 76.1320
  }
]
```

This is an illustrative response contract. The actual field names must
match the implemented DTO and quantity definitions. Do not include
private user details in public map data.

## 9.8 Dashboard routes

  Method   Route                  Purpose                  Access
  -------- ---------------------- ------------------------ ---------------
  GET      `/officer/dashboard`   Officer dashboard        Officer
  GET      `/donor/dashboard`     Donor dashboard          Donor
  GET      `/user/dashboard`      General user dashboard   Authenticated

## 9.9 Error handling

Use consistent behavior for: - Invalid form input. - Unauthenticated
access. - Unauthorized role. - Missing record. - Invalid state
transition. - Quantity conflict. - Database failure.

For JSP pages, display safe and understandable error messages. For JSON
endpoints, return appropriate HTTP status codes and a structured error
body.

## 9.10 Route ownership

Each module owner should coordinate route names with the team before
implementation. Avoid duplicate mappings and avoid changing shared route
contracts without informing dependent module owners.
