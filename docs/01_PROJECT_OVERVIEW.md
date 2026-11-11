# 01. Project Overview

## 1.1 Project identity

**Project name:** ReliefGrid\
**Project type:** Web-based Disaster Relief Coordination System\
**Academic context:** Java Spring Framework Mini Project\
**Department:** Computer Science and Engineering, NMAM Institute of
Technology

## 1.2 Executive summary

ReliefGrid is intended to provide a centralized platform for
coordinating disaster-related requirements and relief resources. It
connects people reporting needs, donors offering resources, volunteers
supporting operations, and department officers overseeing the response.

The system records requirements in a structured format, supports
departmental verification, makes verified needs discoverable to donors,
tracks pledges and allocations, and provides visibility into fulfillment
progress. A map interface is intended to help users understand where
needs are concentrated.

The initial implementation will prioritize a reliable end-to-end
workflow over a large number of advanced features.

## 1.3 Problem statement

During natural disasters such as floods, earthquakes, cyclones, and
landslides, affected communities may urgently require food, drinking
water, medicines, blankets, temporary shelter, and other essentials.

Coordination can become fragmented when information is distributed
across calls, messaging groups, and independently maintained
spreadsheets. This creates several risks:

-   Multiple people may report the same requirement.
-   Requests may be incomplete or unverified.
-   Donors may not know which locations have the greatest unmet need.
-   Multiple contributors may coordinate the same requirement without a
    shared record.
-   A pledge may be mistaken for a completed delivery.
-   Relief authorities may lack a consolidated view of outstanding
    requirements.
-   It may be difficult to identify shortages by location and urgency.

ReliefGrid addresses the coordination and visibility problem by
maintaining a shared record of needs, verification decisions, pledges,
allocations, and fulfillment status.

## 1.4 Proposed solution

The application will allow an affected person, or an authorized officer
acting on their behalf, to submit a requirement. A department officer
reviews the submission and decides whether it can be published as a
verified need.

Donors can browse verified needs and pledge quantities. The application
records pledges separately from allocations and delivery confirmation.
Department officers can monitor progress and close requirements when
fulfillment is confirmed.

The map interface will display the geographical distribution of eligible
needs using stored latitude and longitude coordinates.

## 1.5 Goals

1.  Maintain a centralized record of disaster-related needs.
2.  Establish a verification step before needs are presented as
    verified.
3.  Allow multiple donors to contribute to one need through partial
    pledges.
4.  Prevent resource over-allocation through transactional backend
    operations.
5.  Make requirement status and fulfillment progress visible.
6.  Provide location-based visibility through a map.
7.  Keep the codebase modular and understandable for team development.

## 1.6 Success criteria

The MVP can be considered functionally successful when:

-   A user can register and log in.
-   Role restrictions prevent unauthorized actions.
-   An affected person can submit a need.
-   An officer can verify or reject the need.
-   A donor can view verified needs and submit a valid pledge.
-   The system rejects invalid quantities and prevents over-allocation.
-   Pledge, allocation, and fulfillment states are distinguishable.
-   A user can view the current status of a need.
-   Active needs with valid coordinates can be displayed on a map.

## 1.7 Actors and responsibilities

  -----------------------------------------------------------------------
  Actor                               Main responsibility
  ----------------------------------- -----------------------------------
  Affected Person                     Report a need and follow its
                                      progress

  Donor                               Offer resources against verified
                                      needs

  Department Officer                  Verify needs and supervise relief
                                      coordination

  Volunteer                           Support assigned relief tasks;
                                      detailed workflow is planned
  -----------------------------------------------------------------------

The synopsis identifies Affected Person, Donor, and Department as the
three primary actors. Volunteer functionality is included in the broader
project concept and should be treated as a separate module whose exact
scope is confirmed by the team.

## 1.8 Design principles

-   **Verification before publication:** Unverified requests must not
    appear as verified needs.
-   **Traceability:** Important state changes should be recorded.
-   **Quantity correctness:** Required, pledged, allocated, delivered,
    and remaining quantities must not be confused.
-   **Least privilege:** Users should only perform actions allowed by
    their role.
-   **Separation of concerns:** Controllers, services, DAOs, models, and
    views should have distinct responsibilities.
-   **Incremental delivery:** Complete and test the core workflow before
    adding advanced integrations.
-   **Honest status:** The interface must distinguish a pledge from an
    actual delivery.

## 1.9 Assumptions and limitations

-   The MVP is a coordination tool, not an emergency dispatch service.
-   The application depends on users or authorized officers entering
    reasonably accurate information.
-   The system cannot independently guarantee that a reported need or
    donor contribution is genuine.
-   Map coordinates and geocoding results may be approximate.
-   SMS, WhatsApp, AI/ML, predictive forecasting, and government-system
    integrations are not required for the first MVP.
-   The application should not be presented as an officially integrated
    government emergency system unless such integration is actually
    established.
