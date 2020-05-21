#!/bin/bash

. ./env.sh

echo "Using Docker image: ${DOCKER_IMAGE}"
 
docker run -it -p 8080:8080 \
    ${DOCKER_IMAGE}
