#!/usr/bin/env bash
set -Eeuo pipefail

PGDATA_DIR="${PGDATA:-/var/lib/postgresql/data}"
PRIMARY_HOST="${PRIMARY_HOST:?PRIMARY_HOST is required}"
PRIMARY_PORT="${PRIMARY_PORT:-5432}"
POSTGRES_USER="${POSTGRES_USER:?POSTGRES_USER is required}"
POSTGRES_DB="${POSTGRES_DB:?POSTGRES_DB is required}"
REPLICATION_USER="${REPLICATION_USER:?REPLICATION_USER is required}"
REPLICATION_PASSWORD="${REPLICATION_PASSWORD:?REPLICATION_PASSWORD is required}"
REPLICA_APP_NAME="${REPLICA_APP_NAME:-campusenroll-pg-replica}"

mkdir -p "$PGDATA_DIR"
chown -R postgres:postgres "$PGDATA_DIR"
chmod 700 "$PGDATA_DIR"

if [ ! -s "$PGDATA_DIR/PG_VERSION" ]; then
  # Bootstrap the standby from the primary only on an empty replica volume.
  echo "Waiting for primary PostgreSQL at ${PRIMARY_HOST}:${PRIMARY_PORT}..."
  until gosu postgres pg_isready -h "$PRIMARY_HOST" -p "$PRIMARY_PORT" -U "$POSTGRES_USER" -d "$POSTGRES_DB" >/dev/null 2>&1; do
    sleep 2
  done

  rm -rf "${PGDATA_DIR:?}/"*

  export PGPASSWORD="$REPLICATION_PASSWORD"
  gosu postgres pg_basebackup \
    --pgdata="$PGDATA_DIR" \
    --write-recovery-conf \
    --wal-method=stream \
    --checkpoint=fast \
    --host="$PRIMARY_HOST" \
    --port="$PRIMARY_PORT" \
    --username="$REPLICATION_USER"
  unset PGPASSWORD

  cat >> "$PGDATA_DIR/postgresql.auto.conf" <<EOF
primary_conninfo = 'host=${PRIMARY_HOST} port=${PRIMARY_PORT} user=${REPLICATION_USER} password=${REPLICATION_PASSWORD} application_name=${REPLICA_APP_NAME}'
hot_standby = 'on'
EOF

  chown postgres:postgres "$PGDATA_DIR/postgresql.auto.conf"
fi

exec /usr/local/bin/docker-entrypoint.sh postgres -c listen_addresses=* -c hot_standby=on
