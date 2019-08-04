FROM timescale/timescaledb:1.2.1-pg10
ENV LANG en_US.UTF-8
RUN  /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

ARG PG_HOME="/data/postgresql"
ENV PG_HOME=${PG_HOME}
RUN mkdir -p ${PG_HOME}
RUN mkdir -p ${PG_HOME}/data
RUN mkdir -p ${PG_HOME}/logs
RUN mkdir -p ${PG_HOME}/run
RUN mkdir -p ${PG_HOME}/run/10-main.pg_stat_tmp
RUN mkdir -p ${PG_HOME}/conf
RUN chown -R postgres.postgres ${PG_HOME}

COPY conf/* ${PG_HOME}/conf/
COPY start.sh /

ARG INIT_DB_DIR="/initdb"
RUN mkdir -p ${INIT_DB_DIR}
COPY init_db.sql ${INIT_DB_DIR}/

CMD chmod +x /start.sh && /bin/sh -c /start.sh
