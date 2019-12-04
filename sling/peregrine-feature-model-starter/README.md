# Feature Model based Sling Starter

This page contains instructions on how to build and run the Peregrine CMS
based on a Feature Model.

## Introduction to Feature Model

1. Generic Overview with Links: https://github.com/apache/sling-org-apache-sling-feature/blob/master/readme.md
1. Feature Model Definition: https://github.com/apache/sling-org-apache-sling-feature-io/blob/master/design/feature-model.json

## Run Peregrine

### Prepare

As of 12/4/2019 the Sling Feature Maven Plugins are provided as part of
the project local repository and so there is no need to clone and built
them locally.

### Build and Launch

This module can be built and launched in one step:
```
mvn clean install -P launch
```
**Note**: To only build the project then just omit the **launch** profile.

Now you can connect to the Peregrine via http://localhost:8080, log in and
then you are redirected to the Peregrine CMS Welcome Page.

**Attention**: in case of issues please try to delete the /launcher
and the /target folder manually.
