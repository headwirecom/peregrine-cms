# concepts

## front end rendering

We generate the final html for a page by rendering it client side in the browser
that's displaying the page. This approach moves some of the required computer power
from the server to the client. It also drastically reduces the amount of data that's
needed to be sent to the browser and allows us to run any given website in a single
page application style

## page

A page is a renderable unit of a site and typically is used to respond to a request
from the clients browser. 

## components

A component is a single renderable entity of a site. Each component provides its own 
editing UI and can be dragged onto a page.

## templates

A template defines the basic structure of a page. Modifying a template will update all 
pages using that template

## objects

An object is used to store a set of name/value pairs and can be reused throughout the system. 

## de/activation

activation and deactivation are used to publish and unpublish an asset or set of assets
in the system

## replication

replication is the process of moving an asset from authoring to the published site
 
## packages

A zip file containing a structure with content and code that can be moved from one
environment to another. Packages are also used in the build process to bundle content
and code and deploy it to the given environment

