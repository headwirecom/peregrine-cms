#!/bin/bash

mvn clean install
cd  sling/peregrine-builder-sling-12 && mvn clean install --quiet
cd ../..
git clone https://github.com/headwirecom/themeclean-flex
cd themeclean-flex
git checkout develop-sling12
mvn clean install
cd ..
find . -type f -name \*.zip

echo "TODO: deploy to docker container.."
rm -v -rf themeclean-flex
