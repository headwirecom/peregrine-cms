Introduction to Peregrine-CMS
===

Peregrine-CMS (PCMS) aims to provide a fast and extensible future proof website and web application 
platform. 

We currently use the following technologies to implement PCMS: 

- vue js (frontend, javascript)
- apache sling (backend, java, osgi)

In order to be future proof any of the technologies should be exchangeable at any given time. PCMS
uses well defined API's as well as Domain Specific Languages to achieve this goal. 

## APIs

### Client/Server API

check out http://localhost:8080/perapi/swaggereditor/ on a running pcms instance for the client/server api

### Javascript Abstraction Layer

We abstract the client side javascript calls to the backend at https://github.com/headwirecom/peregrine-cms/blob/master/admin-base/ui.apps/src/main/js/api.js.

## Form Definitions

We use json schemas to define our data structures and process these schemas into sling models and dialogs
with the help of a DSL. 

## percli

`percli` is our command line tool - it provides an entry point to the DSLs

### html to components (hatch)

In order to be more or less technology agnostic PCMS introduces a process to convert `html` through a `dsl`
to the target implementation. 

Let's say we have a single title/text component. The output `html` could look something like this: 

```html
<div>
    <h1>title</h1>
    text
</div>
```

We can also define the model for this component as

```json
{
    "definitions": {
        "Action": {
            "type": "object",
            "properties": {
                "component": {
                    "type": "string",
                    "source": "ignore"
                },
                "path": {
                    "type": "string",
                    "source": "ignore"
                },
                "title": {
                    "type": "string",
                    "source": "inject"
                },
                "text": {
                    "type": "string",
                    "source": "inject"
                }
            }
        }
    }
}
```

note: we have 2 additional properties here - `component` and `path`. These are necessary for
the PCMS page render. 