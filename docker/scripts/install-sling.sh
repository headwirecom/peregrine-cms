#!/bin/bash
# exit if any command fails
set -e 

SAVE_PWD=`pwd`

mkdir -v -p /app/sling
mv -v /app/binaries/org.apache.sling.feature.launcher.jar /app/sling
mv -v /app/binaries/feature-oak_tar.json /app/sling
mv -v /app/binaries/com.peregrine-cms.sling.launchpad-12-SNAPSHOT-oak_tar_far.far /app/sling

cd ${SAVE_PWD}
