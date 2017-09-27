#!/bin/bash

##
## Created Build Folder and Build Feature JSON
##

ROOT_PATH=$(dirname $0)
echo "Root: $ROOT_PATH"

REPO=~/.m2/repository
SLING_REPO="$REPO/org/apache/sling"
FAB="org.apache.sling.feature.applicationbuilder"
VERSION="0.0.1-SNAPSHOT"
CONFIG=$ROOT_PATH/src/main/config
BUILD=$ROOT_PATH/build
PEREGRINE=$CONFIG/peregrine
SLING=$CONFIG/sling

if [ ! -e $CONFIG ]; then
    echo "Config Folder: $CONFIG does not exist -> Exit"
    exit 1
fi

if [ ! -d $CONFIG ]; then
    echo "Config Folder: $CONFIG does not a Folder -> Exit"
    exit 2
fi

echo "Build the Peregrine Sling Feature"

if [ -e $BUILD ]; then
    rm -f $BUILD/peregrine.json
fi

mkdir -p $BUILD

java -jar "$SLING_REPO/$FAB/$VERSION/$FAB-$VERSION.jar" \
    -d $SLING \
    -f $PEREGRINE/pkginstaller.json,$PEREGRINE/peregrine.json \
    -u $REPO \
    -o $BUILD/peregrine.json

FAN="org.apache.sling.feature.analyser"

echo "Analyze the Peregrine Project"

java -jar $SLING_REPO/$FAN/$VERSION/$FAN-$VERSION.jar \
    $BUILD/peregrine.json

FL="org.apache.sling.feature.launcher"

echo "Lauch Sling / Peregrine. Hit ctrl-c to end it"

java -jar $SLING_REPO/$FL/$VERSION/$FL-$VERSION.jar \
    -I \
    -v \
   -a $BUILD/peregrine.json

