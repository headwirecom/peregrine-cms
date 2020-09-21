#!/bin/bash

echo "Building Sling"

SAVE_PWD=`pwd`

# Build Sling
cd peregrine-cms/sling/peregrine-builder-sling-12
mvn clean install --quiet

# Move Sling assets to target installation dir
mkdir -v -p /app/sling
mv -v target/dependency/org.apache.sling.feature.launcher.jar /app/sling
mv -v target/slingfeature-tmp/feature-oak_tar.json /app/sling

cd ${SAVE_PWD}

echo "Starting Sling for the first time..."

/app/scripts/start.sh
