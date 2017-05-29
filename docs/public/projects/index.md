# starting a new project

## create project with the peregrine-cli tool

```
(future, use maven archetype for now)
per create <projectname>
cd <projectname>
```

## building a project

`mvn clean install`

## deploying a project to a server

If you're running your server locally: 

```mvn clean install -PautoInstallPackage```

you can also use a set of options to deploy to other servers by adding one or more
of the following options to your command line statement

```cmd
-Dsling.host=<ip>
-Dsling.port=<port>
-Dsling.user=<username>
-Dsling.password=<password>
```