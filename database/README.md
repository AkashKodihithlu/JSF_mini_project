# Database Setup

This folder contains SQL scripts for the ReliefGrid database.

## Planned database

Database name: `reliefgrid`

## Setup

1.  Install and start MySQL.
2.  Create the database using the agreed schema script.
3.  Configure the application's database URL, username, and password in
    local configuration.
4.  Run the application and verify connectivity.

Example database creation:

``` sql
CREATE DATABASE reliefgrid;
```

The team should add the finalized `schema.sql` after agreeing on: -
Table definitions and data types. - Foreign keys and indexes. - Quantity
and unit semantics. - Status values. - Volunteer/task tables, if
included.

Do not commit real database passwords or production credentials.
