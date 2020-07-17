#!/bin/bash

. ./env.sh

echo "Using Docker image: ${DOCKER_IMAGE}"
 
docker run -it -p 8888:80 -p 8080:8080 \
    -e APACHE_DOMAIN=${APACHE_DOMAIN} \
    -e APACHE_PROXY_URL=${APACHE_PROXY_URL} \
    -e PEREGRINE_SITE=${PEREGRINE_SITE} \
    -e PEREGRINE_HOSTNAME=${PEREGRINE_SITE} \
    ${DOCKER_IMAGE}
