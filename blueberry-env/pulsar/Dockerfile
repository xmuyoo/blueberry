FROM apachepulsar/pulsar:2.4.0
ENV LANG en_US.UTF-8

ARG BLUEBERRY_PULSAR_HOME="/blueberry-pulsar"
ARG PULSAR_HOME="/pulsar"

ENV BLUEBERRY_PULSAR_HOME=${BLUEBERRY_PULSAR_HOME}
ENV PULSAR_HOME=${PULSAR_HOME}
ENV PULSAR_STANDALONE_CONF=${PULSAR_HOME}/conf/standalone.conf
ENV PULSAR_LOG_CONF=${PULSAR_HOME}/conf/log4j2.yaml
ENV PULSAR_ROOT_LOGGER=INFO,ROLLINGFILE

RUN mkdir -p ${BLUEBERRY_PULSAR_HOME}/conf

COPY standalone.conf ${BLUEBERRY_PULSAR_HOME}/conf/
COPY log4j2.yaml ${BLUEBERRY_PULSAR_HOME}/conf/
COPY start.sh /

CMD chmod +x /start.sh && /bin/sh -c /start.sh