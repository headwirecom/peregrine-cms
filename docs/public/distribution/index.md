peregrine cms distribution
=====

# Introduction

Peregrine supports the content distribution through various means out of the box. This document shows
you the supported mechanism and how to set them up.

Keep in mind that in Sling Distribution is what Replication is in AEM. The code uses **replication**
instead of **distribution** to avoid confusion with Sling Distribution. In this documentation we use
the term Distribution to go along with the Sling parlance.

# Supported Distributions

These are the current supported distributions:

1. Local File System
1. Remote Sling Distributions (Peregrine to remote Peregrine)
1. Default Distribution
1. Local, in-Peregrine Copies (deprecated)

**Attention**:

Assets are replicated out of the box but for external system like the local file system
the **asset rendition** must be setup to reliably replicate all desired renditions.

# General Usage

Distribution can be executed using the **repl.json** admin action. It is a POST call
having these parameters:

|Name|Required|Type|Default|Description|
|:---|:-------|:---|:------|:----------|
|name|yes|String|none|Name of the Distribution Service|
|deep|no|boolean|false|Replicate children as well|
|deactivate|no|boolean|false|Deactivate / Remove replicants|

Please use the [Swagger UI](http://localhost:8080/perapi/swaggereditor/), look for **repl.json**
and click on **try it out**, enter the parameter values on the click on **Execute**.

The **name** of the distribution service is the name of a service that implements the
*com.peregrine.replication.Replication* interface. If the name could not be found
the call ends with an exception.

# Local, intra-Peregrine Copies (deprecated)

This distributions allows to copy resources to another folder in the JCR of Peregrine.
This service copies the resource and any references to other resources (like templates)
to another folder. To make this work you need to configure the service
**com.peregrine.replication.impl.LocalReplicationService**:

|Name|Parameter|Required|Type|Default|Description|
|:---|:--------|:-------|:---|:------|:----------|
|Name|name|yes|String|none|Name of the Distribution Service|
|Local Mapping|localMapping|yes|String|none|Folder Mapping, format: &lt;absolute folder path>=&lt;absolute target path>|

Out of the box there is a service called **local** that has the this local mapping: **/content=/live**. 

![OSGi System Console Config for Local](distribution.in.sling.local.configuration.png)

Because this is copying resources in Peregrine the renditions are only copied
if already created. If not then Peregrine will create them when the desired
rendition in the target is obtained.

# Peregrine to Peregrine Copies

This distribution allows to copy resources from one Peregrine to another, remote
Peregrine instance using the **Sling Distribution** service. This is mostly used to replicate
content from an **Author to a Publish** instance but it is not limited to that.
 To configure this distribution service: **com.peregrine.replication.impl.DistributionReplicationService**:

|Name|Parameter|Required|Type|Default|Description|
|:---|:--------|:-------|:---|:------|:----------|
|Name|name|yes|String|none|Name of the Distribution Service|
|Forward Agent|agentName|yes|String|none|Name of the Sling Distribution Forward Agent|

Out of the box there is a service called **remote** that has uses a Forward Agent: **publish**. 

![OSGi System Console Config for Remote](distribution.inter.sling.remote.configuration.png)

## Sling Distribution Setup

In order to make this work the Sling Distribution must be configured right. For this we only
need a Forward Agent on the **Source (Author)** Peregrine instance and a Local Distribution Package
Importer Factory on the **Target (Publish)** Peregrine instance.

The setup is not easy but Peregrine comes with a pre-configured setup out of the box. To enable it
go through these steps.

### Setup Runmodes for Author / Publish

A Peregrine **Sample** setup is provided with the module **distribution** which contains the necessary
setup for it to work. The important step is to set the **runmodes** accordingly. The available runmodes
are **author, publish, notshared, shared**.

To set up a configuration do:

1. Obtain the peregrine-builder
   1. Follow instructions from https://github.com/peregrine-cms/peregrine-builder
1. Create project directories 
   1. mkdir ~/per-projects
   1. cd ~/per-projects
   1. mkdir author
   1. mkdir publish
1. Copy Peregrine Builder artifacts into ~/per-projects/author and ~/per-projects/publish
   1. com.peregrine-cms.sling.launchpad-12-SNAPSHOT-oak_tar_fds_far.far
   1. org.apache.sling.feature.launcher.jar
1. Initialize author instance (from author folder)
   ```
   java -jar org.apache.sling.feature.launcher.jar -f com.peregrine-cms.sling.launchpad-12-SNAPSHOT-oak_tar_fds_far.far -D sling.runmodes=author,notshared,oak_tar_fds -p sling
   ``` 
   Note: `-D sling.runmodes=author,notshared,oak_tar_fds` is only needed the first time
1. Initialize publish instance (from publish folder)
   ```
   java -jar org.apache.sling.feature.launcher.jar -f com.peregrine-cms.sling.launchpad-12-SNAPSHOT-oak_tar_fds_far.far -D sling.runmodes=publish,notshared,oak_tar_fds -D org.osgi.service.http.port=8180 -p sling
   ```
1. Install Peregrine CMS
   1. clone https://github.com/headwirecom/peregrine-cms
   1. install to the running author instance `mvn clean install -P autoInstallPackage`
   1. installing to the running publish instance `mvn clean install -P autoInstallPackage -Dsling.port=8180`
1. Server Side Junit Test  
   Executing the Sling Junit test (RemoteReplAuthorJTest) from author may be useful for determining whether the setup is correct.
   ```
   http://localhost:8080/system/sling/junit/com.peregrine.slingjunit.author.RemoteReplAuthorJTest.html
   ```

The procedure above will configure Peregrine CMS instance based on the sling runmodes. 
Additional docs below describe the configurations in more detail in case a manual approach is needed or desired.     


### Configure Author Instance

The only things that needs to be adjusted is the URL that points to the **Publish**
on the **Author** and then let the Peregrine Replication Service know about
that . Do this:

1. Open [**OSGi System Console Configuration**](http://localhost:8080/system/console/configMgr)
1. Search for [**Forward Agents Factory**](http://localhost:8080/system/console/configMgr/org.apache.sling.distribution.agent.impl.ForwardDistributionAgentFactory)
1. Either select an [existing Forward Agent](http://localhost:8080/system/console/configMgr/org.apache.sling.distribution.agent.impl.ForwardDistributionAgentFactory~publish)
   and click on edit or create [a new one](http://localhost:8080/system/console/configMgr/org.apache.sling.distribution.agent.impl.ForwardDistributionAgentFactory) by clicking the
**+** sign on the Factory
1. Give it a **name**
1. Look for Property: **Importer Endpoints**
1. Adjust URL. Default Value is: http://localhost:8180/libs/sling/distribution/services/importers/default. The thing
to adjust is the server name and its port
1. Click on **save**
1. Search for **Peregrine: Remote Replication Service**
1. Either select an existing Remote Replication Service and click on edit or create a new one by clicking the
**+** sign on the Factory
1. Give it a **name** (which is name used in the repl.json)
1. Add the name of the forward agent into **Forward Agent**
1. Click on **save**

#### Verification

In the [**OSGi System Console Configuration**](http://localhost:8080/system/console/configMgr) make sure these services are configured:

* [Forward Agent](http://localhost:8080/system/console/configMgr/org.apache.sling.distribution.agent.impl.ForwardDistributionAgentFactory~publish)
* [Vault Package Builder Factory](http://localhost:8080/system/console/configMgr/org.apache.sling.distribution.serialization.impl.vlt.VaultDistributionPackageBuilderFactory~default)
* [Privilege Request Authorization Strategy](http://localhost:8080/system/console/configMgr/org.apache.sling.distribution.agent.impl.PrivilegeDistributionRequestAuthorizationStrategyFactory~default)
* User Credentials based [DistributionTransportSecretProvider](http://localhost:8080/system/console/configMgr/org.apache.sling.distribution.transport.impl.UserCredentialsDistributionTransportSecretProvider~default)
* Peregrine: [Remote Replication Service](http://localhost:8080/system/console/configMgr/com.peregrine.replication.impl.DistributionReplicationService~remote)

**Attention**: make sure the *User Credentials based DistributionTransportSecretProvider* has
the correct credentials as by default it is set to the default Sling admin password.

### Configure Publish Instance

On the **Publish** site there is nothing to be done

# Local File System Copies (Default)

This distribution service will replicate the Peregrine content as files in a given target folder. Regular
resources (not folders) are exported as **.data.json**, assets as **images** including their renditions
and folders are created on a need to have basis.

Example Local File Output Tree Structure of */content** with deep distribution (look at the **staticreplication**
folder):

![Deep Local FS Export File System Tree](distribution.local.file.system.result.tree.png)

To configure this distribution service: **com.peregrine.replication.impl.LocalFileSystemReplicationService**:

|Name|Parameter|Required|Type|Default|Description|
|:---|:--------|:-------|:---|:------|:----------|
|Name|name|yes|String|none|Name of the Distribution Service|
|Description|description|no|String|none|Description of this Service|
|Target Folder|targetFolder|yes|String|none|Absolute Path to the Output Folder. Can use place holders inside ${} which supports Java / Sling properties|
|Export Extensions|exportExtensions|yes|String|none|List of Extensions to be exported. The format is &lt;extension[~raw]>=&lt;&vert;-split list of primary types that are exported>|
|Creation Strategy|creationStrategy|yes|int|1|If target folder(s) is missing what is created (0: none, 1: leaf folder, 2: all folders (mkdirs)|
|Mandatory Renditions|mandatoryRenditions|String|no|Name of the Renditions that are created (if not already done) during the distribution|

**Note**: For the **Export Extensions** the extension (key) can be either a regular extension without a leading dot
or it can be a ** * ** if the file is exported with the name of the node (for example NT Files). The extension can also
have the **~raw** suffix which indicates that the content is exported as byte array rather than as text which should
be done for files.

**Attention**: to learn more about the **Renditions** please go to the Renditions Documentation.

Peregrine comes with a default configuration called **localFS** that will export the content into *Sling's Home folder*/staticreplication:

![OSGi System Configuration for Local FS](distribution.local.file.system.configuration.png)

`Local FS` distribution relies on versions, and the renditions are based on `VersioningResourceResolver`.
You can read more about it [here](../versioning/index.md). We are basically using this resolver to render stuff from
the point of view of `Published` versions only.

In addition to regular resources, `local FS` stores the sitemap under `[..]/content/<tenant>/pages.sitemap.xml`
location, also using the `VersioningResourceResolver` to process `Published` pages only.

# Default Distribution Mapping

In Peregrine the Default Distribution (Default Replication Mapper Service) is a
way to configure Distribution based on the path of a resource. Whenever
a resource is distributed by the name of a Default Distribution that service
will select the appropriate distribution (also by its name) and then delegate
the distribution to that service.

Default Distribution is used like any other distribution but it does not
actually do a distribution but rather delegate it to the target one.

To configure this Default Replication Mapper Service service:
**com.peregrine.replication.DefaultReplicationMapperService**:

|Name|Parameter|Required|Type|Default|Description|
|:---|:--------|:-------|:---|:------|:----------|
|Name|name|yes|String|none|Name of the Distribution Service|
|Description|description|no|String|none|Description of this Service|
|Default|defaultMapping|yes|String|none|Name of the Distribution service that is used by default|
|Path Mapping|pathMapping|no|String|none|Target Distribution for a sub path. Format &lt;distribution name>:path=&lt;root path>[&lt;pipe>(&lt;parameter key>=&lt;parameter value>)*]|

**Attention**: The Peregrine UI uses the Default Distribution called **defaultRepl** as
the default distribution for its UI. It is important that this service is configured
appropriately otherwise the UI will not be able to distribute.

That said there can be many more default distribution created and set up as a convenient
way to manage contribution.
