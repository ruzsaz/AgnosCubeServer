#!/bin/bash

TARGET_CONTAINER_NAME="ruzsaz/agnos-cube-server:2.0"

RELATIVE_SCRIPT_DIR=$( dirname -- ${BASH_SOURCE[0]} )
SCRIPT_DIR=$( cd -- ${RELATIVE_SCRIPT_DIR} &> /dev/null && pwd )

cd ${SCRIPT_DIR}

LATEST_JAR=$( ls -v ../../target/AgnosCubeServer*.jar | tail -n 1 )
echo ${LATEST_JAR}

cp ${LATEST_JAR} ./AgnosCubeServer.jar

docker build -t ${TARGET_CONTAINER_NAME} .

rm ./AgnosCubeServer.jar
