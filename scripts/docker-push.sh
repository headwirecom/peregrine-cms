#!/bin/bash

DOCKER_IMAGE=peregrinecms/peregrine-cms

usage () {
  echo "Usage: `basename $0` <branch>"
  echo "  <branch>   Git branch)"
}

if [ $# -ne 1 ]; then
  usage
  exit 1
fi

branch=$1

echo  "Pushing tags for '${branch}' branch."

case "$branch" in
  master)
    docker tag "$DOCKER_IMAGE" "${DOCKER_IMAGE}:stable"
    docker push ${DOCKER_IMAGE}:stable
  ;;
  develop)
    docker tag "$DOCKER_IMAGE" "${DOCKER_IMAGE}:develop"
    docker tag "$DOCKER_IMAGE" "${DOCKER_IMAGE}:latest"
    docker push ${DOCKER_IMAGE}:develop
    docker push ${DOCKER_IMAGE}:latest 
  ;;
  develop-sling11)
    docker tag "$DOCKER_IMAGE" "${DOCKER_IMAGE}:sling11"
    docker push ${DOCKER_IMAGE}:sling11
  ;;
  *)
    echo "Branch not supported."
    exit 1
  ;;
esac


