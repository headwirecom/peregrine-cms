# Project Structure

We use a single git repo at the moment for all of PER:CMS. Each subfolder off the root
of the git repo is a project by itself and could be potentially moved into their own
git repository. 

### resources
Just a copy of the current version of Apache Sling-9 we are using as a reference to 
develop PER:CMS before Apache Sling-9.

### nodetypes
Definitions of nodetypes necessary for PER:CMS. This should be as light as possible. 
We are currently defining the following node types: 

- per:Page
- per:PageContent
- per:Component
- per:FeLib

### base
The base module pulls in a couple of outside dependencies that are not part of Apache
Sling-9 but are required for PER:CMS to work. 

- sling-node (support for nodejs within sling using J2V8)
- sling-swagger-ui (the openapi web ui to test our api endpoints)
- PER:CMS > nodetypes

### felib
A simple frontend library handler. A node of type per:FeLib with a child js.txt and 
css.txt is used to assemble a single css and js file for the library. The syntax of
the .txt files is straight forward: 

Each line in the file represents a path to a file to be included in the library. Any
path starting with / is considered from the content root, otherwise it's considered 
a relative path. A special keyword `base=` is used to set a new current path relative
to the current path.

### pagerender-vue
VueJS based rendering infrastructure. 

### admin-base
Administrator interface - VueJS based implementation

### example-vue-site
An example VueJS based site. 

### docs
that's where you can find the current documentatijon, hopefully this will be served from 
a PER:CMS instance soon.  