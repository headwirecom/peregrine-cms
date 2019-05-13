#!/bin/bash

#
# Script goes to the given folder, converts the package to Feature model
#

baseDir=`pwd`
peregrineHome=$baseDir/../../..
fmConfigOut=$baseDir/fm.out

function doConversion {
    name=$1
    shift
    packages=$@

    for var in "$@"
    do
        if [ ! -f $var ]; then
            echo "Package '$var' does not exist -> exit"
            exit
        fi
    done

    fmOut=$fmConfigOut/$name
    if [ -d $fmOut ]; then
        echo "FM Configuration Out '$fmOut' does already exist -> exit"
        exit
    fi
    mkdir $fmOut

    # Call the CP 2 FM Converter
    # -X: verbose
    # -m: merge configs with same ID
    ./bin/cp2sf \
        -X \
        -m \
        -a $fmOut \
        -b 20 \
        -i "com.peregrine-cms:com.peregrine-cms.featuremodel.starter:slingosgifeature:1.0-SNAPSHOT" \
        -o $fmOut \
        $packages
}

# Create Output Folders if not there yet
if [ ! -d $fmConfigOut ]; then
    mkdir $fmConfigOut
fi

# Check if I have to clean first
if [ "$1" == "clean" ]; then
    echo "Clean FM Config Out"
    rm -rf $fmConfigOut/*
fi

# Convert Peregrine CMS
doConversion peregrine-cms \
  $peregrineHome/platform/base/ui.apps/target/base.ui.apps-1.0-SNAPSHOT.zip \
  $peregrineHome/platform/felib/ui.apps/target/felib.ui.apps-1.0-SNAPSHOT.zip \
  $peregrineHome/platform/node-js/ui.apps/target/node-js.ui.apps-1.0-SNAPSHOT.zip \
  $peregrineHome/platform/node-js/ui.script.apps/target/node-js.ui.apps.script-1.0-SNAPSHOT.zip \
  $peregrineHome/admin-base/materialize/target/admin.ui.materialize-1.0-SNAPSHOT.zip \
  $peregrineHome/admin-base/sling.ui.apps/target/admin.sling.ui.apps-1.0-SNAPSHOT.zip \
  $peregrineHome/admin-base/ui.apps/target/admin.ui.apps-1.0-SNAPSHOT.zip \
  $peregrineHome/pagerenderer/vue/ui.apps/target/pagerender-vue.ui.apps-1.0-SNAPSHOT.zip \
  $peregrineHome/themes/themeclean/ui.apps/target/themeclean-ui.apps-1.0-SNAPSHOT.zip \
  $peregrineHome/samples/example-vue-site/ui.apps/target/example-vue.ui.apps-1.0-SNAPSHOT.zip

echo
echo "Conversion Done"
echo
