# DCL And Security Notes

## Purpose

This document explains how Database Control Language, or DCL, relates to CampusEnroll HA and why the current repository keeps security hardening at the documentation layer in S27.

It is intentionally honest:

- the project already has a working academic local environment
- it does not yet implement a full least-privilege runtime credential split
- the examples below are illustrative and are not applied automatically by the repo

## What DCL is

DCL stands for Data Control Language.

In practical PostgreSQL terms, DCL covers actions such as:

- creating roles
- granting privileges
- revoking privileges
- limiting which users can read, write, or administer data

Typical DCL goals:

- reduce blast radius
- enforce least privilege
- separate operational responsibilities
- avoid using one powerful shared account for everything

## Why least privilege matters

Least privilege means each runtime identity should receive only the minimum permissions it actually needs.

Why this matters in a production-grade system:

- a read-only process should not be able to write or delete data
- an application account should not automatically be a schema owner
- migration tooling should not share the same privileges as every runtime service
- accidental misuse becomes less destructive

For Database II, least privilege is important because security is part of database engineering, not a separate topic.

## Current local academic setup

CampusEnroll HA currently uses a simple local Docker-based environment.

Evidence:

- `.env` defines `POSTGRES_USER=campus`
- services connect to the same PostgreSQL instance
- the environment is optimized for repeatable academic setup and demo stability

Current characteristics:

- single local PostgreSQL user for the main application path
- local credentials stored in `.env`
- Docker local environment intended for coursework and demonstration
- no split between app reader, app writer, migration owner, backup operator, or reporting role

This is acceptable for the current academic delivery, but it is not the final production security model.

## Why the project did not change runtime credentials now

S27 is documentation-only by scope.

Changing runtime credentials at this late stage would require coordinated changes across:

- `.env`
- local PostgreSQL initialization and role creation
- service connection configuration
- possibly Compose startup or bootstrap steps
- presentation verification flow

That would create unnecessary delivery risk in exchange for limited short-term presentation value.

So the correct decision for this segment is:

- document the DCL topic clearly
- provide illustrative least-privilege examples
- avoid destabilizing the working demo stack

## Illustrative DCL examples only

The following SQL snippets are examples for classroom discussion.

They are not applied automatically by this repository.

### Example 1: create roles

```sql
CREATE ROLE app_readonly LOGIN PASSWORD 'change_me_readonly';
CREATE ROLE app_writer LOGIN PASSWORD 'change_me_writer';
```

### Example 2: grant database access

```sql
GRANT CONNECT ON DATABASE campusenroll TO app_readonly;
GRANT CONNECT ON DATABASE campusenroll TO app_writer;
```

### Example 3: grant schema usage

```sql
GRANT USAGE ON SCHEMA public TO app_readonly;
GRANT USAGE ON SCHEMA public TO app_writer;
```

### Example 4: grant read-only access

```sql
GRANT SELECT ON ALL TABLES IN SCHEMA public TO app_readonly;
```

### Example 5: grant write access

```sql
GRANT SELECT, INSERT, UPDATE ON ALL TABLES IN SCHEMA public TO app_writer;
```

### Example 6: revoke permissions

```sql
REVOKE INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public FROM app_readonly;
```

### Example 7: future-proof default privileges

```sql
ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT SELECT ON TABLES TO app_readonly;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT SELECT, INSERT, UPDATE ON TABLES TO app_writer;
```

## How these examples map to CampusEnroll HA

In a future production-style evolution, a safer role split could look like this:

- schema owner or migration role
  - creates and evolves tables
- application writer role
  - normal CRUD and transactional writes
- application read-only role
  - reporting, auditing, or read-focused tools
- backup operator role
  - controlled operational access for dumps or restore flows

That model is not implemented in the current repo because:

- the project uses one local PostgreSQL instance
- the delivery is academic and demo-oriented
- the verified runtime path matters more than late-stage credential churn

## What to say if asked about database security

Suggested answer:

`The current repository uses a simplified local academic PostgreSQL credential model so the demo stack stays reproducible. For Database II, we explicitly document DCL and least privilege as the next security hardening step. In production we would separate schema ownership, application write access, read-only access, and backup or operations roles instead of keeping one shared local account.`

## Trap-question answers

### Is the current repo using least privilege at runtime?

No. The current repo uses a simplified local academic credential model.

### Does that mean the project ignores security?

No. It means the current segment prioritizes reproducible local delivery while documenting how DCL and least privilege should be added in a safer future phase.

### Why not change the credentials now?

Because S27 is documentation-only and changing credentials would create avoidable integration risk across all services and demo commands.

### Is `.env` with local credentials acceptable?

For this academic local environment, yes. For production, no. Production should use role separation, secret management, and tighter operational controls.

## Optional verification commands

Review current local credential configuration:

```powershell
Get-Content .\.env
```

Review current service connection assumptions:

```powershell
rg -n "DATASOURCE_URL|DATASOURCE_USERNAME|DATASOURCE_PASSWORD" backend
```

Review current course-audit notes about DCL:

```powershell
rg -n "DCL|least privilege|role" docs/course-audit
```

## Bottom line

CampusEnroll HA does not claim production-grade database access control today.

What it does provide in S27 is:

- clear DCL coverage for the course
- accurate explanation of the current local tradeoff
- practical examples of how least privilege should be approached later
