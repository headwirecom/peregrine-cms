How to Buid and Run PER:CMS
===================

To build and run PER:CMS you need to build all sub folders from the git repository. 

Due to some current limitations you need to build and install the `nodetypes` sub project
first follwed by the `base` sub project. 

Start an Apache Sling-9 instance
```aidl
cd resources
java -jar org.apache.sling.launchpad-9-SNAPSHOT.jar
```

Build and install PER:CMS into the running Apache Sling-9 instance

```aidl
nodetypes           > mvn clean install -PautoInstallPackage
base                > mvn clean install -PautoInstallPackage
felib               > mvn clean install -PautoInstallPackage
pagerender-vue      > mvn clean install -PautoInstallPackage
admin-base          > mvn clean install -PautoInstallPackage
example-vue-site    > mvn clean install -PautoInstallPackage
```

