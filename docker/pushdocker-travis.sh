#!/bin/bash
#
# Creates a Docker tag based on the PR #

echo "Travis pull request env..."
env | grep "PULL_REQUEST"

if [ ${TRAVIS_PULL_REQUEST} != "false" ]; then
  echo "Pull request build detected for PR: ${TRAVIS_PULL_REQUEST}"
  TAG="pr-${TRAVIS_PULL_REQUEST}"
  docker login -u "$DOCKER_USERNAME" -p "$DOCKER_PASSWORD"
  docker tag "$DOCKER_IMAGE" "${DOCKER_IMAGE}:${TAG}"
  docker push ${DOCKER_IMAGE}:${TAG}
fi
