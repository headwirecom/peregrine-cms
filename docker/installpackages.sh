#!/bin/bash

usage () {
  echo "Usage: `basename $0` <peregrine-cms branch> <themeclean-flex branch>"
}

if [ $# -ne 2 ]; then
  usage
  exit 1
fi

# Install peregrine-cms
git clone https://github.com/headwirecom/peregrine-cms.git
cd peregrine-cms
git checkout $1
mvn clean install -P autoInstallPackage
cd ..
rm -rf peregrine-cms

# Install themeclean-flex
git clone https://github.com/headwirecom/themeclean-flex.git
cd themeclean-flex
git checkout $2
mvn clean install -P autoInstallPackage
