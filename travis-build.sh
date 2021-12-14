#!/bin/bash

echo "Building Peregrine CMS..."
mvn clean install

#disabled building the sling builder and instead pull sling builder and far from separate project
#echo "Building Peregrine CMS Feature Launcer..."
#cd sling/peregrine-builder-sling-12 && mvn clean install --quiet
#cd ../..

echo "Fetching Peregrine CMS Feature Launcher..."
curl -L -o ./docker/files/com.peregrine-cms.sling.launchpad-12-SNAPSHOT-oak_tar_far.far https://github.com/peregrine-cms/peregrine-builder/releases/download/0.0.9/com.peregrine-cms.sling.launchpad-12-SNAPSHOT-oak_tar_far.far
curl -L -o ./docker/files/org.apache.sling.feature.launcher.jar  https://github.com/peregrine-cms/peregrine-builder/releases/download/0.0.9/org.apache.sling.feature.launcher.jar

echo "Building 'themeclean-flex'..."
git clone https://github.com/headwirecom/themeclean-flex
cd themeclean-flex
git checkout develop-sling12
mvn clean install
cd ..

echo "Copying packages to Docker working directory..."
rm -rf ./docker/files/*.zip
find . -type f -name \*.zip -exec cp {} ./docker/files/ \;
rm ./docker/files/npm-*.zip

#echo "Copy select bundles..."
#cp platform/login/target/login-1.0-SNAPSHOT.jar ./docker/files/

#echo "Copying Feature Models artifacts to Docker working directory..."
#cp sling/peregrine-builder-sling-12/target/dependency/org.apache.sling.feature.launcher.jar ./docker/files/
#cp sling/peregrine-builder-sling-12/target/slingfeature-tmp/feature-oak_tar.json ./docker/files/
#cp sling/peregrine-builder-sling-12/target/com.peregrine-cms.sling.launchpad-12-SNAPSHOT-oak_tar_far.far ./docker/files/

echo "Removing temporary 'themeclean-flex' project..."
rm -rf themeclean-flex
