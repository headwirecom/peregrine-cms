#!/bin/bash
# exit if any command fails
set -e

PACKAGE_DIR=/app/binaries

SAVE_PWD=`pwd`

echo "Starting Sling for the first time..."
/app/scripts/start.sh $1

echo "Installing Sling Packager"
npm install @peregrinecms/slingpackager -g

PKG_ORDER=( \
  base.ui.apps-1.0-SNAPSHOT.zip \
  felib.ui.apps-1.0-SNAPSHOT.zip \
  pagerender-vue.ui.apps-1.0-SNAPSHOT.zip \
  pagerender-server.ui.apps-1.0-SNAPSHOT.zip \
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
  sleep 1
done
sleep 4

# Wait for Sling to be fully ready again
# added an extra sleep to make sure the last package install is completed (sling jobs may take a bit to run)
while [ "$(curl -u admin:admin -s --fail  http://localhost:8080/system/console/bundles.json | jq '.s[3:5]' -c)" != "[0,0]" ]
do
  echo "Sling still starting. Waiting for all bundles to be ready.."
  sleep 5
done

#echo "Stopping Peregrine..."
kill `ps -ef | grep org.apache.sling.feature.launcher.jar | grep -v grep | awk '{print $2}'`

#echo "Starting Sling for the second time..."
#/app/scripts/start.sh

cd ${SAVE_PWD}
