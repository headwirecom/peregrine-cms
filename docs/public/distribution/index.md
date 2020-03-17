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

1. Local, in-Peregrine Copies
1. Remote Sling Distributions (Peregrine to remote Peregrine)
1. Local File System
1. S3 Bucket
1. Default Distribution

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

# Local, intra-Peregrine Copies

This distributions allows to copy resources to another folder in the JCR of Peregrine.
This service copies the resource and any references to other resources (like templates)
to another folder. To make this work you need to configure the service
**com.peregrine.admin.replication.impl.LocalReplicationService**:

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

This distributions allows to copy resources from one Peregrine to another, remote
Peregrine instance using the **Sling Distribution** service. This is mostly used to replicate
content from an **Author to a Publish** instance but it is not limited to that.
 To configure this distribution service: **com.peregrine.admin.replication.impl.DistributionReplicationService**:

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

1. Create an **Author** and **Publish** instance using **percli** service (*percli server start --author* /
*percli server start --publish*)
2. Stop both servers with *percli server stop*
3. Edit **sling/sling.properties** files
    1. Add this line to the Author: **sling.run.modes=author,notshared**
    2. Add this line to the Publish: **sling.run.modes=publish,notshared**

or

1. Start and Stop two Peregrine Sling instance with the Peregrine Sling JAR file
2. Edit **sling/sling.properties** files
    1. Add this line to the Author: **sling.run.modes=author,notshared**
    2. Add this line to the Publish: **sling.run.modes=publish,notshared**
3. Restart both Peregrine instances

### Configure Author Instance

The only things that needs to be adjusted is the URL that points to the **Publish**
on the **Author** and then let the Peregrine Replication Service know about
that . Do this:

1. Open [OSGi System Console Configuration](http://localhost:8080/system/console/configMgr)
1. Search for **Forward Agents Factory**
1. Either select an existing Forward Agents and click on edit or create a new one by clicking the
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

In the **OSGi System Console Configuration make sure these services are configured:

* Forward Agent
* Vault Package Builder Factory
* Privilege Request Authorization Strategy
* User Credentials based DistributionTransportSecretProvider
* Peregrine: Remote Replication Service

**Attention**: make sure the *User Credentials based DistributionTransportSecretProvider* has
the correct credentials as by default it is set to the default Sling admin password.

### Configure Publish Instance

On the **Publish** site there is nothing to be done

# Local File System Copies

This distribution service will replicate the Peregrine content as files in a given target folder. Regular
resources (not folders) are exported as **.data.json**, assets as **images** including their renditions
and folders are created on a need to have basis.

Example Local File Output Tree Structure of */content** with deep distribution (look at the **staticreplication**
folder):

![Deep Local FS Export File System Tree](distribution.local.file.system.result.tree.png)

To configure this distribution service: **com.peregrine.admin.replication.impl.LocalFileSystemReplicationService**:

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

# S3 Bucket

Peregrine allows the user to setup a distribution of content to an AWS S3 service. In a nutshell
this service is more or less the same as the **local file system copies** just copying in to an
S3 rather than writing to a local file.

To configure this distribution service: **com.peregrine.admin.replication.impl.RemoteS3SystemReplicationService**:

|Name|Parameter|Required|Type|Default|Description|
|:---|:--------|:-------|:---|:------|:----------|
|Name|name|yes|String|none|Name of the Distribution Service|
|Description|description|no|String|none|Description of this Service|
|AWS Access Key|awsAccessKey|yes|String|none|Access Key for the S3 Service|
|AWS Secret Key|awsSecretKey|yes|String|none|Secret Key for the S3 Service|
|AWS Region Name|awsRegionName|yes|String|none|Region of your S3 Service|
|AWS Bucket Name|awsBucketName|yes|String|none|Bucket Name of your S3 Service|
|Export Extensions|exportExtensions|yes|String|none|List of Extensions to be exported. The format is &lt;extension[~raw]>=&lt;&vert;-split list of primary types that are exported>|
|Mandatory Renditions|mandatoryRenditions|String|no|Name of the Renditions that are created (if not already done) during the distribution|

Whenever the service tries to push a change to S3 and the connection fails it will retry
once and if it fails again it will end the distribution.

# Default Distribution Mapping

In Peregrine the Default Distribution (Default Replication Mapper Service) is a
way to configure Distribution based on the path of a resource. Whenever
a resource is distributed by the name of a Default Distribution that service
will select the appropriate distribution (also by its name) and then delegate
the distribution to that service.

Default Distribution is used like any other distribution but it does not
actually do a distribution but rather delegate it to the target one.

To configure this Default Replication Mapper Service service:
**com.peregrine.admin.replication.DefaultReplicationMapperService**:

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
