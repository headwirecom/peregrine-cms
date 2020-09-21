#!/bin/bash

echo "Building peregrine..."

SAVE_PWD=`pwd`

/app/scripts/start.sh
cd peregrine-cms
mvn clean install -PautoInstallPackage --quiet

cd ${SAVE_PWD}
