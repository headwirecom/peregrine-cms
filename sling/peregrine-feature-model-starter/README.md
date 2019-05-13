# Feature Model based Sling Starter

This page contains instructions on how to build and run the Peregrine CMS
based on a Feature Model.

## Introduction to Feature Model

1. Generic Overview with Links: https://github.com/apache/sling-org-apache-sling-feature/blob/master/readme.md
1. Feature Model Definition: https://github.com/apache/sling-org-apache-sling-feature-io/blob/master/design/feature-model.json

## Run Peregrine

### Prepare

Download the launcher:

The latest versions of the launcher can be found here: https://repository.apache.org/content/groups/snapshots/org/apache/sling/org.apache.sling.feature.launcher/1.0.1-SNAPSHOT/
but it is probably best to actually download and build the code with `mvn clean install`.
Here are the necessary Git repos:
1. https://github.com/apache/sling-org-apache-sling-feature-launcher
1. https://github.com/apache/sling-org-apache-sling-feature-extension-content
1. https://github.com/apache/sling-org-apache-sling-feature-extension-apiregions

Afterwards copy the created JAR files into this folder.

Stable Versions can be obtained this way but for now should not be used:
```
curl http://repo1.maven.org/maven2/org/apache/sling/org.apache.sling.feature.launcher/1.0.0/org.apache.sling.feature.extension.content-1.0.0.jar -O
curl http://repo1.maven.org/maven2/org/apache/sling/org.apache.sling.feature.launcher/1.0.0/org.apache.sling.feature.extension.apiregions-1.0.0.jar -O
curl http://repo1.maven.org/maven2/org/apache/sling/org.apache.sling.feature.launcher/1.0.0/org.apache.sling.feature.launcher-1.0.0.jar -O
```

### Build and Launch

Build the feature model project with:

```
mvn clean install
```

Launch Peregrine (adjust the version numbers accordingly):

```
java -cp org.apache.sling.feature.extension.content-1.0.2.jar:org.apache.sling.feature.extension.apiregions-1.0.1-SNAPSHOT.jar:org.apache.sling.feature.launcher-1.0.2.jar \
     org.apache.sling.feature.launcher.impl.Main \
     -f target/slingfeature-tmp/feature-example-runtime.json \
     -c "./target/peregrine/cache" \
     -p "./target/peregrine" \
     -v
```

**Note**: the **-p** option is the place where Peregrine will be installed
and it works the same as the regular **sling.home** does.

Now you can connect to the Peregrine via http://localhost:8080

## Convert Peregrine to Feature Model

This is a copy of the Sling Feature Model Starter project found here:
https://github.com/schaefa/sling-featuremodel-starter

The only adjustments made where in the POM to adjust the group, artifact
and version as well as the parent POM.

### Peregrine Package Conversion

#### Preparation

First the Sling Content Package to Feature Model Converter needs to be
checked out, built and installed:
1. Clone the project here: https://github.com/apache/sling-org-apache-sling-feature-cpconverter
1. Build it with `mvn clean install`
1. Open and extract /libs and /bin from generated ZIP file to ./peregrine-conversion folder
1. Change to the peregrine-conversion folder
1. Check the converter with `./bin/cp2sf -h`

#### Package Conversion

1. Make sure that you are in the peregrine-conversion folder on CLI
1. Make sure all of peregrine is built ahead
1. Backup **fm.out** folder and then delete it if it is already there 
1. Run the conversion with `./convert.peregrine.2.fm.sh

This conversion will create a new folder **fm.out/peregrine-cms** which
contains the Feature Model files (*.json) as well as the converted package
ZIP files in sub folder **com** and **org**.

#### Install Converted Packages into Local Maven Repo

The feature launcher will takes package dependency from your local
Maven Repository and so we need to copy the converted files to there:
1. Make sure that you are in the peregrine-conversion folder on CLI
1. Run the copy script: `./copy.converted.zip.file.to.m2.sh`

#### Manual Adjustments

The feature model files need some manual adjustments before installing
into the feature model starter:
1. Replace the Feature Model Ids with this and replace base.ui.apps (classifier)
with the package name of the feature model file: 
```
  "id":"${project.groupId}:${project.artifactId}:slingosgifeature:base.ui.apps:${project.version}",
```
2. Adjust the Feature Model Configuration files:
```
admin.sling.ui.apps.json: NO CHANGES
admin.ui.apps.json: Add the following:
    {
      "id":"com.peregrine-cms:admin.core:1.0-SNAPSHOT",
      "start-order":"20"
    }
Remove the following:
    {
      "id":"com.peregrine-cms:commons:1.0-SNAPSHOT",
      "start-order":"20"
    }
admin.ui.apps-example.json: Delete the file (example for S3 Connectivity)
admin.ui.materialize.json: NO CHANGES
base.ui.apps: Add the following: NO CHANGES
base.ui.apps-dev.json: NO CHANGES / Is a Runmode Configuration
base.ui.apps-it.json: Delete the file (only needed for IT tests)
example-vue.ui.apps.json: NO CHANGES
felib.ui.apps.json: NO CHANGES
node-js.ui.apps.json: Add the following:
    {
      "id":"com.peregrine-cms:node-js.core:1.0-SNAPSHOT",
      "start-order":"20"
    }
Remove the following:
    {
      "id":"org.json:json:20140107",
      "start-order":"20"
    }
node-js.ui.apps.script.json: NO CHANGES
pagerender-vue.ui.apps.json: NO CHANGES
themeclean-ui.apps.json: NO CHANGES
```

#### Installation

The last step is to copy all the feature model files into the target
folder for the starter project using the script **copy.feature.models.to.target.sh**

