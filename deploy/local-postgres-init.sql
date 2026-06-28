-- Run this script with a PostgreSQL superuser before starting the backend
-- when using a local PostgreSQL instance instead of Docker.
--
-- Example:
-- psql -U postgres -f deploy/local-postgres-init.sql

select 'create user stip with password ''stip'''
where not exists (select from pg_roles where rolname = 'stip')\gexec

select 'create database stip owner stip'
where not exists (select from pg_database where datname = 'stip')\gexec

\connect stip

create extension if not exists postgis;
