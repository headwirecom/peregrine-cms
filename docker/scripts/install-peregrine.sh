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

slingpackager list | awk '{print $1}' | cut -d"=" -f2 | while read package;
do
  echo "Installing package: ${package}..."
#  slingpackager -v install ${package}
done

cd ${SAVE_PWD}
