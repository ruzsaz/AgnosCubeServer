#!/bin/bash

TARGET_CONTAINER_NAME="agnos-cube-nginx:1.0"

docker build -t ${TARGET_CONTAINER_NAME} .
