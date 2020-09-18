#!/bin/bash

usage () {
  echo "Usage: `basename $0` <sling repo> <sling branch> <directory>"
}

if [ $# -ne 3 ]; then
  usage
  exit 1
fi

REPO=$1
BRANCH=$2
DIR1=$3
DIR2=peregrine-builder-sling-12
#DIR=sling-org-apache-sling-starter

echo "Fetching Sling from repo: ${REPO}"
echo "Building Sling using folder: ${DIR1}"
echo "Building Sling using branch: ${BRANCH}"


# Build Sling
git clone ${REPO}
cd ${DIR1}
git checkout ${BRANCH}
cd ${DIR2}
mvn clean install --quiet

# Move Sling assets to target installation dir
mkdir -v -p /app/sling
mv -v target/dependency/org.apache.sling.feature.launcher.jar /app/sling
mv -v target/slingfeature-tmp/feature-oak_tar.json /app/sling
cd ../..

rm -rf ${DIR1}

echo "Starting Sling for the first time..."

/app/scripts/start.sh
