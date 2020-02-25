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
THEMECLEANFLEX_PKG=themecleanflex.ui.apps-1.0-SNAPSHOT.zip
curl -L -o ${THEMECLEANFLEX_PKG} \
  https://vagrant.headwire.com/peregrine/${THEMECLEANFLEX_PKG}
 npx @peregrinecms/slingpackager upload -i ${THEMECLEANFLEX_PKG}
