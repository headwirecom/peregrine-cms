# Peregrine Migrations

## Introduction

During the Development of Peregrine we add new features and data and some
existing customers might now have this data and so they cannot use that
feature.
In addition some stuff might have gone wrong and Peregrine might prepare
a fix for that to run over a customer's data.
For that Peregrine CMS provides a **Migration Servlet** and a pluggable
**Migration Actions** that will do the work of the migration.

## Overview

The customer can two things with the Migration Service:

1. List all available Migration Actions
2. Execute a particular Migration Action to migrate data

For **1.** the user just calls the Migration Servlet with a Get:
```
curl -u admin:admin http://localhost:8080/bin/migration
```
This will respond with a JSon object like this:
```json
{
  "migrations" : [
    {
      "name" : "assetDimension",
      "description" : "Adds Dimensions to any Assets in Peregrine if not already there",
      "lastUpdated" : "08/29/2019"
    }
  ]
}
```

**Attention**: the name in a migration object is the key that is used later
to execute it.

Now we can migrate the Assets with:
```
curl -u admin:admin http://localhost:8080/bin/migration?migrationName=assetDimension
```
If successful it will return this:
```json
{
  "name" : "assetDimension",
  "code" : 100,
  "message" : "Action Successfully Executed: 'assetDimension'"
}
```
Now all **Assets** under `/content/assets` should have properties like
this: **jcr:content/metadata/per-data/width** and **jcr:content/metadata/per-data/height**

## Design

This is the overall structure:

```
- Migration Servlet
  |- Migration Service (Migration)
    |- Migration Actions (multiples)
```

The Migration Servlet is either returning a list of registered Migration Actions
on a GET request or executes the Migration Action (if found) on a POST.

Each Migration Action provides a **Migration Descriptor** and a **Migration
Response** when executed for the Servlet to create its response.

**Migration Action** can be added to Peregrine by doing the following:

1. Create a class that implements **com.peregrine.admin.migration.MigrationAction**
2. Make it as OSGi Component:
```
@Component(
    service = MigrationAction.class,
    immediate = true
)
```

Then deploy the enclosing OSGi Bundle and the Migration Action is available
and ready to use.

 
Andreas Schaefer, 8/27/2019
