#!/bin/bash

. ./env.sh

docker run -it -p 8080:8080 \
    ${DOCKER_IMAGE}
