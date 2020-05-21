#!/bin/bash

if [ -z ${DOCKER_IMAGE+x} ]; then
  export DOCKER_IMAGE=peregrinecms/peregrine-cms:latest
fi
