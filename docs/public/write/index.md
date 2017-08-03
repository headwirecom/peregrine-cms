# things we have to document so people can understand it 

## allowedObjects

A folder under /content/objects can have a allowedObjects array propety. This array 
contains a list of the names of object types that are allowed to be created in the 
folder (and any subfolder [not implemented yet]) Need to decide if we also extend 
this to the objects for the ones that allow children to be created

## page components

a component is considered a page component if it's name is page or if a property 
templateComponent="true" is set on the component. This allows for the component to be 
used as a root component of a template and drives the list of the create template wizard

## group property of components

if a component has a property group that is set to .hidden then the component does not
show up in the component explorer to be dragged onto a page

## need to document the basic extension mechanism

apps/example/extensions is a sample of an extension for admin.pages. In general the page
defines an extension point with an id and the example project registers an extension for
that location. 

We still need to implement a way in the toolingpage/renderer.html to pull and define all
the extensions that can be found in the system. 

## percli changes to support author and publish node

`percli server start [-a|-p]`

## explain how distribution works

https://github.com/apache/sling/tree/trunk/contrib/extensions/distribution for more info

## documentation about the existing field types

we need to document the field types currently available in peregrine
