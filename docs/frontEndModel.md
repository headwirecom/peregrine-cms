# Front End Model

`per:cms` uses a front end model to provide all the information necessary for the UI
to render the admin application and the sites. This model is at the core of `per:cms`
and is described in this document. 

## root structure

The following elements can be found at the root of the `perAdminView` variable. 

```aidl
{
    status: '',     // loading or loaded
    adminPage: {},  // the admin page currently rendered
    admin: {},      // data from the server in support of the admin UI
    pageView: {},   // the page that is currently in the editor
    pages: {},      // page tree information
    state : {}      // information about the current state 
}
```

the structure so far has grown organically and probably should be revisited with better 
reasoning behind every node and what it is for. 

### status

Status is either 'loading' or 'loaded' and only used during the first load of the page. 

### adminPage

The current page of the admin user interface as returned by the .data.json extension of 
any administration page. Admin pages are located under /content/admin and use a 
`jcr:resourceType=per:Page` and `sling:resourceType=admin/components/toolingpage`

The nodes generally follow the structure of

```aidl
{
  path: '',            // path to the node in the repository where this node came from
  component: '',       // vue component name
  *,                   // any additional properties needed to render this node
  children: []         //  list of child nodes if children exist
}
```

### admin

- `admin.tools[]` a list of the main tools and a link to the corresponding page in the
admin UI tree. 
- `admin.toolsConfig[]` a list of the configuration tools
- `admin.components{}` available components 

### pageView

The current page in the editor following a similar structure as the adminPage

```
pageView.path: '',      // path to the page currently displayed
pageView.page: {}       // the content from the page (.data.json representation)
```

### pages

pages content structure, data is grabbed through the composum tree servlet

### state

state information we would like to keep for a while (eg whenever you get back to a 
tool it would be great to get to the same place again). This data structure can 
potentially be saved per user

```
 "state": {
    "editor": {
      "path": "/content/sites/example/jcr:content/content/row/col1/text2",
    },
    "tools": {
      "pages": {
        "value": "/content/sites"
      }
    }
  }
```
