# 13. Testing Strategy

## 13.1 Purpose

Testing verifies that ReliefGrid's workflows, access rules, database
operations, and quantity calculations behave correctly.

## 13.2 Testing levels

### Unit testing

Test service logic in isolation: - Need validation. - Status transition
rules. - Pledge quantity checks. - Matching and prioritization
calculations. - Allocation validation.

### DAO/integration testing

Test database operations against a test database: - Insert and retrieve
needs. - Verification history. - Pledge persistence. - Allocation
updates. - Foreign-key behavior. - Transaction rollback.

### Controller/web testing

Test: - Route mappings. - Form validation. - Redirects and views. -
Authentication requirements. - Role restrictions. - Error responses.

### End-to-end testing

Test the complete workflow from need submission to fulfillment.

## 13.3 Core test scenarios

  -----------------------------------------------------------------------
  ID                      Scenario                Expected result
  ----------------------- ----------------------- -----------------------
  T-01                    Submit valid need       Need saved as pending

  T-02                    Submit zero/negative    Rejected
                          quantity                

  T-03                    Donor views pending     Not shown as verified
                          need                    opportunity

  T-04                    Officer verifies need   Status changes and
                                                  history is recorded

  T-05                    Unauthorized user       Access denied
                          verifies need           

  T-06                    Donor pledges valid     Pledge recorded
                          quantity                

  T-07                    Donor pledges invalid   Rejected
                          quantity                

  T-08                    Allocate more than      Rejected
                          pledge balance          

  T-09                    Allocate more than need Rejected
                          capacity                

  T-10                    Database failure during Transaction rolls back
                          allocation              

  T-11                    Partial delivery        Need remains partially
                                                  fulfilled

  T-12                    Full delivery           Need becomes fulfilled

  T-13                    Map record has invalid  No invalid marker is
                          coordinates             rendered

  T-14                    User accesses another   Access follows
                          user's private record   authorization policy
  -----------------------------------------------------------------------

## 13.4 Concurrency test

Example: - A need has 20 units available for allocation. - Two requests
each attempt to allocate 15 units at nearly the same time. - Only an
allocation sequence that respects the available quantity may succeed. -
The final allocated amount must never exceed the allowed quantity.

Run this test against the actual transactional database configuration. A
unit test alone cannot prove row-locking behavior.

## 13.5 Security test checklist

-   Passwords are hashed.
-   Protected routes require authentication.
-   Role checks happen on the server.
-   SQL uses parameters.
-   User input is validated.
-   Sensitive details are not exposed in map/API responses.
-   Session logout works.
-   CSRF protection is enabled where applicable.
-   Secrets are not committed to Git.

## 13.6 Manual demonstration checklist

For the final demonstration: 1. Log in as an affected person. 2. Submit
a need. 3. Log in as a department officer. 4. Verify the need. 5. Log in
as a donor. 6. View the verified need and pledge a quantity. 7. Return
to the officer account. 8. Allocate the pledged quantity. 9. Record
delivery. 10. Show the updated need status and map/dashboard.

Use test data and clearly label it as demonstration data.
