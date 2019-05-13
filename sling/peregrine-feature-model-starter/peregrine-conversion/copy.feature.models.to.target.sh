#!/bin/bash

# TODO:
# - Let the user pass int the fmConfigOut

baseDir=`pwd`
fmConfigOut=$baseDir/fm.out/peregrine-cms
targetDir=$baseDir/../src/main/features/sling

clear
if [ ! -d $fmConfigOut ]; then
    echo "FM Config Folder '$fmConfigOut' does not exist -> exit"
    exit
fi
if [ ! -d $targetDir ]; then
    echo "Target Folder '$targetDir' does not exist -> exit"
    exit
fi

for file in $fmConfigOut/*.json; do
    filename=`basename $file`
    cp $file $targetDir/peregrine_$filename
done
