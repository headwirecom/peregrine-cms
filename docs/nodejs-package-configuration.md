# Peregrine's Node.js Package Configuration

## Introduction

A project that contains Node.js scripts with dependencies to other modules would require
a manual installation by the admin. The Peregrine Package Configuration service is there
to make it possible for the project provider to configure an automatic installation of these
modules so that they are ready when a user is using them.

## Design

The Package Configuration service is listening for changes of resources under **/conf/nodejs**.

Any resource that has been added or changed and has a **primary type** of **per:NpmPackageConfig** will be
searched for a property called **packages** (multi-string) which must have entries of this format:
`<package name>[@<package version>]`.

If the entry is having no **@** character or is the last character then the version **latest** is assumed.

**Attention**: because  Node.js modules can be installed manually either on the command line
or through the Peregrine API this service cannot determine if a module is still needed or not.
Therefore this service is **not uninstalling** a module at any time.

## Configuration

For a project to install the required Node.js packages it has to provide a resource representation
inside its deployment unit.

### 1. JCR Content Package

To create a package configuration inside a content package do the following:

1. Create a folder **/conf/nodejs** under your **jcr_root** folder
1. Create a project folder under the previously created folder like **/conf/nodejs/myExample**
1. Create a **.content.xml** file in the myExample folder with the content mentioned below
1. Add a **filter** entry into your **META-INF/vault/filter.xml** file (see below). Make sure the path limits it to your folder(s)
1. Deploy it as JCR Package

#### .content.xml resource file

This is the configuration for the **ssr.html** extension which requires both **vue** and
**vue-server-renderer** modules and is placed under **/conf/nodejs/ssr-html**:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jcr:root xmlns:jcr="http://www.jcp.org/jcr/1.0" xmlns:nt="http://www.jcp.org/jcr/nt/1.0"
          jcr:primaryType="per:NpmPackageConfig"
          jcr:title="SSR HTML"
          jcr:description="Packages needed for SSR selector"
          packages="[vue@latest,vue-server-renderer@latest]"
/>
```  

#### filter.xml

The filter to the example above is this:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<workspaceFilter version="1.0">
    <filter root="/conf/nodejs/ssr-html"/>
</workspaceFilter>
```

**ATTENTION**: it is important to add this filter pointing to the resource
you are going to add. Adding a filter like **/conf/nodejs** while wipe any
other configurations like the **ssr-html** during the installation of that
package which can lead to strange and unexpected situations.

### 2. (Content) Bundles

If you use a OSGi Bundle to deploy content then you have do to the following:

1. Create a folder **/conf/nodejs**
1. Create a JSon file called **myExample.json** in that folder with the content below
1. Add the path to your **Sling-Initial-Content** instruction for your bundle
1. Install the bundle on Sling/Peregrine

#### myExample.json file

The file must contain the resource data in JSon format:

```json
{
  "jcr:primaryType": "per:NpmPackageConfig",
  "jcr:title": "SSR HTML",
  "jcr:description": "Packages needed for SSR selector",
  "packages": ["vue@latest", "vue-server-renderer@latest"]
}

```

#### Sling-Initial-Content definition

This is how an Sling-Initial-Content configuration can look like (this would be
part of the maven-bundle-plugin configuration instructions) assuming that your
file would be in this folder `<resource folder>/SLING-INF/conf/nodejs/myExample.json`:

```xml
<Sling-Initial-Content>
    SLING-INF/conf/nodejs;path:=/conf/nodejs;overwrite=true;uninstall=false
</Sling-Initial-Content>

```
