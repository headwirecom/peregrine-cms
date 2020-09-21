#!/bin/bash

echo "Building peregrine..."

SAVE_PWD=`pwd`

echo "Starting Sling for the first time..."
/app/scripts/start.sh

cd peregrine-cms
mvn clean install -PautoInstallPackage
cd ../themeclean-flex
mvn clean install -PautoInstallPackage

cd ${SAVE_PWD}
