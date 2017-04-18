base peregrine-cms package
==========================

## Introduction

**peregrine-cms** is an Apache Sling Headless CMS with an API first apporach.
This package deploys a set of basic components into Apache Sling that are
used by the CMS

- OpenAPI (Swagger-UI) to render the APIs of the CMS
- J2V8 NodeJS Egnine to augment Apache Sling with NodeJS support

## Install

In order to use J2V8 we have to install the engine one time (switch the
install target based on your opeating system - the install steps assume
that there is a running AEM instance)

```
> mvn clean install -PinstallJ2V8Windows
```

We can then continue to install the base package

```
> mvn clean install -PautoInstallPackage
```

## Use

Go to http://localhost:8080/etc/openapi.html to access the API Console

## Dependencies

This module depends on:

- `com.headwire.sling:sling-swagger-ui:1.0.0-SNAPSHOT`
- `com.headwire.sling:sling-node-project:1.0.0-SNAPSHOT`
