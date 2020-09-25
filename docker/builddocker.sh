#!/bin/bash
#
# Updated     : 20 August 2020
# Description : Build script for Docker image

. env.sh

echo "Removing old build artifacts..."
rm files/*.xz files/*.jar

./fetchfiles.sh

if [ $# -eq 2 ]; then
  docker build \
      --build-arg PEREGRINECMS_BRANCH=$1 \
      --build-arg THEMECLEANFLEX_BRANCH=$2 \
      --tag=${DOCKER_IMAGE} .
else
  echo "Tip: You can change the branches used: `basename $0` <peregrine-cms branch> <themeclean-flex branch>"
  docker build --no-cache --tag=${DOCKER_IMAGE} .
fi
