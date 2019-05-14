# Feature Model based Sling Starter

This page contains instructions on how to build and run the Peregrine CMS
based on a Feature Model.

## Introduction to Feature Model

1. Generic Overview with Links: https://github.com/apache/sling-org-apache-sling-feature/blob/master/readme.md
1. Feature Model Definition: https://github.com/apache/sling-org-apache-sling-feature-io/blob/master/design/feature-model.json

## Run Peregrine

### Prepare

First we need to checkout all the necessary Sling Feature project into
a single folder and the absolute path to the fodler must be placed into
an env variable called SLING_DEV:
1. sling-org-apache-sling-feature
1. sling-org-apache-sling-feature-analyser
1. sling-org-apache-sling-feature-apiregions
1. sling-org-apache-sling-feature-cpconverter
1. sling-org-apache-sling-feature-extension-content
1. sling-org-apache-sling-feature-io
1. sling-org-apache-sling-feature-launcher
1. sling-org-apache-sling-feature-modelconverter
1. sling-slingfeature-maven-plugin

Afterwards make sure that you create a branch of all that have a release
tag of 1.0.2 and make sure they are HEAD. All others should be master.

Now we build all of them (make sure SLING_DEV is set):
1. Open a terminal and go to the project folder
1. Run script **build.and.install.sling.fm.tools.sh**

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
java -cp org.apache.sling.feature.extension.content-1.0.2.jar:org.apache.sling.feature.extension.apiregions-1.0.2.jar:org.apache.sling.feature.launcher-1.0.2.jar \
     org.apache.sling.feature.launcher.impl.Main \
     -f target/slingfeature-tmp/feature-example-runtime.json \
     -c "./target/peregrine/cache" \
     -p "./target/peregrine" \
     -v
```

**ATTENTION**: because Peregrine is installed into the **target** folder this
folder is deleted during `mvn clean`. Adjust both the **-c** and **-p** accordingly.

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

See Preparation of Run above.

#### Package Conversion

1. Make sure that you are in the peregrine-conversion folder on CLI
1. Make sure all of peregrine was built ahead
1. Backup **fm.out** folder and then delete it if it is already there 
1. Run the conversion with `./convert.peregrine.2.fm.sh` (use clean as parameter
if you want to delete the fm.out folder)

This conversion will create a new folder **fm.out/peregrine-cms** which
contains the Feature Model files (*.json) as well as the converted package
ZIP files in sub folder **com** and **org**.

#### Install Converted Packages into Local Maven Repo

The feature launcher will takes package dependency from your local
Maven Repository and so we need to copy the converted files to there:
1. Make sure that you are in the peregrine-conversion folder on CLI
1. Run the copy script: `./copy.converted.zip.file.to.m2.sh`

#### Manual Adjustments

There are no more manual adjustments necessary.

#### Installation

The last step is to copy all the feature model files into the target
folder for the starter project using the script **copy.feature.models.to.target.sh**.
This script will not only copy them over but also prepend the file
name with **peregrine_** to distinguish them from the sling files.

**ATTENTION**: there are two superfluous files generated and they do
not bother the launcher / Peregrine but are not added to Git repo:
1. peregrine_admin.ui.apps-example.json
1. peregrine_base.ui.apps-it.json
