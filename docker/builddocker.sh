#!/bin/bash
#
# Updated     : 21 Janaury 2021
# Description : Build script for Docker image

. env.sh

./fetchfiles.sh

docker build --no-cache --tag=${DOCKER_IMAGE} .
