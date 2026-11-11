# 02. Requirements and Scope

## 2.1 Purpose

This document defines what ReliefGrid should do, which users it serves,
and what is included in the initial implementation. It is the reference
for deciding whether a feature belongs in the MVP or should be
postponed.

## 2.2 Functional requirements

### FR-01: User registration

The system shall allow a user to create an account with the required
identity and login information.

### FR-02: User authentication

The system shall authenticate users before allowing access to protected
functionality.

### FR-03: Role-based authorization

The system shall restrict operations according to the user's role.

### FR-04: Need submission

An affected person or authorized officer shall be able to submit a need
containing a resource category, description, required quantity, urgency,
and location.

### FR-05: Need review

A department officer shall be able to view pending needs and inspect
their details.

### FR-06: Need verification

A department officer shall be able to verify, reject, or request
correction of a submitted need. The decision and relevant remarks should
be recorded.

### FR-07: Verified need discovery

Donors shall be able to view needs that have passed verification and are
open for contributions.

### FR-08: Pledge submission

A donor shall be able to pledge a positive quantity of a resource
against an eligible need.

### FR-09: Pledge validation

The system shall validate the need status, resource category, quantity,
and applicable remaining capacity before accepting a pledge.

### FR-10: Allocation tracking

The system shall record how much of a pledge is allocated to a need.

### FR-11: Fulfillment tracking

The system shall allow authorized users to update and confirm
delivery/fulfillment status.

### FR-12: Need status visibility

Users shall be able to view the status of needs they are permitted to
access.

### FR-13: Map display

The system shall display eligible needs with valid geographical
coordinates on an interactive map.

### FR-14: Volunteer coordination

If included in the agreed scope, volunteers shall be able to register
availability and receive relief-task assignments.

### FR-15: Department monitoring

Department officers shall have a consolidated view of pending, verified,
urgent, partially fulfilled, and fulfilled needs.

## 2.3 Non-functional requirements

  -----------------------------------------------------------------------
  ID                      Requirement             Expected behavior
  ----------------------- ----------------------- -----------------------
  NFR-01                  Correctness             Quantities and statuses
                                                  remain consistent

  NFR-02                  Security                Passwords are not
                                                  stored as plain text

  NFR-03                  Authorization           Protected actions
                                                  enforce role checks

  NFR-04                  Reliability             Failed database
                                                  operations do not leave
                                                  partial updates

  NFR-05                  Usability               Forms provide clear
                                                  labels and validation
                                                  feedback

  NFR-06                  Maintainability         Code follows layered
                                                  architecture

  NFR-07                  Responsiveness          Main pages remain
                                                  usable on common
                                                  desktop and mobile
                                                  widths

  NFR-08                  Traceability            Important verification
                                                  and allocation actions
                                                  are recorded

  NFR-09                  Concurrency             Simultaneous allocation
                                                  requests cannot exceed
                                                  available quantity

  NFR-10                  Data integrity          Foreign keys and
                                                  database constraints
                                                  protect relationships
  -----------------------------------------------------------------------

## 2.4 MVP scope

The MVP should prove the central relief coordination workflow.

**Included:** - Registration and login. - Basic role-based access. -
Need creation. - Department verification. - Verified need listing. -
Donor pledging. - Allocation and fulfillment status tracking. - Quantity
validation and transaction safety. - Basic department dashboard. - Map
of open needs with valid coordinates.

## 2.5 Planned after MVP

-   Volunteer availability and task assignment.
-   Live dashboard updates using Spring WebSocket.
-   Advanced map filters and clustering.
-   Location-based notifications.
-   Reports and exports.
-   Messaging between relevant users.

## 2.6 Future scope

-   AI-assisted need classification.
-   Duplicate-request recommendations.
-   Predictive need forecasting.
-   Government disaster data integrations.
-   SMS and WhatsApp Business integration.
-   Advanced analytics and response-time measurement.

These features are not prerequisites for a successful MVP.

## 2.7 Out of scope for the initial release

-   Guaranteeing the authenticity of every donor or report.
-   Processing online payments.
-   Replacing official emergency response channels.
-   Automated physical dispatch or vehicle routing.
-   Machine-learning-based allocation.
-   Real government-system integration without authorization and API
    access.

## 2.8 Acceptance criteria for the core workflow

The core workflow is accepted when a test can demonstrate:

1.  Affected Person submits a need for 100 units.
2.  The need is initially marked pending.
3.  A Department Officer verifies it.
4.  A Donor can now view it.
5.  A donor submits a valid pledge.
6.  The application records the pledge without treating it as delivered.
7.  An authorized allocation is recorded.
8.  Delivery is confirmed through the agreed workflow.
9.  The need's remaining quantity and status reflect the recorded
    operations.
10. Invalid or concurrent operations cannot create an over-allocation.
