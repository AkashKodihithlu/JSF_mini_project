# 12. Git and GitHub Collaboration Guidelines

## 12.1 Purpose

These guidelines help four contributors work in one repository without
overwriting one another's changes.

## 12.2 Branch model

Recommended branches:

-   `main`: Stable, reviewed version suitable for demonstration.
-   `develop`: Integration branch for completed features.
-   `feature/authentication`
-   `feature/need-management`
-   `feature/verification`
-   `feature/pledges`
-   `feature/resource-allocation`
-   `feature/maps`
-   `feature/volunteers`

Create feature branches from the agreed base branch, normally `develop`.

## 12.3 Typical workflow

``` bash
git clone <repository-url>
cd ReliefGrid

git checkout develop
git pull origin develop

git checkout -b feature/need-management

# Make changes and test them

git status
git add .
git commit -m "feat: implement need submission"

git push -u origin feature/need-management
```

Then open a pull request from the feature branch into `develop`.

After review and successful testing, merge the pull request.

## 12.4 Commit message convention

Use clear, action-oriented messages.

Examples: - `feat: add need submission form` -
`feat: implement department verification` -
`fix: prevent invalid pledge quantity` -
`refactor: move allocation logic into service` -
`test: add allocation concurrency tests` -
`docs: update database design`

## 12.5 Pull request checklist

Before requesting review: - \[ \] Code compiles. - \[ \] Relevant tests
pass. - \[ \] No secrets or local configuration are committed. - \[ \]
Database changes are documented. - \[ \] Shared routes/models remain
compatible. - \[ \] Error handling is included. - \[ \] README or
documentation is updated if necessary. - \[ \] The pull request
describes what changed and how it was tested.

## 12.6 Code review rules

Reviewers should check: - Correctness of business rules. -
Authorization. - Input validation. - SQL parameterization. - Transaction
boundaries. - Naming and readability. - Impact on other modules. - Test
coverage.

## 12.7 Shared-file coordination

Coordinate changes to: - Maven `pom.xml`. - Spring configuration. -
Security configuration. - Database schema. - Shared model classes. -
Common JSP layouts. - Route definitions.

Do not resolve a conflict by blindly accepting one version. Understand
both changes and reconcile them.

## 12.8 Files that should not be committed

Examples: - Build output such as `target/`. - IDE-specific temporary
files unless intentionally shared. - Local server deployment
directories. - Database passwords. - API keys. - Personal environment
files. - Logs.

Use `.gitignore` and an example configuration file with placeholder
values where appropriate.

## 12.9 Repository protection

-   Restrict direct pushes to `main`.
-   Require pull-request review where GitHub settings permit.
-   Keep `develop` buildable.
-   Tag important demo or submission versions.
-   Never force-push shared branches without team agreement.
