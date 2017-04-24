How to Buid and Run PER:CMS
===========================

To build and run PER:CMS you need to build all sub folders from the git repository and
check out `peregrine-cms` as well as `peregrine-cli` into the same folder at the moment.

### git projects

```
> git clone https://github.com/headwirecom/peregrine-cli.git
> git clone https://github.com/headwirecom/peregrine-cms.git
```

`peregrine-cli` is a node module that helps with common tasks for the `peregrine-cms`.
You can for now create a sling model from a OpenAPI definition. More helpers will be
added to this tool at a later point.

### build process

Due to some current limitations you need to build and install the `nodetypes` sub project
first followed by the `base` sub project.

Start an Apache Sling-9 instance:

```==
cd resources
java -jar org.apache.sling.launchpad-9-SNAPSHOT.jar
```

Build and install PER:CMS into the running Apache Sling-9 instance:

```
nodetypes           > mvn clean install
base                > mvn clean install -PautoInstallPackage
felib               > mvn clean install -PautoInstallPackage
pagerender-vue      > mvn clean install -PautoInstallPackage
admin-base          > mvn clean install -PautoInstallPackage
example-vue-site    > mvn clean install -PautoInstallPackage
```

next: [getting around](gettingAround.md)