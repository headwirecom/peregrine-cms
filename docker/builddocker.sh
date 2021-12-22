#!/bin/bash
#
# Updated     : 21 Janaury 2021
# Description : Build script for Docker image

. env.sh

export RUNMODE=$1
export DOCKER_IMAGE_WITH_TYPE=${DOCKER_IMAGE}-$2
if [ -z "$1" ]
  then
    export RUNMODE=none
    export DOCKER_IMAGE_WITH_TYPE=${DOCKER_IMAGE}
fi

echo build docker image for runmode ${RUNMODE} using tag ${DOCKER_IMAGE_WITH_TYPE}
docker build --tag=${DOCKER_IMAGE_WITH_TYPE} --build-arg runmode=$RUNMODE .
