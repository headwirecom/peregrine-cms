#!/bin/bash

if [ -z ${DOCKER_IMAGE+x} ]; then
  export DOCKER_IMAGE=peregrinecms/peregrine-cms:sso-sans-auth-20200519r1
fi
