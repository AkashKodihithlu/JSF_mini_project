# 10. Team Responsibilities and Integration

## 10.1 Purpose

This document defines how the four-person team can divide implementation
while maintaining one coherent application.

## 10.2 Suggested ownership

The following is a proposed split. Replace Member 1--4 with the agreed
names.

  -----------------------------------------------------------------------
  Member                  Primary ownership       Main deliverables
  ----------------------- ----------------------- -----------------------
  Member 1                Authentication and      Registration, login,
                          shared application      roles, shared layout,
                          foundation              user DAO

  Member 2                Need reporting and      Need forms, need
                          verification            listing, verification
                                                  queue, verification
                                                  history

  Member 3                Pledges, allocation,    Pledge flow, allocation
                          and business logic      transaction, quantity
                                                  validation, matching
                                                  rules

  Member 4                Map, dashboard, and     Map display, map data
                          volunteer extension     integration, dashboard
                                                  visuals, volunteer
                                                  scope if time permits
  -----------------------------------------------------------------------

The team should assign one integration owner to review shared
configuration, dependencies, and merges. This does not mean that person
writes every module.

## 10.3 Shared responsibilities

Everyone must: - Understand the complete user workflow. - Follow the
agreed architecture. - Use the shared database schema. - Follow naming
conventions. - Test their changes. - Review pull requests. - Communicate
changes to shared models and routes.

## 10.4 Define contracts before parallel coding

Before module implementation, agree on: - Package names. - Model field
names and types. - Table and column names. - Status values. - Route
names. - Service method signatures where modules depend on one
another. - Error-handling conventions. - Shared JSP layout and
navigation. - Database connection configuration.

This prevents modules from being built against incompatible assumptions.

## 10.5 Integration sequence

1.  Create the shared project and confirm it runs.
2.  Configure database connectivity.
3.  Agree on and commit the schema.
4.  Integrate authentication and role checks.
5.  Integrate need creation and verification.
6.  Integrate pledge submission.
7.  Integrate transactional allocation.
8.  Integrate map and dashboard.
9.  Integrate planned modules only after the core workflow passes.

## 10.6 Definition of done

A module is not complete merely because its page opens.

A module is complete when: - Its agreed use cases work. - Input is
validated on the server. - Authorization is enforced. - Database changes
are correct. - Error paths are handled. - Relevant tests pass. - The
code is pushed to a feature branch. - A pull request is reviewed and
merged. - Documentation is updated where necessary.

## 10.7 Communication

Use GitHub issues for bugs and tasks. Use pull requests for code review.
Record major architecture decisions in the repository so that important
decisions do not live only in chat messages.

## 10.8 Avoiding integration problems

-   Do not independently rename shared database columns.
-   Do not commit different versions of the same configuration file
    without coordination.
-   Do not hardcode local machine paths or passwords.
-   Do not merge untested changes directly into `main`.
-   Notify the team before changing a shared model, route, or service
    contract.
