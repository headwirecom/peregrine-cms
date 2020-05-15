#!/bin/bash
. env.sh

echo "Removing old build artifacts..."
rm files/*.xz files/*.jar

./fetchfiles.sh
docker build --tag=${DOCKER_IMAGE} .

