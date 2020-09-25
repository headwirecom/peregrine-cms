# Apache Sling Starter for Peregrine CMS

This module is part of the [Peregrine CMS ProjectSling](https://www.peregrine-cms.com/)
It runs on [Apache Sling](https://sling.apache.org) project.


## How to run the Sling Starter module in Standalone mode

  NOTE: "mvn clean" deletes the "launcher" work directory in the project base
        directory. It is advisable to use a work directory outside of the
        project directory.

1) Build the Sling Starter using `mvn clean install` in the current directory.
2) Copy the launch to project directory `cp target/dependency/org.apache.sling.feature.launcher.jar $path-to-project`
3) Copy either the feature archive (*.far) or individual set of feature.json files to the project folder 

	 
Start an instance using the launcher specifying the aggregate feature `feature-oak_tar_fds.json` 

        java -jar org.apache.sling.feature.launcher.jar -f feature-oak_tar_fds.json -D sling.runmodes=author,notshared,oak_tar_fds -p sling

Arguments
        -p specifies sling.home and is traditionally "sling"
        -D prodive framework properties such as Sling Run Modes. 

Runmodes
While run are not central to initializing Sling. Runmodes are still important parameters for the application operations and configurations.
boot.json specifies `sling.run.mode.install.options` which are needed to be set only the first time as shown in the example above.

Browse Peregrine in:

        http://localhost:8080


