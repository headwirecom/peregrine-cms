#!/bin/bash

# Install peregrine-cms
# We need to build Peregrine because themeclean-flex needs com.peregrine-cms:base.core:jar:1.0-SNAPSHOT.
git clone https://github.com/headwirecom/peregrine-cms.git
cd peregrine-cms
git checkout issues/93
mvn clean install -P autoInstallPackage
cd ..
rm -rf peregrine-cms

# Install themeclean-flex
git clone https://github.com/headwirecom/themeclean-flex.git
cd themeclean-flex
git checkout feature/contentrestructureblog
mvn clean install -P autoInstallPackage
