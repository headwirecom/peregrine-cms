## Sling Node Package Manager

### Introduction

This project provides a NPM based package installer
used through a JSon API on Sling to:

* Install Packages
* Remove an installed Package
* List installed Packages

The API is defined through [Swagger](http://swagger.io).

### Build and Installation

After cloning the project you can build
the project in the root with:

    mvn clean install

Installing on default Sling 9 server:

    mvn clean install -P autoInstallAll

### Testing / Usage

Both the NPM Package Manager and the Script Caller have their
API exposed as Swagger configuration and can be viewed and
executed in Sling.

To test the API click on the desired method, click on the *Try Out*
button, fill in the parameter(s) and then hit *Execute*.

Swagger will also provide you with a CURL command but be advised that
you need to add the access credentials (like '-u admin:admin') to the
call.

1. NPM Package Manager

Open the Swagger UI on this URL: http://localhost:8080/api/nodejs/swaggereditor/

1. Scrip Caller

Open the Swagger UI on this URL: http://localhost:8080/api/nodejs/execute/swaggereditor/

### Permissions

Permissions to list or modify packages are managed through
the folders under **/apps/nodejs/permission**. Any user that has
read access to the respective folder will get the permission
to execute the methods. Out of the box only user 'admin' has
the default permission.

It is a good practice to create groups for listing and modifying
packages, add these groups to the folder and then add users to
the groups. This way users can be added / removed without having
to change the permissions on this folder.

The reason why this project is not providing any groups out
of the box is the fact that Sling would wipe out any user
membership and that would defy the purpose of it.

### Attention

Please make sure that **NPM is installed** on your sling instance and accessible
from the user Sling is started with. Also make sure that the login
credentials are correct otherwise you get nothing back.

In order to use the J2V8 scripts you need to have it installed on you Sling server
and for that please look into the **j2v8** module.

The log for this app is redirected to **sling/logs/sling-node.log** and set to
trace by default.

Have fun - Peregrine CMS Team