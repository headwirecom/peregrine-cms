# full stack deployment (client view)

It is not the intention of pcms to become a monolithic application. As such
we use a full stack deployment philosophy. PCMS can however act as the 
implementation of some of the additional services for a leaner deployment. 
We achieve this by using APIs for the different functions

- load balancer
- web server (apache httpd)
- cms (peregrine)
- search/surface (apache solr)
- processing platform (apache camel)
- messaging platform (activeMQ)

## session establishment

- We expect a session to be established on the load balancer or the 
  web server tier for all clients
- The session should pass a userId/groupId/featureId to the CMS
- The CMS pairs all users of the groupId with a default user for the group
- The CMS can establish a user for the userId/groupId
- The Web Server adds selectors for groupId/featureId to the requests

## authentication

- Users are authenticated by a 3rd party system

## search

- search is offloaded

## 3rd party data

It is common to surface and use 3rd party data in a CMS to make rendering
decisions and help authors with their tasks (for example the use of analytics
data). This 3rd party data should be indexed and made available in the autorhing UI
