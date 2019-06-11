#!/bin/bash

#
# Script goes to the given folder, converts the package to Feature model
#

baseDir=`pwd`/..
slingDevHome=$1
conversionDir=$baseDir/peregrine-conversion
conversionBinDir=$conversionDir/bin
conversionLibDir=$conversionDir/lib

function buildMavenProject {

    projectName=$1
    projectFolder=$2
    if [ ! -d $projectFolder ]; then
      echo "Project Folder: '$projectFolder' does not exit -> exit"
      exit
    fi
    if [ ! -f $projectFolder/pom.xml ]; then
      echo "Project Folder: '$projectFolder' is not a Maven Project (no pom.xml) -> exit"
      exit
    fi

    echo
    echo ---------------------------------------------------
    echo     Build $1
    echo ---------------------------------------------------
    echo

    cd $projectFolder
    mvn clean install
    retVal=$?
    if [ $retVal -ne 0 ]; then
        echo "Maven Build failed -> exit"
        exit
    fi
}


if [ "$slingDevHome" ==  "" ]; then
    slingDevHome=$SLING_DEV
fi

if [ "$slingDevHome" ==  "" ]; then
    echo "No Sling Dev (Parameter 1 or SLING_DEV) is provided -> exit"
    exit
fi

# Build All FM Maven projects
buildMavenProject "Sling Feature" $slingDevHome/sling-org-apache-sling-feature
buildMavenProject "Sling Feature Analyzer" $slingDevHome/sling-org-apache-sling-feature-analyser
buildMavenProject "Sling Feature API Regions" $slingDevHome/sling-org-apache-sling-feature-apiregions
buildMavenProject "Sling Feature CP 2 FM Converter" $slingDevHome/sling-org-apache-sling-feature-cpconverter
buildMavenProject "Sling Feature Content Extension" $slingDevHome/sling-org-apache-sling-feature-extension-content
buildMavenProject "Sling Feature IO" $slingDevHome/sling-org-apache-sling-feature-io
buildMavenProject "Sling Feature Launcher" $slingDevHome/sling-org-apache-sling-feature-launcher
buildMavenProject "Sling Feature Model Converter" $slingDevHome/sling-org-apache-sling-feature-modelconverter
buildMavenProject "Sling Feature Maven Plugin" $slingDevHome/sling-slingfeature-maven-plugin

# Install the CP 2 FM Converter
if [ -d $conversionBinDir ]; then
    rm -rf $conversionBinDir
fi
if [ -d $conversionLibDir ]; then
    rm -rf $conversionLibDir
fi

unzip $slingDevHome/sling-org-apache-sling-feature-cpconverter/target/org.apache.sling.feature.cpconverter-*.zip -d $conversionDir
cp -R $conversionDir/org.apache.sling.feature.cpconverter*/bin $conversionDir
cp -R $conversionDir/org.apache.sling.feature.cpconverter*/lib $conversionDir
rm -rf $conversionDir/org.apache.sling.feature.cpconverter*

# Copy the Feature Launcher over
cp $slingDevHome/sling-org-apache-sling-feature-launcher/target/*.jar $baseDir
cp $slingDevHome/sling-org-apache-sling-feature-extension-content/target/*.jar $baseDir
cp $slingDevHome/sling-org-apache-sling-feature-apiregions/target/*.jar $baseDir
