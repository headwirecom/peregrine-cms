#!/bin/bash

# TODO:
# - Let the user pass int the fmConfigOut

baseDir=`pwd`
folderName=`basename $baseDir`
if [ "$folderName" == "bin" ]; then
    baseDir=$baseDir/..
fi
peregrineConversionDir=$baseDir/peregrine-conversion
fmConfigOut=$peregrineConversionDir/fm.out/peregrine-cms
addOnFm=$baseDir/src/main/resources/features/sling
targetDir=$baseDir/target/fm

if [ ! -d $fmConfigOut ]; then
    echo "FM Config Folder '$fmConfigOut' does not exist -> exit"
    exit
fi
if [ -d $targetDir ]; then
    echo "Target Folder '$targetDir' does not exist -> create"
    mkdir $targetDir
fi

for file in $fmConfigOut/*.json; do
    filename=`basename $file`
    cp $file $targetDir/peregrine_$filename
done

cp $addOnFm/*.json $targetDir