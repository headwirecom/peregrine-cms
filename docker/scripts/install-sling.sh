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
mvn clean install

# Move Sling assets to target installation dir
mkdir -v -p /app/sling
mv -v target/dependency/org.apache.sling.feature.launcher.jar /app/sling
mv -v target/slingfeature-tmp/feature-oak_tar.json /app/sling
cd ../..

rm -rf ${DIR1}

echo "Starting Sling for the first time..."

/app/scripts/start.sh
# cd /app/sling && java -jar /app/sling/org.apache.sling.feature.launcher.jar \
#     -f /app/sling/feature-oak_tar.json \
#     -p /app/sling  \
#     -c /app/sling/launcher/cache &&

# # Wait for Sling to fully start up
# STATUS=$(curl -u admin:admin -s --fail  http://localhost:8080/system/console/bundles.json | jq '.s[3:5]' -c)
# if [ "$STATUS" != "[0,0]" ]; then
#   while [ "$STATUS" != "[0,0]" ]
#   do    
#     echo "Sling still starting. Waiting for all bundles to be ready.."
#     STATUS=$(curl -u admin:admin -s --fail  http://localhost:8080/system/console/bundles.json | jq '.s[3:5]' -c)
#     sleep 5
#   done
# fi
