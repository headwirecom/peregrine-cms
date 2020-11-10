#!/bin/bash


echo "Travis pull request env..."
env | grep "PULL_REQUEST"

if [ ${TRAVIS_PULL_REQUEST} != "false" ]; then
  echo "Pull request build detected"
  docker login -u "$DOCKER_USERNAME" -p "$DOCKER_PASSWORD"
  docker tag "$DOCKER_IMAGE" "${DOCKER_IMAGE}:${TRAVIS_PULL_REQUEST_BRANCH}"
  docker push ${DOCKER_IMAGE}:${TRAVIS_PULL_REQUEST_BRANCH}
fi
