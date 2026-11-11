# 06. Database Design

## 6.1 Purpose

The database stores users, disasters, needs, verification decisions,
pledges, allocations, messages, and alerts.

The initial design follows the ER diagram in the project synopsis. Any
changes made during implementation should be reflected in this document
and in the actual SQL schema.

## 6.2 Initial entity list

  Entity              Purpose
  ------------------- ---------------------------------------------
  USERS               Stores application users and their roles
  DISASTERS           Stores disaster events
  NEEDS               Stores resource requirements
  NEED_VERIFICATION   Stores department verification decisions
  PLEDGES             Stores donor offers
  ALLOCATIONS         Stores quantities allocated from pledges
  MESSAGES            Stores communication associated with a need
  ALERTS              Stores department-issued alerts

## 6.3 Entity relationships

-   One user can create many needs.
-   One disaster can have many needs.
-   One need can have many verification records over time.
-   One department officer can perform many verification actions.
-   One need can receive many pledges.
-   One donor can create many pledges.
-   One pledge can have many allocation records.
-   One need can have many allocation records.
-   One need can have many related messages.
-   One user can send many messages and receive many messages.
-   One department user can issue many alerts.

The synopsis shows all actors stored in `USERS` with a role value. The
volunteer role can be added to the role model if volunteer functionality
is implemented.

## 6.4 Table specifications

### USERS

  Column          Suggested type   Notes
  --------------- ---------------- -----------------------------------------------
  user_id         BIGINT           Primary key
  mobile_number   VARCHAR          Unique login/contact identifier
  name            VARCHAR          Display name
  password_hash   VARCHAR          Hashed password, never plaintext
  role            VARCHAR          AFFECTED_PERSON, DONOR, DEPARTMENT, VOLUNTEER
  latitude        DECIMAL          Optional location
  longitude       DECIMAL          Optional location
  created_at      TIMESTAMP        Account creation time

The synopsis includes mobile number, name, role, latitude, longitude,
and created_at. `password_hash` is an implementation requirement for
secure authentication.

### DISASTERS

  Column        Suggested type   Notes
  ------------- ---------------- ----------------------------------
  disaster_id   BIGINT           Primary key
  name          VARCHAR          Disaster name
  type          VARCHAR          Flood, earthquake, cyclone, etc.
  description   TEXT             Details
  start_date    DATE/DATETIME    Start date
  end_date      DATE/DATETIME    Optional end date
  status        VARCHAR          Active, closed, etc.

### NEEDS

  -----------------------------------------------------------------------
  Column                  Suggested type          Notes
  ----------------------- ----------------------- -----------------------
  need_id                 BIGINT                  Primary key

  disaster_id             BIGINT                  FK to DISASTERS

  created_by              BIGINT                  FK to USERS

  category                VARCHAR                 Food, water, medicine,
                                                  etc.

  description             TEXT                    Requirement details

  quantity_required       DECIMAL                 Required quantity

  quantity_pledged        DECIMAL                 Accepted pledged
                                                  quantity, if this
                                                  aggregate is retained

  quantity_allocated      DECIMAL                 Allocated quantity

  quantity_remaining      DECIMAL                 Remaining requirement,
                                                  with a clearly defined
                                                  meaning

  urgency                 VARCHAR                 Critical, high, medium,
                                                  low

  status                  VARCHAR                 Need lifecycle state

  latitude                DECIMAL                 Need location

  longitude               DECIMAL                 Need location

  created_at              TIMESTAMP               Recommended audit field
  -----------------------------------------------------------------------

**Important design decision:** The synopsis contains `quantity_pledged`,
`quantity_allocated`, and `quantity_remaining`. Before coding, define
whether `quantity_remaining` means remaining to pledge, remaining to
allocate, or remaining to deliver. These are not always the same value.
Prefer calculating derived totals from pledge/allocation/delivery
records where practical, or document exactly how stored aggregates are
maintained.

Also store a resource unit such as `PACKET`, `LITRE`, `KG`, or `BOX`.
Quantities without units can be misleading. Add `unit` to NEEDS and
ensure pledges use a compatible unit.

### NEED_VERIFICATION

  Column            Suggested type   Notes
  ----------------- ---------------- --------------------------------------
  verification_id   BIGINT           Primary key
  need_id           BIGINT           FK to NEEDS
  department_id     BIGINT           FK to USERS
  status            VARCHAR          VERIFIED, REJECTED, NEEDS_CORRECTION
  remarks           TEXT             Officer comments
  verified_at       TIMESTAMP        Decision time

If verification history is retained, each decision should be a separate
row. Do not assume one need can have only one verification attempt.

### PLEDGES

  -----------------------------------------------------------------------
  Column                  Suggested type          Notes
  ----------------------- ----------------------- -----------------------
  pledge_id               BIGINT                  Primary key

  need_id                 BIGINT                  FK to NEEDS

  donor_id                BIGINT                  FK to USERS

  quantity_offered        DECIMAL                 Quantity offered

  quantity_allocated      DECIMAL                 Total allocated from
                                                  pledge

  status                  VARCHAR                 PENDING, ACCEPTED,
                                                  PARTIALLY_ALLOCATED,
                                                  FULLY_ALLOCATED,
                                                  CANCELLED

  created_at              TIMESTAMP               Creation time
  -----------------------------------------------------------------------

A pledge is an offer/commitment, not proof of delivery. Define whether
the pledge is immediately accepted or requires department review.

### ALLOCATIONS

  Column               Suggested type   Notes
  -------------------- ---------------- ---------------------------------------------
  allocation_id        BIGINT           Primary key
  pledge_id            BIGINT           FK to PLEDGES
  need_id              BIGINT           FK to NEEDS
  quantity_allocated   DECIMAL          Allocated amount
  status               VARCHAR          ALLOCATED, IN_TRANSIT, DELIVERED, CANCELLED
  allocated_at         TIMESTAMP        Allocation time

For accurate fulfillment, consider adding delivered quantity and
delivery confirmation metadata, or a separate delivery record/table. Do
not mark an allocation as delivered merely because it was allocated.

### MESSAGES

  Column        Suggested type   Notes
  ------------- ---------------- ----------------
  message_id    BIGINT           Primary key
  need_id       BIGINT           FK to NEEDS
  sender_id     BIGINT           FK to USERS
  receiver_id   BIGINT           FK to USERS
  message       TEXT             Message body
  sent_at       TIMESTAMP        Send time
  read_status   BOOLEAN          Read indicator

### ALERTS

  Column          Suggested type   Notes
  --------------- ---------------- -----------------------
  alert_id        BIGINT           Primary key
  department_id   BIGINT           FK to USERS
  title           VARCHAR          Alert title
  message         TEXT             Alert details
  latitude        DECIMAL          Optional center point
  longitude       DECIMAL          Optional center point
  radius_km       DECIMAL          Optional alert radius
  created_at      TIMESTAMP        Creation time

## 6.5 Relationship diagram

``` mermaid
erDiagram
    USERS ||--o{ NEEDS : creates
    DISASTERS ||--o{ NEEDS : contains
    NEEDS ||--o{ NEED_VERIFICATION : undergoes
    USERS ||--o{ NEED_VERIFICATION : performs
    NEEDS ||--o{ PLEDGES : receives
    USERS ||--o{ PLEDGES : makes
    PLEDGES ||--o{ ALLOCATIONS : funds
    NEEDS ||--o{ ALLOCATIONS : receives
    NEEDS ||--o{ MESSAGES : discusses
    USERS ||--o{ MESSAGES : sends
    USERS ||--o{ MESSAGES : receives
    USERS ||--o{ ALERTS : issues
```

## 6.6 Integrity rules

-   Every foreign key must reference an existing parent record.
-   Mobile numbers should be unique if used as the login identifier.
-   Quantities must be positive where an amount is being pledged or
    allocated.
-   A need must have a valid category, unit, urgency, and status.
-   Coordinates must be within valid latitude/longitude ranges.
-   Only verified, open needs may receive donor pledges.
-   Allocated quantity must not exceed the unallocated accepted pledge
    quantity.
-   Allocation must not exceed the need's remaining allocatable
    quantity.
-   Cancellation and correction operations must update aggregates
    consistently.

## 6.7 Index recommendations

Consider indexes on: - `USERS.mobile_number` - `NEEDS.status` -
`NEEDS.disaster_id` - `NEEDS.category` - `NEEDS.urgency` -
`NEEDS.created_by` - `PLEDGES.need_id` - `PLEDGES.donor_id` -
`ALLOCATIONS.need_id` - `ALLOCATIONS.pledge_id` - `MESSAGES.need_id` -
`NEED_VERIFICATION.need_id`

Add indexes based on actual query patterns and inspect query plans if
performance becomes an issue.

## 6.8 Transaction and concurrency considerations

Allocation should be performed in a transaction: 1. Lock the relevant
need row. 2. Lock the relevant pledge row. 3. Read current quantities
after acquiring locks. 4. Validate the requested quantity. 5. Insert the
allocation. 6. Update related totals/statuses. 7. Commit.

If any step fails, roll back the transaction.

Use InnoDB. Consider locking rows in a consistent order to reduce
deadlocks. Test simultaneous allocation attempts.

## 6.9 Decisions required before schema freeze

The team must agree on: - Whether quantity fields are stored aggregates
or calculated values. - Exact meaning of each remaining-quantity
field. - Whether a pledge requires officer acceptance. - Whether
multiple resource units are supported. - How partial delivery is
represented. - Whether cancelled pledges restore pledgeable capacity. -
Whether verification history is append-only. - Whether volunteer tasks
require additional tables.

The ER diagram is a starting model, not a substitute for these business
decisions.
