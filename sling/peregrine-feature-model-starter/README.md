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
You need to clone and checkout either 1.0.2 or master branch:
 
1. sling-org-apache-sling-feature-cpconverter branch output-file-name-prefix
1. sling-org-apache-sling-feature-modelconverter tag 1.0.4
1. sling-org-apache-sling-starter
1. sling-slingfeature-maven-plugin branch merge-cpconverter-plugin

Built the first two modules. Afterwards then copy the bin/setenv.proto.sh
to bin/setenv.sh and update the environment variables.

### Build and Launch

This module can be built in two steps:
```
mvn clean install -Dsling.launchpad.folder=<path to your **Sling Starter** base directory>
```
After a successful conversion it can be started with:
```
sh bin/run.sling.conversion.sh
```

Now you can connect to the Peregrine via http://localhost:8080
