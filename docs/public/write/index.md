# things we have to document so people can understand it 

A folder under /content/objects can have a allowedObjects array propety. This array 
contains a list of the names of object types that are allowed to be created in the 
folder (and any subfolder [not implemented yet]) Need to decide if we also extend 
this to the objects for the ones that allow children to be created

# page components

a component is considered a page component if it's name is page or if a property 
templateComponent="true" is set on the component. This allows for the component to be 
used as a root component of a template and drives the list of the create template wizard

# group property of components

if a component has a property group that is set to .hidden then the component does not
show up in the component explorer to be dragged onto a page

