#!/bin/sh

DATA_DIR=$PG_HOME/data
CONF_DIR=$PG_HOME/conf
LOG_DIR=$PG_HOME/logs
RUN_DIR=$PG_HOME/run
PG_CTL=`which pg_ctl`
POSTGRES="su postgres"

${POSTGRES} -c "${PG_CTL} init -D ${DATA_DIR}"
cp ${CONF_DIR}/* ${DATA_DIR}
mkdir -p ${DATA_DIR}/conf.d
chown -R postgres.postgres ${DATA_DIR} 

${POSTGRES} -c "${PG_CTL} start -D ${DATA_DIR} -l ${LOG_DIR}/postgresql-10-main.log"

sleep 5

PSQL=`which psql`
INIT_DB_DIR="/initdb"
DATABASE="blueberry";
WRITTER="writer";
READER="reader";
PASSWORD="123456";
test -f ${RUN_DIR}/10-main.pid
if [ $? -eq 0 ];then
    rm -rf /var/run/postgresql
    ln -s /data/postgresql/run /var/run/postgresql
    chown -R postgres.postgres /var/run/postgresql

    ${PSQL} -U postgres -c 'CREATE DATABASE blueberry;'
    ${PSQL} -U postgres $DATABASE -c 'DROP EXTENSION IF EXISTS timescaledb;'
    ${PSQL} -U postgres $DATABASE -c "CREATE USER ${WRITTER} WITH NOSUPERUSER NOINHERIT LOGIN PASSWORD '${PASSWORD}';";
    ${PSQL} -U postgres $DATABASE -c "CREATE USER ${READER} WITH NOSUPERUSER NOINHERIT LOGIN PASSWORD '${PASSWORD}';";
    ${PSQL} -U postgres $DATABASE -c "ALTER USER ${READER} SET default_transaction_read_only = on;";
    ${PSQL} -U postgres $DATABASE < ${INIT_DB_DIR}/init_db.sql

    echo "Start and initial database Successfully!"
fi

touch 1
tail -f 1 
