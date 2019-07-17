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

fmVersion=1.0.2
laucherName=org.apache.sling.feature.launcher
contentExtensionName=org.apache.sling.feature.extension.content
apiRegionsExtensionName=org.apache.sling.feature.extension.apiregions
# ATTENTION: content extension needs to be placed ahead of the launcher to make
#            sure the Extension's Content Handler is picked up ahead of the dummy
#            implementation of the Launcher
launcherClassPath=$baseDir/$contentExtensionName-$fmVersion.jar:$baseDir/$apiRegionsExtensionName-$fmVersion.jar:$baseDir/$laucherName-$fmVersion.jar

# Launch Sling

echo "First Parameter: $1"

if [ "x$1" == "xdebug" ]; then
    echo java \
          -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005 \
          -cp $launcherClassPath \
          org.apache.sling.feature.launcher.impl.Main \
          -f target/slingfeature-tmp/feature-example-runtime.json
    java \
          -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005 \
          -cp $launcherClassPath \
          org.apache.sling.feature.launcher.impl.Main \
          -f target/slingfeature-tmp/feature-example-runtime.json
else
    echo java \
          -cp $launcherClassPath \
          org.apache.sling.feature.launcher.impl.Main \
          -f target/slingfeature-tmp/feature-example-runtime.json
    java \
          -cp $launcherClassPath \
          org.apache.sling.feature.launcher.impl.Main \
          -f target/slingfeature-tmp/feature-example-runtime.json
fi