#!/bin/bash
. env.sh

git branch -v
git log -p -1

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
  docker build --tag=${DOCKER_IMAGE} .
fi
