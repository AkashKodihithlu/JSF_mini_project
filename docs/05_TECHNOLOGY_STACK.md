# 05. Technology Stack

## 5.1 Stack summary

  -----------------------------------------------------------------------
  Layer                   Selected technology     Purpose
  ----------------------- ----------------------- -----------------------
  Language                Java                    Core application and
                                                  business logic

  Web framework           Spring Framework /      Request handling and
                          Spring MVC              dependency management

  View                    JSP                     Server-rendered pages

  Frontend                HTML, CSS, JavaScript   Layout and browser
                                                  interactions

  Database                MySQL                   Persistent relational
                                                  data

  Data access             Spring JDBC /           SQL execution and row
                          JdbcTemplate            mapping

  Server                  Apache Tomcat           Runs the web
                                                  application

  Map library             Leaflet.js              Interactive map
                                                  interface

  Map data                OpenStreetMap           Base map tiles/data

  Geocoding               Nominatim               Convert location
                                                  descriptions to
                                                  coordinates, subject to
                                                  usage policy

  Real-time               Spring WebSocket /      Live notifications,
                          Spring Messaging        planned

  Version control         Git and GitHub          Collaboration and
                                                  source history
  -----------------------------------------------------------------------

## 5.2 Java

Java is the main programming language for: - Controllers. - Services. -
DAO classes. - Models and form objects. - Validation. - Matching and
prioritization logic. - Transactional allocation operations.

All core backend business logic should remain in Java.

## 5.3 Spring Framework and Spring MVC

Spring MVC handles HTTP requests and coordinates the web layer.

The team should understand: - Inversion of Control (IoC). - Dependency
Injection. - Spring beans and application context. -
DispatcherServlet. - Controllers and request mappings. - Model and
ModelAndView. - View resolution. - Form binding and validation. -
Exception handling.

Do not assume Spring Boot is being used. The project brief specifies
Spring Framework / Spring MVC. Confirm with the faculty whether Spring
Boot is permitted before selecting it.

## 5.4 JSP

JSP is used to render dynamic server-side pages.

Potential pages: - `login.jsp` - `register.jsp` - `dashboard.jsp` -
`reportNeed.jsp` - `pendingNeeds.jsp` - `verifiedNeeds.jsp` -
`needDetails.jsp` - `donorPledges.jsp` - `allocationDetails.jsp` -
`map.jsp`

Keep Java business logic out of JSP. Prefer controller-provided model
attributes and JSTL where configured.

## 5.5 HTML, CSS, and JavaScript

HTML structures forms and content. CSS handles layout and responsive
styling. JavaScript supports: - Form interactions. - Filters. - Map
behavior. - Fetching data from data endpoints. - Dynamic status updates.

Client-side validation improves usability but does not replace backend
validation.

## 5.6 MySQL

MySQL stores relational application data.

Use: - Primary keys and foreign keys. - Transactions. - InnoDB tables. -
Indexes for frequently filtered columns. - Appropriate decimal/integer
types. - Consistent timestamps. - Constraints for valid data.

Use a consistent unit for each resource category. For example, do not
compare kilograms with packets as if they were interchangeable.

## 5.7 Spring JDBC / JdbcTemplate

JdbcTemplate reduces repetitive JDBC code while retaining SQL control.

It supports: - Parameterized queries. - Updates and inserts. - Query
result mapping. - Batch operations. - Integration with Spring
transaction management.

Use DAO classes to encapsulate database operations.

## 5.8 Apache Tomcat

Tomcat hosts the Spring MVC web application in a traditional servlet
deployment.

The team must agree on: - Tomcat version. - Java version. - Build
tool. - WAR deployment configuration. - JSP view resolver settings.

All team members should use compatible versions.

## 5.9 Leaflet and OpenStreetMap

Leaflet provides: - Map rendering. - Markers. - Popups. - Layer
controls. - Map interaction.

OpenStreetMap provides map data/tiles through an appropriate tile
service. Follow the tile provider's usage policy and attribution
requirements.

## 5.10 Nominatim

Nominatim can geocode a place description into coordinates. Its public
service has usage policies and rate limits. Do not send uncontrolled
requests for every keystroke. For the MVP, manually entered coordinates
or a simple location-selection workflow may be sufficient.

## 5.11 Spring WebSocket (planned)

Use WebSocket only after the standard request/response workflow works.

Potential events: - `NEED_VERIFIED` - `PLEDGE_CREATED` -
`ALLOCATION_UPDATED` - `NEED_FULFILLED`

Clients must still obtain authoritative data from the backend.

## 5.12 Tools

Recommended development tools: - IntelliJ IDEA or Eclipse. - MySQL
Workbench or command-line MySQL client. - Postman for testing data
endpoints. - Git and GitHub. - Browser developer tools.

## 5.13 Build and dependency management

Use one build tool consistently across the team, preferably Maven if the
project setup permits it.

Commit the build descriptor and dependency versions. Do not commit
generated build output, IDE caches, local credentials, or
server-specific temporary files.

## 5.14 Technology decisions to confirm

Before implementation, record: 1. Java version. 2. Traditional Spring
MVC or Spring Boot permission. 3. Spring Framework version. 4. Tomcat
version. 5. Maven or Gradle. 6. JSP/JSTL configuration. 7. Spring JDBC
configuration. 8. MySQL version. 9. Authentication approach.
