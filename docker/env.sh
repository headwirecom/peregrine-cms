#!/bin/bash

if [ -z ${DOCKER_IMAGE+x} ]; then
  export DOCKER_IMAGE=peregrinecms/peregrine-cms:latest
fi

export PEREGRINE_SITE=themecleanflex
export APACHE_DOMAIN=localhost
export APACHE_PROXY_URL=http://localhost:8080/
