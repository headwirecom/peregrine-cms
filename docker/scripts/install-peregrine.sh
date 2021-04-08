#!/bin/bash
# exit if any command fails
set -e

PACKAGE_DIR=/app/binaries

SAVE_PWD=`pwd`

echo "Starting Sling for the first time..."
/app/scripts/start.sh

echo "Installing Sling Packager"
npm install @peregrinecms/slingpackager -g

PKG_ORDER=( \
  base.ui.apps-1.0-SNAPSHOT.zip \
  felib.ui.apps-1.0-SNAPSHOT.zip \
  pagerender-vue.ui.apps-1.0-SNAPSHOT.zip \
  admin.ui.apps-1.0-SNAPSHOT.zip \
  admin.ui.materialize-1.0-SNAPSHOT.zip \
  admin.sling.ui.apps-1.0-SNAPSHOT.zip \
  example-vue.ui.apps-1.0-SNAPSHOT.zip \
  themeclean-ui.apps-1.0-SNAPSHOT.zip \
  themecleanflex.ui.apps-1.0-SNAPSHOT.zip \
)

for pkg in "${PKG_ORDER[@]}"
do
  echo "Installing package '${pkg}' in defined order..."
  slingpackager -v upload --install ${PACKAGE_DIR}/$pkg
done

#echo "Stopping Peregrine..."
#kill `ps -ef | grep org.apache.sling.feature.launcher.jar | grep -v grep | awk '{print $2}'`

#echo "Starting Sling for the second time..."
#/app/scripts/start.sh

cd ${SAVE_PWD}
