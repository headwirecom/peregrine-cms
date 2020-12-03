#!/bin/bash
# exit if any command fails
set -e 
echo "Building Sling"

SAVE_PWD=`pwd`

# Build Sling
cd peregrine-cms/sling/peregrine-builder-sling-12
mvn clean install --quiet

# Move Sling assets to target installation dir
mkdir -v -p /app/sling
mv -v target/dependency/org.apache.sling.feature.launcher.jar /app/sling
mv -v target/slingfeature-tmp/feature-oak_tar.json /app/sling
mv -v target/com.peregrine-cms.sling.launchpad-12-SNAPSHOT-oak_tar_fds_far.far /app/sling

cd ${SAVE_PWD}
