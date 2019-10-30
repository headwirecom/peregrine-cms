#!/bin/bash

# Copy all packages to 'files' directory
find ../ -type f -name \*.zip | grep "/target/" | while read package; do cp $package files; done;
cp ../resources/com.peregrine-cms.sling.launchpad-11.jar files/sling-11.jar

NODE_VERSION=v10.16.0
NODE_TARBALL=node-${NODE_VERSION}-linux-x64.tar.xz
NODE_URL=https://nodejs.org/dist/${NODE_VERSION}/${NODE_TARBALL}

if [ ! -e files/${NODE_TARBALL} ]; then
  curl -L ${NODE_URL} -o files/${NODE_TARBALL}
fi 
