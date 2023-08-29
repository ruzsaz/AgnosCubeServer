#!/bin/bash

cp ../../../../target/AgnosCubeServer-2.0.jar ./AgnosCubeServer.jar

docker build -t agnos-cube-server:2.0 .

rm ./AgnosCubeServer.jar
