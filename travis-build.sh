#!/bin/bash

echo "Building Peregrine CMS..."
mvn clean install
cd  sling/peregrine-builder-sling-12 && mvn clean install --quiet

echo "Building 'themeclean-flex'..."
cd ../..
git clone https://github.com/headwirecom/themeclean-flex
cd themeclean-flex
git checkout develop-sling12
mvn clean install
cd ..

echo "Copying packages to Docker working directory..."
find . -type f -name \*.zip -exec cp {} ./docker/files/ \;

echo "Removing temporary 'themeclean-flex' project..."
rm -rf themeclean-flex
