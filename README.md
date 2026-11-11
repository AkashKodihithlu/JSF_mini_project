# ReliefGrid 🌍

### Disaster Relief Coordination System

[![Language](https://img.shields.io/badge/Language-Java-orange)]()
[![Backend](https://img.shields.io/badge/Backend-Spring%20MVC-green)]()
[![Database](https://img.shields.io/badge/Database-MySQL-blue)]()
[![Status](https://img.shields.io/badge/Status-In%20Development-yellow)]()

**ReliefGrid** is a web-based disaster relief coordination platform
designed to connect affected individuals, donors, volunteers, and relief
authorities through a centralized system.

The platform aims to improve disaster response by organizing resource
requirements, verifying requests, coordinating donations, tracking
resource allocation, and providing geographical visibility into unmet
needs.

## The Problem

During floods, earthquakes, cyclones, and other disasters, relief
coordination often depends on scattered phone calls, messaging groups,
and spreadsheets. This can lead to duplicate requests, uneven
distribution of supplies, poor visibility into shortages, and difficulty
tracking whether pledged resources have actually reached the people who
need them.

ReliefGrid proposes a structured platform where relief requirements and
contributions can be recorded, verified, coordinated, and monitored.

## Objectives

-   Centralize disaster-related resource requirements.
-   Allow relief authorities to review and verify submitted needs.
-   Connect donors with verified requirements.
-   Support partial pledges and controlled resource allocation.
-   Track the journey from requirement to delivery and fulfillment.
-   Coordinate volunteers and relief tasks.
-   Visualize active requirements and unmet needs geographically.
-   Give relief officers a consolidated view of response progress.

## Core Workflow

1.  An affected person or authorized officer submits a resource
    requirement.
2.  A department officer reviews and verifies the requirement.
3.  The verified requirement becomes visible to donors.
4.  Donors submit pledges for quantities they can provide.
5.  The system validates and records pledges and allocations.
6.  Relief personnel coordinate delivery.
7.  An authorized officer confirms fulfillment and closes the
    requirement.

## Key Features

-   **Need reporting:** Submit resource category, description, quantity,
    urgency, and location.
-   **Need verification:** Review, approve, reject, or request changes
    to submitted needs.
-   **Donor pledging:** Discover verified needs and pledge available
    resources.
-   **Resource allocation:** Track pledged, allocated, delivered, and
    remaining quantities.
-   **Volunteer coordination:** Maintain volunteer availability and
    coordinate relief tasks.
-   **Interactive map:** Display active needs using geographical
    coordinates.
-   **Fulfillment tracking:** Monitor the status of each requirement.
-   **Role-based access:** Provide role-appropriate functionality to
    system users.
-   **Department dashboard:** Monitor active needs, urgent shortages,
    pledges, and fulfillment.

## Technology Stack

  Area                   Technology
  ---------------------- ----------------------------------------
  Programming language   Java
  Backend framework      Spring Framework, Spring MVC
  View technology        JSP
  Frontend               HTML, CSS, JavaScript
  Database               MySQL
  Database access        Spring JDBC / JdbcTemplate
  Application server     Apache Tomcat
  Mapping                Leaflet.js, OpenStreetMap
  Geocoding              Nominatim (planned integration)
  Real-time updates      Spring WebSocket (planned enhancement)
  Version control        Git and GitHub

The core application logic is intended to be implemented in Java using
the Spring Framework. Python-based backend services and separate backend
frameworks are not part of the proposed core stack.

## Architecture at a Glance

ReliefGrid follows a layered architecture:

`JSP / HTML / CSS / JavaScript → Spring MVC Controller → Service Layer → DAO Layer (Spring JDBC) → MySQL`

The controller handles incoming requests, the service layer contains
business rules, the DAO layer performs database operations, and MySQL
stores application data.

## User Roles

-   **Affected Person:** Reports disaster-related requirements and
    tracks their status.
-   **Donor:** Discovers verified needs and pledges resources.
-   **Department Officer:** Verifies needs, oversees allocation, and
    monitors response progress.
-   **Volunteer:** Participates in relief tasks. Volunteer-specific
    workflows are part of the planned scope and will be finalized during
    implementation.

## Project Documentation

Detailed engineering documentation is available in [`docs/`](docs/):

-   [Project overview](docs/01_PROJECT_OVERVIEW.md)
-   [Requirements and scope](docs/02_REQUIREMENTS_AND_SCOPE.md)
-   [System workflow](docs/03_SYSTEM_WORKFLOW.md)
-   [System architecture](docs/04_SYSTEM_ARCHITECTURE.md)
-   [Technology stack](docs/05_TECHNOLOGY_STACK.md)
-   [Database design](docs/06_DATABASE_DESIGN.md)
-   [Module specifications](docs/07_MODULE_SPECIFICATIONS.md)
-   [Business logic](docs/08_BUSINESS_LOGIC.md)
-   [Application routing design](docs/09_API_AND_ROUTING_DESIGN.md)
-   [Team responsibilities](docs/10_TEAM_RESPONSIBILITIES.md)
-   [Development roadmap](docs/11_DEVELOPMENT_ROADMAP.md)
-   [Git collaboration guidelines](docs/12_GIT_COLLABORATION.md)
-   [Testing strategy](docs/13_TESTING_STRATEGY.md)

## Project Status

**Planning and initial development.** The first implementation milestone
is the application foundation: project configuration, Spring MVC request
handling, MySQL connectivity, and the first end-to-end feature.

Features marked as planned or future scope should not be considered
implemented until they are completed and tested.

## Team

Developed as a Java Spring Framework mini project by students of the
Department of Computer Science and Engineering, NMAM Institute of
Technology.

## Guiding Principle

**Make relief requirements visible, contributions accountable, and
fulfillment trackable.**
