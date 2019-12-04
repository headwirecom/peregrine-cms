# Feature Model based Sling Starter

This page contains instructions on how to build and run the Peregrine CMS
based on a Feature Model.

## Introduction to Feature Model

1. Generic Overview with Links: https://github.com/apache/sling-org-apache-sling-feature/blob/master/readme.md
1. Feature Model Definition: https://github.com/apache/sling-org-apache-sling-feature-io/blob/master/design/feature-model.json

## Run Peregrine

### Prepare

Most of this module is downloading dependencies for the Feature Model
from the Sling Maven repository with the exception of the Feature Model
and the Content Package Converter and the Sling Starter to provide the
source Provisioning Model for Sling.
You need to clone and checkout these Sling projects and built them locally:
 
1. sling-slingfeature-maven-plugin branch master
1. sling-feature-converter-maven-plugin branch master

### Build and Launch

This module can be built and launched in one step:
```
mvn clean install -P launch
```
**Note**: To only build the project then just omit the **launch** profile.

Now you can connect to the Peregrine via http://localhost:8080, log in and
then you are redirected to the Peregrine CMS Welcome Page.

**Attention**: As of 12/4/2019 all converted ui.apps from peregrine are not
installed (see http://localhost:8080/system/console/status-osgi-installer)
and they are marked as **Untransformed Resources**.

**Attention**: in case of issues please try to delete the /launcher
and the /target folder manually.
