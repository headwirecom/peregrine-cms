#!/bin/bash

usage () {
  echo "Usage: `basename $0` <sling repo> <sling branch>"
}

if [ $# -ne 2 ]; then
  usage
  exit 1
fi

REPO=$1
BRANCH=$2
DIR=sling-org-apache-sling-starter

echo "Fetching Sling from repo: ${REPO}"
echo "Building Sling using branch: ${BRANCH}"

# Build Sling
git clone ${REPO}
cd ${DIR}
git checkout ${BRANCH}
mvn clean install

# Move Sling assets to target installation dir
mkdir -v -p /app/sling
mv -v target/dependency/org.apache.sling.feature.launcher.jar /app/sling
mv -v target/slingfeature-tmp/feature-oak_tar.json /app/sling
cd ..

rm -rf ${DIR}
