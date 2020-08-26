[![Apache Sling](https://sling.apache.org/res/logos/sling.png)](https://sling.apache.org)

&#32;[![Build Status](https://ci-builds.apache.org/job/Sling/job/modules/job/sling-org-apache-sling-starter/job/master/badge/icon)](https://ci-builds.apache.org/job/Sling/job/modules/job/sling-org-apache-sling-starter/job/master/)&#32;[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=apache_sling-org-apache-sling-starter&metric=alert_status)](https://sonarcloud.io/dashboard?id=apache_sling-org-apache-sling-starter)&#32;[![JavaDoc](https://www.javadoc.io/badge/org.apache.sling/org.apache.sling.starter.svg)](https://www.javadoc.io/doc/org.apache.sling/org-apache-sling-starter)&#32;[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apache.sling/org.apache.sling.starter/badge.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.apache.sling%22%20a%3A%22org.apache.sling.starter%22) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

# Apache Sling Starter

This module is part of the [Apache Sling](https://sling.apache.org) project.

The starter project produces feature artifacts that can be launched using the
[Feature Launcher](https://github.com/apache/sling-org-apache-sling-feature-launcher).

It is **not meant to be a production-ready setup**, more as a way to facilitate experimenting and learning Sling. 

See [Releasing a new version of the Sling starter](https://cwiki.apache.org/confluence/display/SLING/Releasing+a+new+version+of+the+Sling+Starter) for how to create a release of this module.

## How to run the Sling Starter module in Standalone mode


  NOTE: "mvn clean" deletes the "launcher" work directory in the project base
        directory. It is advisable to use a work directory outside of the
        project directory.

1) Build the Sling Starter using `mvn clean install` in the current directory.
2) Copy the launch to project directory `cp target/dependency/org.apache.sling.feature.launcher.jar $path-to-project`
3) Copy either the feature archive (*.far) or individual set of feature.json files to the project folder 


Example 1: Start an instance using the launcher specifying a feature archive (.far)
This example uses the aggregate `oak_tar_far` as defined in the pom.xml

        java -jar org.apache.sling.feature.launcher.jar -f org.apache.sling.starter-12-SNAPSHOT-oak_tar_far.far 
	 
Example 2: Start an instance using the launcher specifying an aggregate feature file. 
This example uses the aggregate `feature-oak_tar_fds.json` as defined in the pom.xml

        java -jar org.apache.sling.feature.launcher.jar -f feature-oak_tar_fds.json

Example 3: Start an instance using the launcher specifying a set feature files.
 
        java -jar org.apache.sling.feature.launcher.jar -f feature-base.json,feature-boot.json,....

Browse Sling in:

        http://localhost:8080

For MongoDB support replace the launch command with

    java -jar target/dependency/org.apache.sling.feature.launcher.jar -f target/slingfeature-tmp/feature-oak_mongo.json

This expects a MongoDB server to be running, search for `mongodb://` in the feature files for the expected URL
(currently `mongodb://localhost:27017`).

## Extending the Sling Starter

If you wish to extend the Sling Starter but would like to keep various application-level features out, you can
start with the `nosample_base` aggregate, which contains:

- all the base features
- Oak base features, without the NodeStore setup
- No applications ( Composum, Slingshot, etc )

For instance, launching an empty Sling Starter with segment persistence can be achieved by running

    java -jar target/dependency/org.apache.sling.feature.launcher.jar -f target/slingfeature-tmp/feature-nosample_base.json,target/slingfeature-tmp/feature-oak_persistence_sns.json
    
Your own feature files can be added to the feature list.
