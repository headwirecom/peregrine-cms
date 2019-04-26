#!/bin/bash

find ../ | grep "target.*zip$" | grep -v "docker" | xargs --max-args=1 -i cp '{}' files
cp ../resources/com.peregrine-cms.sling.launchpad-9.1.jar files/sling-9.jar

# TODO: should check if the file already has been downloaded or not to avoid redownloading
wget https://nodejs.org/dist/v10.15.3/node-v10.15.3-linux-x64.tar.xz -O files/node-v10.15.3-linux-x64.tar.xz