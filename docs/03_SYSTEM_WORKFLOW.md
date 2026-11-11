# 03. System Workflow

## 3.1 Overview

ReliefGrid follows a controlled lifecycle:

**Need reported → Department verification → Verified need published →
Donor pledge → Allocation → Delivery confirmation → Fulfillment
closure**

The lifecycle is deliberately divided into stages. A submitted need is
not automatically trusted, a pledge is not automatically an allocation,
and an allocation is not proof of delivery.

## 3.2 Main workflow diagram

``` mermaid
flowchart TD
    A["Affected Person / Officer"] --> B["Submit Need"]
    B --> C["Need status: PENDING"]
    C --> D["Department reviews"]
    D --> E{"Verification decision"}
    E -->|Reject| F["REJECTED"]
    E -->|Request changes| G["NEEDS_CORRECTION"]
    E -->|Verify| H["VERIFIED / OPEN"]
    H --> I["Donor browses verified needs"]
    I --> J["Donor submits pledge"]
    J --> K["Validate pledge"]
    K --> L{"Valid and eligible?"}
    L -->|No| M["Reject with explanation"]
    L -->|Yes| N["Record PLEDGE"]
    N --> O["Authorized allocation"]
    O --> P["Record ALLOCATION"]
    P --> Q["Coordinate delivery"]
    Q --> R["Confirm delivered quantity"]
    R --> S{"Need fully fulfilled?"}
    S -->|No| T["Remain PARTIALLY_FULFILLED / OPEN"]
    T --> I
    S -->|Yes| U["FULFILLED / CLOSED"]
```

## 3.3 Workflow A: Need reporting

**Actor:** Affected Person or authorized Department Officer.

1.  User opens the report-need page.
2.  User selects a disaster, if the application is using disaster
    records.
3.  User enters resource category and description.
4.  User enters required quantity and unit.
5.  User selects urgency.
6.  User provides a location. The form should store latitude and
    longitude when available.
7.  User submits the form.
8.  The controller validates request data.
9.  The service applies business validation.
10. The DAO inserts the need.
11. The system displays a reference ID and pending status.

**Initial status:** `PENDING_VERIFICATION`.

A department officer may create a need on behalf of someone who cannot
access the application. The record should still identify the submitting
account and, where required, preserve who the request is for.

## 3.4 Workflow B: Department verification

**Actor:** Department Officer.

1.  Officer opens the pending-needs queue.
2.  Officer selects a need.
3.  Officer reviews description, quantity, category, urgency, location,
    and available supporting information.
4.  Officer chooses Verify, Reject, or Request Correction.
5.  The service checks that the officer is authorized.
6.  The system updates the need's status.
7.  A verification-history record stores the decision, officer, remarks,
    and timestamp.
8.  The need becomes discoverable to donors only when verified and open.

Verification decisions should be auditable. Avoid silently overwriting a
decision without preserving its history.

## 3.5 Workflow C: Donor discovery

**Actor:** Donor.

1.  Donor opens the verified-needs page.
2.  System lists eligible needs.
3.  Donor filters by category, urgency, disaster, or location.
4.  Donor opens a need to inspect its details.
5.  Donor sees the required quantity and current contribution progress.
6.  Donor chooses a quantity to pledge.

The donor listing should not expose private information about affected
individuals unnecessarily. Display the operational location and need
details required for coordination, not sensitive personal data.

## 3.6 Workflow D: Pledge submission

**Actor:** Donor.

1.  Donor submits a pledge against a need.
2.  Controller validates the request shape.
3.  Service checks donor identity and eligibility.
4.  Service checks that the need is verified and open.
5.  Service validates the quantity and resource unit.
6.  The database operation records the pledge.
7.  The system returns the pledge reference and current status.

A pledge represents an offer or commitment. It must not automatically be
displayed as delivered.

The team must decide whether pledges are accepted up to the outstanding
requirement or whether excess offers are allowed and queued. For the
MVP, the safer rule is to accept only pledge quantities within the
currently pledgeable outstanding quantity, with the check performed
transactionally.

## 3.7 Workflow E: Allocation

**Actor:** Authorized Department Officer.

1.  Officer reviews available pledges.
2.  Officer selects a pledge and quantity to allocate.
3.  System verifies that the pledge has sufficient unallocated quantity.
4.  System verifies that the need has sufficient unallocated
    requirement.
5.  System records the allocation.
6.  Pledge and need totals are updated in the same database transaction.
7.  The allocation receives a status such as `ALLOCATED` or
    `IN_TRANSIT`, depending on the implemented workflow.

Allocation must be atomic. If any required database update fails, all
changes in that transaction must roll back.

## 3.8 Workflow F: Delivery and fulfillment

1.  The allocated resource is delivered through the real-world relief
    process.
2.  An authorized officer records the delivered quantity and
    confirmation.
3.  The system updates delivery/fulfillment totals.
4.  If the required quantity is satisfied, the need becomes `FULFILLED`.
5.  Otherwise, it remains open or partially fulfilled.
6.  The dashboard and map reflect the updated state.

For stronger auditability, delivery confirmation should be stored as a
distinct event or status update rather than inferred merely from the
existence of an allocation.

## 3.9 Workflow G: Map visualization

1.  Frontend requests map-eligible need records from the backend.
2.  Backend returns only records permitted for public or authenticated
    map visibility.
3.  Each record includes an identifier, category, urgency, status,
    latitude, and longitude.
4.  Leaflet places a marker at each valid coordinate.
5.  Clicking a marker displays a concise need summary.
6.  Filters allow users to narrow the displayed needs.

Do not expose private phone numbers, passwords, or unnecessary personal
details in map responses.

## 3.10 Workflow H: Volunteer coordination (planned)

1.  Volunteer registers profile, skills, location, and availability.
2.  Department creates or publishes a relief task.
3.  Matching logic recommends suitable volunteers using availability,
    distance, and skill requirements.
4.  Officer assigns a volunteer.
5.  Volunteer accepts or declines.
6.  Volunteer updates task progress.
7.  Officer confirms task completion.

This workflow is an extension and should not delay the core
need-to-fulfillment workflow.

## 3.11 Key state distinctions

  Concept        Meaning
  -------------- ---------------------------------------------
  Need           A recorded requirement
  Verification   Department's review decision
  Pledge         Donor's offer or commitment
  Allocation     A quantity assigned from a pledge to a need
  Delivery       Physical transfer of the resource
  Fulfillment    Confirmed satisfaction of the requirement

These distinctions are central to the correctness of ReliefGrid.
