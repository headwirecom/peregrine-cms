#!/bin/bash
#
# Installs all Peregrine packages using slingpacker

BIN_DIR=binaries

PKG_ORDER=( \
 base.ui.apps-1.0-SNAPSHOT.zip \
 external-1.0-SNAPSHOT.zip \
 felib.ui.apps-1.0-SNAPSHOT.zip \
 replication.ui.apps-1.0-SNAPSHOT.zip \
 pagerender-vue.ui.apps-1.0-SNAPSHOT.zip \
 admin.sling.ui.apps-1.0-SNAPSHOT.zip \
 admin.ui.materialize-1.0-SNAPSHOT.zip \
 admin.ui.apps-1.0-SNAPSHOT.zip \
 example-vue.ui.apps-1.0-SNAPSHOT.zip \
 themeclean-ui.apps-1.0-SNAPSHOT.zip \
)

# Install packages in a specific order first. 
for pkg in "${PKG_ORDER[@]}"
do
  echo "Installing package '${pkg}' in defined order..."
  npx @peregrinecms/slingpackager upload -i $BIN_DIR/$pkg
done


# Then try to install packages that may be new and not listed above.
find $BIN_DIR -type f -name \*.zip | while read pkg 
do 
  PKG_FILE=$(basename "$pkg") 
  echo ${PKG_ORDER[*]} | grep $PKG_FILE > /dev/null
  if [ $? -ne 0 ]; then 
#    echo "Installing package '${PKG_FILE}' in undefined order..."
#    npx @peregrinecms/slingpackager upload -i $BIN_DIR/$PKG_FILE
    echo "Found a new package '${PKG_FILE}' that needs to be added to the package installation list. This will not be deployed..."
  fi  
done


# Install themeclean-flex
# We need to build Peregrine because themeclean-flex needs com.peregrine-cms:base.core:jar:1.0-SNAPSHOT.
git clone https://github.com/headwirecom/peregrine-cms.git
cd peregrine-cms
git checkout issues/93
mvn clean install
cd ..
rm -rf peregrine-cms

# Now build themeclean-flex
git clone https://github.com/headwirecom/themeclean-flex.git
cd themeclean-flex
git checkout feature/contentrestructureblog
mvn clean package

npx @peregrinecms/slingpackager upload -i ./ui.apps/target/themecleanflex.ui.apps-1.0-SNAPSHOT.zip
cd ..
rm -rf themeclean-flex
