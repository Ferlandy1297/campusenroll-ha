#!/usr/bin/env bash
set -Eeuo pipefail

# Allow the replica container to clone and stream from this demo primary.
cat >> "$PGDATA/pg_hba.conf" <<EOF
host replication ${REPLICATION_USER} all scram-sha-256
host ${POSTGRES_DB} ${POSTGRES_USER} all scram-sha-256
host all all all scram-sha-256
EOF

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<EOF
CREATE ROLE ${REPLICATION_USER} WITH REPLICATION LOGIN PASSWORD '${REPLICATION_PASSWORD}';
EOF
