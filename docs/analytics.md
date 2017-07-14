# Surfacing Data in the Admin UI

Content authors need additional information about the content they are
modifying/curating. 

As such we need a good way to surface additional data in the authoring UI.

A first step is to bring in analytics data into pcms. A large set of analytics
engines exist and can be easily integrated into a web page. We should make this
data easily available at least in the explorer within pcms. 

## data api

```
/api/admin/external/data/digest.json{path}
```

what data fields are available for a given path

```
/api/admin/external/data/fields.json{path}
```

what fields have been configured to surface for a given path

```
/api/admin/external/data/fetch.json{path}?filter={name}
```
provide the configured data for the given path's immediate children

```
/api/admin/external/data/update.json{path}
data
```

update the currently stored data for a given path

## internal storage

In a minimal deployment this data can be stored within pcms

`/data` is used to store the data itself. The `/content/` part of the the 
path is replaced with `/data/` thus using the same hierarchy as in the content
folder to store this augmented information. 

## external storage

a system such as apache solr can be used to store this data instead

## consideation

pcms should not make use of the data itself on the server side but always
go through the API/a service to receive such information for abstraction 
purposes. 