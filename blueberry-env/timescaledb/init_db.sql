/*
 * This script is used to set up the TimescaleDB-based storage for Blueberry.
 *
 * Here are what it mainly does:
 * 1) Create two new schemas:
 *     - blueberry: for Blueberry helper functions and hypertables
 *     - timescale: for functions provided by TimescaleDB extension
 * 2) Create TimescaleDB extension in timescale schema
 * 3) Create helper functions in blueberry schema
 * 4) Configure privileges for Bluebery writer & reader accounts
 *     - Blueberry writer: used by blueberry monitors and data analysis
 *     - Blueberry reader: used by public Blueberry account
 *
 * NOTE:
 * Because the privilege check in TimescaleDB isn't complete, some writable
 * TimescaleDB functions, e.g. drop_chunks(), can still be executed by normal
 * PostgreSQL read-only account. So we have to fine tune the privileges for
 * Blueberry reader account to allow it use some read-only TimescaleDB functions.
 *
 * The script MUST be executed by the superuser 'postgres'.
 *
 * This script is tested against TimescaleDB 1.2.1 (with PostgreSQL 10.6) and
 * may need to be adjusted when upgrading TimescaleDB.
 */

/*
 * The followings MUST be done before running this script.
 * - Create Blueberry writer account: writer
 * - Create Blueberry reader account: reader
 * - Create Blueberry metric database and connect to it
 *
 * The SQL statements below can be used in non-production env.
 */
-- CREATE USER writer WITH NOSUPERUSER NOINHERIT LOGIN PASSWORD '123456';
-- CREATE USER reader WITH NOSUPERUSER NOINHERIT LOGIN PASSWORD '123456';
-- ALTER USER reader SET default_transaction_read_only = on;
-- CREATE DATABASE blueberry

/*
 * Initialize schemas and TimescaleDB extension
 */
DO $$
  DECLARE BLUEBERRY_SCHEMA TEXT = 'blueberry';
  DECLARE TIMESCALE_SCHEMA TEXT = 'timescale';
  DECLARE DATABASE_NAME TEXT = current_database();
BEGIN
  RAISE INFO 'Initialize schemas and TimescaleDB extension';

  EXECUTE 'CREATE SCHEMA IF NOT EXISTS ' || BLUEBERRY_SCHEMA;
  EXECUTE 'CREATE SCHEMA IF NOT EXISTS ' || TIMESCALE_SCHEMA;
  EXECUTE 'ALTER DATABASE "' || DATABASE_NAME || '" SET search_path = ' || concat_ws(',', 'public', BLUEBERRY_SCHEMA, TIMESCALE_SCHEMA);

  EXECUTE 'CREATE EXTENSION IF NOT EXISTS timescaledb WITH SCHEMA ' || TIMESCALE_SCHEMA || ' CASCADE';
END;
$$;

/* ************************************************************
 * Configure privileges
 * ************************************************************/
DO $$
  DECLARE BLUEBERRY_SCHEMA TEXT = 'blueberry';
  DECLARE TIMESCALE_SCHEMA TEXT = 'timescale';
  DECLARE BLUEBERRY_WRITER TEXT = 'writer';
  DECLARE BLUEBERRY_READER TEXT = 'reader';
  DECLARE TARGET_SCHEMAS TEXT[] = ARRAY[BLUEBERRY_SCHEMA,
                                        TIMESCALE_SCHEMA,
                                        '_timescaledb_cache',
                                        '_timescaledb_catalog',
                                        '_timescaledb_config',
                                        '_timescaledb_internal',
                                        'timescaledb_information'
                                       ];
  DECLARE READER_ALLOWED_FUNCTIONS TEXT[] = ARRAY[TIMESCALE_SCHEMA || '.first',
                                           TIMESCALE_SCHEMA || '.last',
                                           TIMESCALE_SCHEMA || '.locf',
                                           TIMESCALE_SCHEMA || '.histogram',
                                           TIMESCALE_SCHEMA || '.time_bucket(interval, timestamp with time zone)',
                                           TIMESCALE_SCHEMA || '.time_bucket_gapfill(interval, timestamp with time zone, timestamp with time zone, timestamp with time zone)',
                                           TIMESCALE_SCHEMA || '.interpolate(smallint, record, record)',
                                           TIMESCALE_SCHEMA || '.interpolate(integer, record, record)',
                                           TIMESCALE_SCHEMA || '.interpolate(bigint, record, record)',
                                           TIMESCALE_SCHEMA || '.interpolate(double precision, record, record)',
                                           TIMESCALE_SCHEMA || '.interpolate(real, record, record)'
                                           ];

  DECLARE schema_name TEXT;
  DECLARE func TEXT;
BEGIN
  RAISE INFO 'Configure privileges for Blueberry writer/reader accounts';

  /* All newly created functions are executable to PUBLIC by default in PostgreSQL,
   * so they're executable to all accounts. Here we must revoke them explicitly
   * from PUBLIC and grant proper privileges to Blueberry writer/reader accounts.
   */
  FOREACH schema_name IN ARRAY TARGET_SCHEMAS
  LOOP
    EXECUTE 'REVOKE ALL ON SCHEMA ' || schema_name || ' FROM PUBLIC';
    EXECUTE 'REVOKE ALL ON ALL FUNCTIONS IN SCHEMA ' || schema_name || ' FROM PUBLIC';
  END LOOP;

  /* Configure privileges for Blueberry writer */
  FOREACH schema_name IN ARRAY TARGET_SCHEMAS
  LOOP
    EXECUTE 'GRANT ALL ON ALL TABLES IN SCHEMA ' || schema_name || ' TO ' || BLUEBERRY_WRITER;
    EXECUTE 'GRANT ALL ON SCHEMA ' || schema_name || ' TO ' || BLUEBERRY_WRITER;
    EXECUTE 'GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA ' || schema_name || ' TO ' || BLUEBERRY_WRITER;

    EXECUTE 'ALTER DEFAULT PRIVILEGES IN SCHEMA ' || schema_name || ' GRANT ALL ON TABLES TO ' || BLUEBERRY_WRITER;
    EXECUTE 'ALTER DEFAULT PRIVILEGES IN SCHEMA ' || schema_name || ' GRANT EXECUTE ON FUNCTIONS TO ' || BLUEBERRY_WRITER;
  END LOOP;

  EXECUTE 'GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO ' || BLUEBERRY_WRITER;

  /* Configure privileges for Blueberry reader */
  FOREACH schema_name IN ARRAY TARGET_SCHEMAS
  LOOP
    EXECUTE 'GRANT USAGE ON SCHEMA ' || schema_name || ' TO ' || BLUEBERRY_READER;
    EXECUTE 'GRANT SELECT ON ALL TABLES IN SCHEMA ' || schema_name || ' TO ' || BLUEBERRY_READER;
    EXECUTE 'ALTER DEFAULT PRIVILEGES IN SCHEMA ' || schema_name || ' GRANT SELECT ON TABLES TO ' || BLUEBERRY_READER;
  END LOOP;

  EXECUTE 'GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA ' || BLUEBERRY_SCHEMA || ' TO ' || BLUEBERRY_READER;
  FOREACH func IN ARRAY READER_ALLOWED_FUNCTIONS
  LOOP
    EXECUTE 'GRANT EXECUTE ON FUNCTION ' || func || ' TO ' || BLUEBERRY_READER;
  END LOOP;
END;
$$;

/* *********************************************************
 * Check privileges of the Blueberry accounts
 * *********************************************************/
DO $$
  DECLARE BLUEBERRY_SCHEMA TEXT = 'blueberry';
  DECLARE TIMESCALE_SCHEMA TEXT = 'timescale';
  DECLARE BLUEBERRY_WRITER TEXT = 'writer';
  DECLARE BLUEBERRY_READER TEXT = 'reader';
  DECLARE TARGET_SCHEMAS TEXT[] = ARRAY[BLUEBERRY_SCHEMA,
                                        TIMESCALE_SCHEMA,
                                        '_timescaledb_cache',
                                        '_timescaledb_catalog',
                                        '_timescaledb_config',
                                        '_timescaledb_internal',
                                        'timescaledb_information'
                                        ];
  DECLARE EXPECTED_SCHEMA_CNT INTEGER = array_length(TARGET_SCHEMAS, 1);
  DECLARE READER_ALLOWED_FUNCTIONS TEXT[] = ARRAY['first',
                                          'last',
                                          'locf',
                                          'histogram',
                                          'time_bucket',
                                          'time_bucket_gapfill',
                                          'interpolate'
                                         ];

  DECLARE r RECORD;
  DECLARE granted_cnt INTEGER = 0;
  DECLARE granted_funcs TEXT[];
  DECLARE func TEXT;
BEGIN
  /* Check privileges of Blueberry writer */
  RAISE INFO 'Check schema privileges of %', BLUEBERRY_WRITER;
  PERFORM nspname, regexp_matches(nspacl::TEXT, '('|| BLUEBERRY_WRITER || '=UC/postgres)')
  FROM pg_namespace
  WHERE nspname = ANY (TARGET_SCHEMAS);
  GET DIAGNOSTICS granted_cnt = ROW_COUNT;

  IF granted_cnt <> EXPECTED_SCHEMA_CNT THEN
    RAISE EXCEPTION 'ERROR: Unexpected number of schema privileges (expected %, actual %)', EXPECTED_SCHEMA_CNT, granted_cnt;
  END IF;
  RAISE INFO 'OK';

  /* Check privileges of Blueberry reader */
  RAISE INFO 'Check schema privileges of %', BLUEBERRY_READER;
  PERFORM nspname, regexp_matches(nspacl::TEXT, '('|| BLUEBERRY_READER || '=U/postgres)')
  FROM pg_namespace
  WHERE nspname = ANY (TARGET_SCHEMAS);
  GET DIAGNOSTICS granted_cnt = ROW_COUNT;

  IF granted_cnt <> EXPECTED_SCHEMA_CNT THEN
    RAISE EXCEPTION 'ERROR: Unexpected number of schema privileges (expected %, actual %)', EXPECTED_SCHEMA_CNT, granted_cnt;
  END IF;
  RAISE INFO 'OK';

  RAISE INFO 'Check function privileges of %', BLUEBERRY_READER;
  FOR r IN SELECT DISTINCT routine_name::TEXT
           FROM information_schema.routine_privileges
           WHERE grantee IN (BLUEBERRY_READER, 'PUBLIC') AND routine_schema = TIMESCALE_SCHEMA
  LOOP
    granted_funcs = array_append(granted_funcs, r.routine_name);
  END LOOP;

  FOREACH func IN ARRAY granted_funcs
  LOOP
    IF array_position(READER_ALLOWED_FUNCTIONS, func) < 1 THEN
      RAISE EXCEPTION 'ERROR: Unexpected privilege on function %', func;
    END IF;
  END LOOP;

  RAISE INFO 'OK';
END;
$$;
