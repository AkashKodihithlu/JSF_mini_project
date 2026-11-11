# 08. Business Logic and Rules

## 8.1 Purpose

This document defines the rules that must be enforced by the Java
service layer. The frontend may guide users, but the backend is
authoritative.

## 8.2 Need lifecycle

Suggested need statuses:

  Status                 Meaning
  ---------------------- -------------------------------------------------------------
  PENDING_VERIFICATION   Submitted and awaiting officer review
  NEEDS_CORRECTION       More information is required
  VERIFIED_OPEN          Verified and available for donor pledges
  REJECTED               Not approved
  PARTIALLY_FULFILLED    Some required quantity has been delivered
  FULFILLED              Required fulfillment condition has been met
  CLOSED                 Administratively closed, if this is distinct from fulfilled

Do not use `CLOSED` and `FULFILLED` interchangeably unless the team
intentionally defines them as the same state.

## 8.3 Verification rules

-   Only a Department Officer may verify or reject a need.
-   A need must be pending or awaiting correction before a new decision
    is made.
-   Verification must record the officer and timestamp.
-   Rejection should include a reason.
-   A donor-facing query must filter for verified, eligible needs.

## 8.4 Quantity terminology

Use these terms consistently:

-   **Required quantity:** Total amount requested.
-   **Pledged quantity:** Amount offered by donors.
-   **Allocated quantity:** Amount assigned from pledges to a need.
-   **Delivered quantity:** Amount confirmed as physically delivered.
-   **Outstanding quantity:** Amount still required to satisfy the need.

A useful conceptual formula is:

`Outstanding quantity = Required quantity - Delivered quantity`

However, the system may also need: - Unpledged quantity. - Unallocated
pledged quantity. - Allocated but not yet delivered quantity.

These are different measures. The team must not use one
`quantity_remaining` field to represent all of them.

## 8.5 Pledge rules

For the MVP: - The donor must be authenticated. - The need must be
verified and open. - Quantity must be greater than zero. - Resource
category and unit must be compatible. - The pledge must follow the
agreed cap on outstanding need. - Pledge creation must be protected
against concurrent submissions.

Decide whether pledges are accepted immediately or first require officer
approval. Document the choice and apply it consistently.

## 8.6 Allocation rules

For a pledge with an offered quantity:

`Unallocated pledge quantity = Quantity offered - Quantity already allocated`

For a need:

`Outstanding need quantity = Required quantity - Delivered quantity`

An allocation quantity must be: - Greater than zero. - No greater than
unallocated pledge quantity. - No greater than the need's allocatable
outstanding quantity, under the chosen allocation policy.

The allocation service must check and update the relevant records inside
one transaction.

## 8.7 Delivery and fulfillment rules

-   Allocation does not mean delivery.
-   Delivery must be recorded through an authorized action.
-   Partial delivery must be supported if the team chooses partial
    fulfillment.
-   The need becomes fulfilled only when the defined fulfillment
    condition is met.
-   Corrections to delivery records should preserve an audit trail.

## 8.8 Concurrency and race conditions

### Example

A need has 20 units remaining. Two allocation requests arrive at nearly
the same time, each attempting to allocate 15 units.

A naive implementation may allow both requests to read 20 before either
updates the database. This could allocate 30 units against 20.

### Required approach

Use a database transaction and row-level locks: 1. Begin transaction. 2.
Lock the relevant need row using `SELECT ... FOR UPDATE`. 3. Lock the
relevant pledge row in a consistent order. 4. Read the latest values. 5.
Validate the requested quantity. 6. Insert allocation record. 7. Update
associated totals/statuses. 8. Commit.

If validation fails, do not write any allocation. If a database
operation fails, roll back the transaction.

### Important implementation notes

-   Use InnoDB.
-   Keep the transaction short.
-   Lock records in a consistent order.
-   Handle deadlock exceptions appropriately.
-   Add integration tests that issue simultaneous allocation attempts.
-   Consider idempotency for operations that may be retried after a
    network timeout.

## 8.9 Rule-based prioritization

A simple recommendation score can be used to sort needs. For example:

-   Critical urgency receives the highest priority.
-   High urgency follows.
-   Medium urgency follows.
-   Low urgency follows.
-   Older needs may receive a tie-break preference.
-   Remaining quantity can be displayed to help donors understand
    shortages.

Do not claim the score is an objective measure of human need. It is a
transparent sorting aid and should be reviewable by department officers.

## 8.10 Donor-need matching

A first version can filter and rank needs using: 1. Resource category
compatibility. 2. Verified and open status. 3. Geographic proximity,
when coordinates exist. 4. Urgency. 5. Outstanding quantity.

The system should recommend rather than silently make real-world
decisions.

## 8.11 Volunteer matching (planned)

Potential factors: - Volunteer availability. - Distance to task
location. - Required skills. - Task urgency. - Transport or carrying
capacity, if collected.

For an MVP, use transparent rule-based filters and let an officer
confirm assignments.

## 8.12 Auditability

Record who performed important actions and when: - Need submission. -
Verification decision. - Pledge creation/cancellation. - Allocation. -
Delivery confirmation. - Task assignment/completion.

An audit history helps resolve disputes and explain how a need reached
its current status.
