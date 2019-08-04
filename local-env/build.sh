#!/bin/bash

WORK_HOME=`pwd`

cd ${WORK_HOME}

docker-compose build
echo -e "\033[32mAll modules are built successfully!\033[0m"
