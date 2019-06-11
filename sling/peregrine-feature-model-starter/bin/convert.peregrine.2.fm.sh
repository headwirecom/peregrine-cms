#!/bin/bash

#
# Script goes to the given folder, converts the package to Feature model
#

baseDir=`pwd`
folderName=`basename $baseDir`
if [ "$folderName" == "bin" ]; then
    baseDir=$baseDir/..
fi
echo "Convert Peregrine, base dir: $baseDir"

peregrineHome=$baseDir/../..
peregrineConversionDir=$baseDir/peregrine-conversion
peregrineConversionBinDir=$peregrineConversionDir/bin
peregrineConversionLibDir=$peregrineConversionDir/lib
fmConfigOut=$peregrineConversionDir/fm.out

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
    # -i: overwrites the Model Id. Need to escape the dollar sign with \ and to add the package name
    #     use the {{}} escape
    sh $peregrineConversionBinDir/cp2sf \
        -X \
        -m \
        -a $fmOut \
        -b 20 \
        -i "\${project.groupId}:\${project.artifactId}:slingosgifeature:\${{filename}}:\${project.version}" \
        -o $fmOut \
        $packages
}

# Create Output Folders if not there yet
if [ ! -d $peregrineConversionDir ]; then
    mkdir $peregrineConversionDir
fi

unzip $SFCPC_HOME/target/org.apache.sling.feature.cpconverter-*.zip -d $peregrineConversionDir
cp -R $peregrineConversionDir/org.apache.sling.feature.cpconverter*/bin $peregrineConversionBinDir
cp -R $peregrineConversionDir/org.apache.sling.feature.cpconverter*/lib $peregrineConversionLibDir
rm -rf $peregrineConversionDir/org.apache.sling.feature.cpconverter*


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
