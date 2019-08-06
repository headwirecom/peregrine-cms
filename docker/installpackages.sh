#!/bin/bash
#
# Installs all Peregrine packages using slingpacker

BIN_DIR=binaries

find $BIN_DIR -type f -name \*.zip | while read pkg 
do 
  PKG_FILE=$(basename "$pkg") 
  echo "Installing package: $PKG_FILE..."
  npx @peregrinecms/slingpackager upload -i binaries/$PKG_FILE
done
