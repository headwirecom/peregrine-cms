#!/bin/bash

# TODO:
# - Let the user pass int the fmConfigOut

baseDir=`pwd`
fmConfigOut=$baseDir/fm.out/peregrine-cms
targetDir=$HOME/.m2/repository

clear
if [ ! -d $fmConfigOut ]; then
    echo "FM Config Folder '$fmConfigOut' does not exist -> exit"
    exit
fi
if [ ! -d $targetDir ]; then
    echo "Target Folder '$targetDir' does not exist -> exit"
    exit
fi

# Find all converted files
# Create target folder in .m2 (use a test first) if not already there
# Copy zip file over
outLength=${#fmConfigOut}
find $fmConfigOut -name "*-cp2fm-converted.zip"|while read fname; do
  # Get Relative path of the file regarding the output folder
  relativePath=${fname:$outLength}
  # Remove the leading /
  relativePath=${relativePath:1}
  # Get the Folder and File Name
  directory=`dirname $relativePath`
  file=`basename $relativePath`
  # Build Target Folder and File
  targetDirectory=$targetDir/$directory
  targetFile=$targetDirectory/$file
   if [ ! -d $targetDirectory ]; then
       echo
       echo "Create Directory: $targetDirectory"
       echo
       mkdir -p $targetDirectory
   fi
   echo
   echo "Copy file: $file to folder: $targetDirectory"
   echo
   cp $fname $targetFile
done
