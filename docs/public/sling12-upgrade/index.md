# Sling 9 to Sling 12 Upgrade

This document describes the procedure for upgrading Peregrine CMS from Sling 9
to Sling 12. The upgrade consists of two parts: updating your development 
toolchain and upgrading your repository.

## Who is this document for?

This document is geared towards:
* developers that have been working with Peregrine on Sling 9 and would
  like to move to Sling 12, 
* developers that are new to Peregrine and want to start using the latest
  release of Peregrine, and
* site administrators that need to upgrade their existing sites.

## Backup your current site(s)

If you're currently running Sling 9 and wish to migrate your content to Sling
12, perform the steps in this section. If you're not interested in migrating
your sites, feel free to skip this section.

1. Start your Sling 9/Peregrine instance if it's not already running.

2. Log into Peregrine as admin.

3. Visit [Composum](http://localhost:8080/bin/browser.html) and navigate to
   _Packages_.

4. Navigate to your site, then select the package ZIP file. It should be of 
   the form: _yoursite_-`full-package-1.0.zip`.

5. Click the _rebuild_ icon. This will ensure that all of your site's pages
   and assets are included and up-to-date in the current package backup.

6. Click the _download_ icon and save the package ZIP file to a safe location.

7. Repeat this procedure for your other sites if desired.

8. If you are performing this upgrade on the same system, you should stop your
   Peregrine service.

9. Continue to the next section.


## Upgrade development toolchain

The toolchain has changed a bit between Sling 9 and Sling 12. The major
changes are the supported versions of Node.js and Java. Please install
the following on your system.

* Java JDK 11
* Node 12.18 LTS

At the time of this writing, there are two main development branches:
* `develop` - Sling 9 branch
* `develop-sling12` - Sling 12 branch

Please switch to the `develop-sling12` branch.

## Install Sling 12

**Make sure you're running Java JDK 11 and Node 12.18 before continuing.**

1. Change into your peregrine-cms Git workspace and checkout the 
   `develop-sling12` branch.

        $ cd peregrine-cms
        $ git fetch
        $ git checkout develop-sling12
        $ git pull --rebase

2. If you previously were developing/deploying an older version of Peregrine,
   you will need to perform a one-time task, to clean the Node modules.

        $ mvn clean -PcleanNodeModules
        $ rm -rf node
        $ cd admin-base/ui.apps && npm i && cd ../..
        $ cd pagerenderer/vue && npm i && cd ../..
        $ cd buildscripts && npm i && cd ..

3. Create a target installation directory for Sling 12 and Peregrine.  For 
   demonstration, we'll create a directory in our home folder, but feel free
   to create your installation anywhere you like. 

       $ mkdir -p ~/opt/sling12

4. Build the Sling 12 Feature Launcher. Then, copy the Feature Launcher and
   Feature Model Archive (FAR) file to your Sling 12 installation directory.

       $ cd sling/peregrine-builder-sling-12
       $ mvn clean install
       $ cp target/dependency/org.apache.sling.feature.launcher.jar ~/opt/sling12/
       $ cp target/com.peregrine-cms.sling.launchpad-12-SNAPSHOT-oak_tar_fds_far.far ~/opt/sling12/

5. Change into your Sling 12 installation directory and start Sling for the first time.

       $ cd ~/opt/sling12

6. Start Sling for the first time. Note: If you prefer to use local filesystem replication,
   you should remove _author_ from the list of runmodes below, otherwise remote
   replication is assumed.

       $ java -jar org.apache.sling.feature.launcher.jar \
         -D sling.runmodes=author,notshared,oak_tar_fds \
         -f com.peregrine-cms.sling.launchpad-12-SNAPSHOT-oak_tar_fds_far.far \
         -p ~/opt/sling12 \
         -c ~/opt/sling12/launcher/cache

7. Continue to the next section.

## Install Peregrine

1. In another terminal window, change into your peregrine-cms Git workspace and
   deploy Peregrine.

       $ mvn clean install -PautoInstallPackage

2. Stop Sling 12 by entering `<CTRL>+C` in the terminal where it was started.

3. Start Sling 12 again.

       $ java -jar org.apache.sling.feature.launcher.jar \
         -D sling.runmodes=author,notshared,oak_tar_fds \
         -f com.peregrine-cms.sling.launchpad-12-SNAPSHOT-oak_tar_fds_far.far \
         -p ~/opt/sling12 \
         -c ~/opt/sling12/launcher/cache

4. Deploy themeclean-flex.

        $ git clone https://github.com/headwirecom/themeclean-flex.git
        $ git checkout develop
        $ mvn clean install -PautoInstallPackage
       
5. Open a browser and log into [Peregrine](http://localhost:8080/) as admin / admin. 

## Restore your site(s)

1. Visit [Composum](http://localhost:8080/bin/browser.html) and navigate to
   _Packages_.

2. Click the _Upload_ icon. Then, upload the site package backup you created earlier.

3. Click the _(Re)install package_ icon to restore your site.

4. Repeat for other sites.


## Peregrine changes in Sling 12

TODO
