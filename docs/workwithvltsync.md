Working with Jackrabbit File Vault Sync
=====

# Introduction 

Jackrabbit FileVault is a part of Apache Jackrabbit project. It provides a set of tools and APIs for mapping JCR repository to file system resources. FileVault official documentation can be found [here](https://jackrabbit.apache.org/filevault/).

The vlt sync command can be used to automatically synchronize files between your projects and JCR repository. This document provides instructions on how to use vlt sync for local development with Peregrine CMS.

# vlt sync

## Installation

1. Download [vault-cli-3.2.8-bin.zip](http://repo1.maven.org/maven2/org/apache/jackrabbit/vault/vault-cli/3.2.8/vault-cli-3.1.8-bin.zip) 
2. Extract downloaded file somewhere on your local file system.
3. Add bin sub-directory in the extracted folder to your PATH environment variable.
4. Make sure Peregrine CMS is running.
5. Open a command line terminal and run the following command (assuming Peregrine CMS is running on port 8080): `vlt --credentials admin:admin sync --uri http://localhost:8080/server/default install`
6. Whitelist vlt sync service in Apache Sling Login Admin Whitelist Configuration Fragmen
    1. In the browser login to Peregrine CMS instance and navigate to OSGi->Configuration in Felix Console (or go to URL http://localhost:8080/system/console/configMgr)
    2. Find Apache Sling Login Admin Whitelist Configuration Fragmen and click on + icon to add new configuration
    3. Set Name field to something, for example vault-sync. Set Whitelisted BSNs field to vault-sync. Then click Save.
7. Steps 5 and 6 have to be repeated for a new Peregrine CMS instance.

## Operation

1. Build and install your CMS project to your local Peregrine CMS if this is not done already.
2. Check if any folders are already registered with the sync service with the following command (assuming Peregrine CMS is running on port 8080): `vlt --uri http://localhost:8080/server/default status`
3. To regsiter your project folder with vlt sync service use this command: `vlt --uri http://localhost:8080/server/default register <projectPath>/ui.apps/src/main/content/jcr_root`. This has to be a path to jcr_root folder which can be a relative path. 
4. Repeat command in step 2 to see your jcr_root folder included in a list of folders registerd with the sync service. Note that multiple folders/projects can be registered.
5. Open jcr_root/.vlt-sync-config.properties and set sync-once="FS2JCR". This tells vlt to sync changes from file system to CMS only. If you leave this unset the sync service will also update files in your project when they are modified in CMS repository.
6. In order to temporarily disable sync service you can set disabled=true in cr_root/.vlt-sync-config.properties. This can be convenient if you need to deploy multiple changes or structural changes, for example if you are switching to another git branch of your project.

Once vlt sync is enable via the steps about try changing one of the source files under jcr_root folder and watch it automatically updated on the server.

See official vlt sync usage developmentation [here](https://jackrabbit.apache.org/filevault/usage.html)