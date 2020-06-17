# Feature Model based Sling Starter

## Alpha Release

Keep in mind that this module is **alpha** software intended to evaluate Peregrine CMS on Sling 12
as a Feature Model and to test its features. This is by no means considered production ready. So
please use this software responsibly.

## Introduction to Feature Model

1. Generic Overview with Links: https://github.com/apache/sling-org-apache-sling-feature/blob/master/readme.md
1. Feature Model Definition: https://github.com/apache/sling-org-apache-sling-feature-io/blob/master/design/feature-model.json

## Run Peregrine

### Prepare

The module will build the Peregrine / Sling 12 launch system using
released software. These modules must be built before launching it:

* Peregrine CMS 'develop' branch
* Themeclean-Flex 'master' branch

with `mvn clean install`.

### Build and Launch

This module can be built and launched in one step:
```
mvn clean install -P launch
```
**Note**: To only build the project then just omit the **launch** profile.

Now you can connect to the Peregrine via http://localhost:8080 and log in.
As of now you must manually access the [Peregrine Welcome Page](http://localhost:8080/content/admin/pages/index.html).

**Attention**: A Feature Model Sling instance is keeping bundles around
if a new bundle is installed with a new version number so you end up with
a Sling instance that have the same bundle in multiple time. To avoid this
just delete the **launcher** folder but you will loose any data / nodes already
created.

Andreas Schaefer, 6/10/2020