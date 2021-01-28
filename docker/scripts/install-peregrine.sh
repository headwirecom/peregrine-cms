#!/bin/bash
# exit if any command fails
set -e

PACKAGE_DIR=/app/binaries

SAVE_PWD=`pwd`

echo "Starting Sling for the first time..."
/app/scripts/start.sh

echo "Installing Sling Packager"
npm install @peregrinecms/slingpackager -g

ls ${PACKAGE_DIR}/*.zip | while read package;
do
  echo "Uploading package: ${package}..."
  slingpackager -v upload ${package}
done

slingpackager list

PKG_ORDER=( \
  /com.peregrine-cms/base.ui.apps-1.0-SNAPSHOT.zip \
  /com.peregrine-cms/external-1.0-SNAPSHOT.zip \
  /com.peregrine-cms/felib.ui.apps-1.0-SNAPSHOT.zip \
  /com.peregrine-cms/replication.ui.apps-1.0-SNAPSHOT.zip \
  /com.peregrine-cms/pagerender-vue.ui.apps-1.0-SNAPSHOT.zip \
  /com.peregrine-cms/admin.ui.apps-1.0-SNAPSHOT.zip \
  /com.peregrine-cms/admin.ui.materialize-1.0-SNAPSHOT.zip \
  /com.peregrine-cms/admin.sling.ui.apps-1.0-SNAPSHOT.zip \
  /com.peregrine-cms.example/example-vue.ui.apps-1.0-SNAPSHOT.zip \
  /themeclean/themeclean-ui.apps-1.0-SNAPSHOT.zip \
  /themeclean-flex/themecleanflex.ui.apps-1.0-SNAPSHOT.zip \
)


for pkg in "${PKG_ORDER[@]}"
do
  echo "Installing package '${pkg}' in defined order..."
  slingpackager install $pkg

  # Install login bundle after felib
  if [ $pkg == '/com.peregrine-cms/felib.ui.apps-1.0-SNAPSHOT.zip' ]; then
    echo "Installing: ${PACKAGE_DIR}/login-1.0-SNAPSHOT.jar"
    curl -u admin:admin -F action=install -F bundlestartlevel=20 -F \
        bundlefile=@"${PACKAGE_DIR}/login-1.0-SNAPSHOT.jar" http://localhost:8080/system/console/bundles
  fi
done

#echo "Stopping Peregrine..."
#kill `ps -ef | grep org.apache.sling.feature.launcher.jar | grep -v grep | awk '{print $2}'`

#echo "Starting Sling for the second time..."
#/app/scripts/start.sh

cd ${SAVE_PWD}
