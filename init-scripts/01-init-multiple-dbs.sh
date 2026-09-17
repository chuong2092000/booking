#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE booking_db;
    CREATE DATABASE user_db;
    CREATE DATABASE payment_db;
    CREATE DATABASE notification_db;
    CREATE DATABASE keycloak;
EOSQL

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "hotel_db" <<-EOSQL
    CREATE EXTENSION IF NOT EXISTS vector;
EOSQL