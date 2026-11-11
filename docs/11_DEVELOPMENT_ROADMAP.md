# 11. Development Roadmap

## 11.1 Delivery strategy

Develop ReliefGrid in vertical slices. Each phase should produce a
working, testable part of the application rather than many disconnected
screens.

The following is a proposed six-week plan. Adjust it to the team's
actual schedule and faculty deadlines.

## 11.2 Week 1: Foundation

**Tasks** - Confirm traditional Spring MVC versus Spring Boot
permission. - Agree on Java, Spring, Tomcat, MySQL, and build-tool
versions. - Create shared GitHub repository. - Create Maven project if
selected. - Configure Spring MVC and JSP view resolution. - Configure
MySQL and Spring JDBC. - Create initial schema. - Implement a basic
controller and database-backed page.

**Deliverable:** A working Spring application that connects to MySQL and
renders a JSP page.

## 11.3 Week 2: Authentication and needs

**Tasks** - Registration and login. - Password hashing. - Role checks. -
Need submission form. - Need DAO and service. - Pending-needs listing.

**Deliverable:** Users can log in and submit needs.

## 11.4 Week 3: Verification and donor pledges

**Tasks** - Department verification queue. -
Verify/reject/request-correction workflow. - Verification history. -
Verified-needs donor listing. - Pledge submission and validation.

**Deliverable:** Verified needs can receive donor pledges.

## 11.5 Week 4: Allocation and map

**Tasks** - Allocation transaction. - Quantity validation. - Partial
allocation. - Delivery status workflow. - Leaflet map. - Map data
endpoint.

**Deliverable:** Resource allocation is traceable and active needs
appear on a map.

## 11.6 Week 5: Dashboards and enhancements

**Tasks** - Department dashboard. - Donor pledge history. - Filters and
sorting. - Volunteer module if feasible. - WebSocket proof of concept if
core workflow is stable.

**Deliverable:** Integrated dashboards and selected enhancements.

## 11.7 Week 6: Testing and finalization

**Tasks** - Test every role. - Test invalid state transitions. - Test
concurrent allocation. - Test database constraints. - Test map
behavior. - Fix defects. - Improve UI and responsive layout. - Prepare
screenshots, demo, and documentation.

**Deliverable:** Demonstrable, tested MVP.

## 11.8 Priority order

1.  Project runs locally.
2.  Database connectivity works.
3.  Authentication and authorization work.
4.  Need reporting works.
5.  Verification works.
6.  Pledges work.
7.  Allocation and quantity correctness work.
8.  Fulfillment works.
9.  Map works.
10. Enhancements.

## 11.9 Milestone gates

Do not begin complex integration until the previous milestone has a
working demonstration.

**Gate 1:** Spring MVC + JSP + MySQL works.\
**Gate 2:** Need submission and verification work.\
**Gate 3:** Donor pledging works.\
**Gate 4:** Allocation is transactionally safe.\
**Gate 5:** Map and dashboards are integrated.\
**Gate 6:** Testing and demo are complete.

## 11.10 Scope protection

If time becomes limited, preserve the
need-to-verification-to-pledge-to-allocation-to-fulfillment workflow.
Postpone AI, external messaging, predictive analytics, and advanced
volunteer matching before compromising core correctness.
