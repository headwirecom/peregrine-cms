#!/bin/bash
# exit if any command fails
set -e

echo "Building peregrine..."

SAVE_PWD=`pwd`

echo "Starting Sling for the first time..."
/app/scripts/start.sh

cd peregrine-cms
mvn clean install -PautoInstallPackage
cd ../themeclean-flex
mvn clean install -PautoInstallPackage
cd ../peregrine-cms/slingjunit.parent 
mvn clean install -P slingJunitIT
cd ${SAVE_PWD}
