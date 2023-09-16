#!/bin/bash

# This script's directory, relative to the running point
RELATIVE_SCRIPT_DIR=$( dirname -- ${BASH_SOURCE[0]} )

# This script's absolute directory
SCRIPT_DIR=$( cd -- ${RELATIVE_SCRIPT_DIR} &> /dev/null && pwd )

# Read the local configured variables
cd ${SCRIPT_DIR}
source ./env.txt

# Base directory of the package
GIT_ROOT_DIR=$( git rev-parse --show-toplevel )

LATEST_JAR=$( ls -v ${GIT_ROOT_DIR}/target/AgnosCubeServer*.jar | tail -n 1 )
echo ${LATEST_JAR}

cp ${LATEST_JAR} ./AgnosCubeServer.jar

docker build -t ${TARGET_CONTAINER_NAME} .

rm ./AgnosCubeServer.jar
