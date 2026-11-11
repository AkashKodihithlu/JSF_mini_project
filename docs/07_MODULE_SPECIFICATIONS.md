# 07. Module Specifications

## 7.1 Purpose

This document defines the responsibility, users, inputs, outputs,
dependencies, and acceptance conditions of each application module.

## 7.2 Module 1: Authentication and User Management

**Purpose:** Identify users and control access.

**Users:** All roles.

**Functions:** - Register account. - Log in and log out. - Maintain
profile. - Identify role. - Enforce access restrictions.

**Backend components:** - `AuthController` - `AuthService` - `UserDAO` -
`User` - Security configuration or session-management component

**Database:** USERS.

**Acceptance conditions:** - Valid credentials allow login. - Invalid
credentials are rejected. - Passwords are stored as secure hashes. -
Protected routes require authentication. - Users cannot perform another
role's restricted actions.

## 7.3 Module 2: Need Reporting

**Purpose:** Record resource requirements.

**Users:** Affected Person; authorized Department Officer.

**Inputs:** - Disaster. - Resource category. - Description. -
Quantity. - Unit. - Urgency. - Location and coordinates.

**Backend components:** - `NeedController` - `NeedService` - `NeedDAO` -
`Need` - Need form/request object

**Database:** NEEDS, DISASTERS, USERS.

**Acceptance conditions:** - Valid submissions are saved. - Invalid or
non-positive quantities are rejected. - New needs start in the agreed
pending status. - Creator and timestamp are recorded. - Coordinates are
validated when supplied.

## 7.4 Module 3: Need Verification

**Purpose:** Allow department officers to review requirements.

**Users:** Department Officer.

**Functions:** - List pending needs. - View need details. - Verify. -
Reject. - Request correction. - Record remarks and verification history.

**Backend components:** - `VerificationController` -
`VerificationService` - `NeedVerificationDAO` - `NeedDAO`

**Database:** NEEDS, NEED_VERIFICATION, USERS.

**Acceptance conditions:** - Only authorized officers can make
decisions. - A decision is recorded with officer and timestamp. -
Rejected or pending needs are not shown as verified donor
opportunities. - Repeated review actions preserve history according to
the agreed model.

## 7.5 Module 4: Donor and Pledge Management

**Purpose:** Let donors discover needs and record contributions.

**Users:** Donor.

**Functions:** - Browse verified needs. - Filter by category, urgency,
and location. - View need details. - Submit pledge. - View own
pledges. - Cancel or update a pledge if permitted by the workflow.

**Backend components:** - `DonorController` - `PledgeController` -
`PledgeService` - `PledgeDAO` - `MatchingService`

**Database:** USERS, NEEDS, PLEDGES.

**Acceptance conditions:** - Donors see only eligible needs. - Pledge
quantities are positive and use compatible units. - Pledges are
associated with the authenticated donor. - Invalid or over-limit pledges
are rejected transactionally. - A pledge is not displayed as delivered.

## 7.6 Module 5: Resource Allocation and Fulfillment

**Purpose:** Track assignment of pledged resources and delivery.

**Users:** Department Officer; authorized relief personnel.

**Functions:** - Review pledges. - Allocate a pledge partially or
fully. - Track allocation status. - Record delivery. - Confirm
fulfillment. - Update need progress.

**Backend components:** - `AllocationController` - `AllocationService` -
`AllocationDAO` - `PledgeDAO` - `NeedDAO`

**Database:** NEEDS, PLEDGES, ALLOCATIONS.

**Acceptance conditions:** - Allocation does not exceed available pledge
quantity. - Allocation does not exceed remaining allocatable need
quantity. - Related writes succeed or roll back together. - Partial
fulfillment is supported. - A need is closed only when its fulfillment
condition is satisfied.

## 7.7 Module 6: Volunteer Management (planned)

**Purpose:** Coordinate people willing to assist with relief tasks.

**Users:** Volunteer; Department Officer.

**Functions:** - Volunteer profile. - Skills and availability. - Task
creation. - Volunteer recommendation. - Assignment. - Accept/decline. -
Task progress.

**Potential backend components:** - `VolunteerController` -
`VolunteerService` - `TaskController` - `TaskService` - Volunteer and
Task DAOs

**Additional database design:** Likely requires VOLUNTEER_PROFILE,
TASKS, and TASK_ASSIGNMENTS or equivalent entities. These are not in the
original eight-table ER diagram and must be designed if the module is
implemented.

## 7.8 Module 7: Interactive Map

**Purpose:** Display geographical needs.

**Users:** Donors and Department Officers, with visibility rules.

**Functions:** - Load map. - Fetch map-eligible needs. - Place
markers. - Show need summary in a popup. - Filter by category, urgency,
and status.

**Technologies:** Leaflet.js, OpenStreetMap, JavaScript, Spring MVC data
endpoint.

**Acceptance conditions:** - Only eligible records are returned. -
Records without valid coordinates are excluded from markers. - Popups
show operational details without exposing sensitive information. - Map
attribution and tile-service requirements are respected.

## 7.9 Module 8: Department Dashboard

**Purpose:** Provide an operational overview.

**Users:** Department Officer.

**Possible indicators:** - Pending verification count. - Verified open
needs. - Critical needs. - Total required quantity by category. -
Pledged quantity. - Allocated quantity. - Delivered quantity. -
Partially fulfilled needs. - Fulfilled needs.

All metrics must use clearly defined quantity semantics. Do not add
unlike units into a single total.

## 7.10 Module 9: Notifications and Messaging (planned)

**Purpose:** Communicate relevant status changes.

**Potential events:** - Need verified or rejected. - Pledge created or
updated. - Allocation created. - Delivery confirmed. - Task assigned.

**Possible implementation:** In-app alerts first; WebSocket updates
later. External SMS/WhatsApp integrations are future scope.

## 7.11 Module dependencies

-   Authentication is required by all protected modules.
-   Need reporting is required before verification.
-   Verification is required before donor discovery.
-   Pledging is required before pledge-based allocation.
-   Allocation and delivery tracking are required before fulfillment
    closure.
-   Map depends on need data and valid coordinates.
-   Volunteer management depends on task and volunteer data models.
-   Dashboard depends on accurate data from the other modules.
