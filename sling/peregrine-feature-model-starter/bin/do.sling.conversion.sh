#!/bin/bash

# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements. See the NOTICE file distributed with this
# work for additional information regarding copyright ownership. The ASF
# licenses this file to You under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations under
# the License.

#
# This script converts the local Sling Launchpad Starter Provisioning Model into a Feature
# Model and starts it with the Feature Launcher. See file pm2fm.sling.arfile to PM 2 FM
# configuration.
#

baseDir=`pwd`
folderName=`basename $baseDir`
if [ "$folderName" == "bin" ]; then
    baseDir=$baseDir/..
fi

# Set Env Variables
. $baseDir/bin/setenv.sh

conversionDir=$baseDir/sling-conversion
conversionBinDir=$conversionDir/bin
conversionLibDir=$conversionDir/lib
export conversionLibOut=$conversionDir/fm.out
fmVersion=1.0.2
laucherName=org.apache.sling.feature.launcher
contentExtensionName=org.apache.sling.feature.extension.content
apiRegionsExtensionName=org.apache.sling.feature.extension.apiregions
# ATTENTION: content extension needs to be placed ahead of the launcher to make
#            sure the Extension's Content Handler is picked up ahead of the dummy
#            implementation of the Launcher
launcherClassPath=$baseDir/$contentExtensionName-$fmVersion.jar:$baseDir/$apiRegionsExtensionName-$fmVersion.jar:$baseDir/$laucherName-$fmVersion.jar

if [ "$1" == "clean" ]; then
    echo "Clean scratch folders"
    rm -rf $conversionDir
    rm -rf target/
    rm -rf laucher/
    shift
fi

if [ "x$SLING_DEV_HOME" ==  "x" ]; then
    echo "No Sling Dev Home is provided -> exit"
    exit
fi

if [ ! -d $conversionDir ]; then
    mkdir $conversionDir
fi

# Install the PM 2 FM Converter
if [ -d $conversionBinDir ]; then
    rm -rf $conversionBinDir
fi
if [ -d $conversionLibDir ]; then
    rm -rf $conversionLibDir
fi

unzip $SFMC_HOME/target/org.apache.sling.feature.modelconverter-*.zip -d $conversionDir
cp -R $conversionDir/org.apache.sling.feature.modelconverter*/bin $conversionDir
cp -R $conversionDir/org.apache.sling.feature.modelconverter*/lib $conversionDir
rm -rf $conversionDir/org.apache.sling.feature.modelconverter*

# Obtain Feature Artifacts

curl http://repo1.maven.org/maven2/org/apache/sling/$laucherName/$fmVersion/$laucherName-$fmVersion.jar -O
curl http://repo1.maven.org/maven2/org/apache/sling/$contentExtensionName/$fmVersion/$contentExtensionName-$fmVersion.jar -O
curl http://repo1.maven.org/maven2/org/apache/sling/$apiRegionsExtensionName/$fmVersion/$apiRegionsExtensionName-$fmVersion.jar -O

# Do the PM 2 FM Conversion

# cd $conversionDir
if [ -d $conversionLibOut ]; then
    rm -rf conversionLibOut/*
fi

echo "conversionBinDir $conversionBinDir"

# Both the Input and Output Folder are set here and so cannot be part of the
# Argument File (arfile) and so there are excluded and set here
sh $conversionBinDir/pm2fm \
   @$baseDir/bin/pm2fm.sling.arfile \
   -i $SLING_STARTER_HOME/src/main/provisioning \
   -o $conversionLibOut


# Copy generated FM files to target
if [ ! -d $baseDir/target ]; then
   mkdir $baseDir/target
fi
if [ ! -d $baseDir/target/fm ]; then
   mkdir $baseDir/target/fm
else
   rm -rf $baseDir/target/fm/*
fi

cp -R $conversionDir/fm.out/* $baseDir/target/fm

# Convert Peregrine

. $baseDir/bin/convert.peregrine.2.fm.sh clean
. $baseDir/bin/copy.converted.zip.file.to.m2.sh
. $baseDir/bin/copy.feature.models.to.target.sh

# Build Project

mvn install

# Launch Sling

java -cp $launcherClassPath \
      org.apache.sling.feature.launcher.impl.Main \
      -f target/slingfeature-tmp/feature-example-runtime.json
