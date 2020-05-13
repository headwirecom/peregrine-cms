# notes on how to create a basic blog site pwith pcms

## assumptions

we will use [bootstrap 4 blog](https://v4-alpha.getbootstrap.com/examples/blog/) as a
template to create a basic blog site in pcms

## create components from a static html prototype

We first have a look at the static prototype and decompose the page into components 
using the css classes for the blog:

- blog-masthead
    - blog-nav
- blog-header
    - blog-title
    - blog-description
- blog-main
    - blog-post
        - blog-post-title
        - blog-post-meta
    - blog-pagination
- blog-sidebar
    - sidebar-module
        - sidebar-module-inset
- blog-footer

The sidebar uses 3 modules at the moment: a basic text component that can be a
sidebar-module-inset (border, different background) and a dynamic list for the
archive.

We decompose this page structure into the following components

- masthead (no dialog)
- header (title, description)
- main (no dialog, container)
- post (title, date, by, container)
- pagination (no dialog)
- sidebar (inset, title, text)
- footer (text)

we also add a text component to allow for blocks of content in the post

## content structure

we will use the following content structure to begin: 

```
/content/blog/pages/index
/content/blog/pages/{other pages}
/content/blog/pages/archive
```
## install an instance of peregrine-cms

```
> npm install percli -g
> percli server install
```
## create the project

```
percli create project blog
cd blog
percli compile -d
```

## create the components

we can use the `percli` command line tool to create each component. To use the
tool we have to create a fragments folder at the top of our project

```
mkdir fragments
```

we can now create each component (use the `--container` flag for the containers)

```
percli create component masthead
percli create component header
percli create component main --container
percli create component post --container
percli create component pagination 
percli create component sidebar
percli create component footer
percli create component text
```

We now have a folder for each component in the `fragements` subfolder

```text
â””â”€â”€â”€fragments
    â”œâ”€â”€â”€footer
    â”œâ”€â”€â”€header
    â”œâ”€â”€â”€main
    â”œâ”€â”€â”€masthead
    â”œâ”€â”€â”€pagination
    â”œâ”€â”€â”€post
    â””â”€â”€â”€sidebar
```

each of those folders contains a set of files: 

```text
template.html
model.json
htmltovue.js
template.vue
```
We also automatically created a folder under 
`ui.apps/src/main/content/jcr_root/apps/blog/components` and a java model
under `core/src/main/java/com/blog/models`

(more info)

Let's build from the root folder of the project and look at pcms

```cmd
percli compile -d
```

Once the build is complete go to pcms at [http://localhost:8080]() and log in
with the default credentials (admin/admin)

![Alt text](img/admin-page.png?raw=true "Admin Page")

click on 'admin', then click on 'sites'. You should now see all the sites in
your pcms instance. The list should contain your `blog` site

click on `blog` and then on `+` (add page in the top right corner).

![Alt text](img/blog-create-page.png?raw=true "Create Index Page")

In the wizard, click on blog (the default template for our site) then click `next` and enter index
as the page name. Click `next` again and click `finish`.

![Alt text](img/create-page-step1.png?raw=true "Create Index Page Step 1")

You should now see the `index` page.

![Alt text](img/created-index-page.png?raw=true "Created Index Page")

Click on the `edit` icon to edit the page.

![Alt text](img/index-page-edit-mode.png?raw=true "Created Index Page")

We can now drag and drop components onto the page. By default we just output the
component names. 

## updating the html for the components

We now have the components for the site established but they are just a `<div>` tag 
with the name of the component. We need to update the html for each component. 

Open your favorite IDE and go to the fragments folder.
(list of all the fragments)

For each component we need to execute
```cmd
percli htmltovue <name>
```

we can now compile the project again

```cmd
percli compile -d
```

note: the content you created before is wiped out as during development we 
recreate the content on compile. We will change this later

We can now create the index page again and drag components onto the page. Since
we have not applied any styling to the site yet it does not look right yet. Let's
fix that

## adding css/js to the site

- Copy all the css files from the static html prototype to `ui.apps/src/main/content/jcr_root/etc/felibs/blog/css`
- Copy all the js files from the static html prototype to `ui.apps/src/main/content/jcr_root/etc/felibs/blog/js`

note: rename the blog.css file to zblog.css as a name ordering will be used, we could fix
this by creating a new felib with our own order.

Create a file styles.html in `ui.apps/src/main/content/jcr_root/apps/blog/components/page` with
the following text:
```html
<sly data-sly-include="/apps/pagerendervue/structure/page/styles.html"></sly>
<link rel="stylesheet" href="/etc/felibs/blog.css" type="text/css">
```

We also need to add some additional js file includes to `renderer.html` of the page component

`apps/components/page/renderer.html`

```html
<script src="https://code.jquery.com/jquery-3.1.1.slim.min.js" integrity="sha384-A7FZj7v+d/sdmMqp/nOQwliLvUsJfDHW+k9Omg/a/EheAdgtzNs3hpfag6Ed950n" crossorigin="anonymous"></script>
<script>window.jQuery || document.write('<script src="../../assets/js/vendor/jquery.min.js"><\/script>')</script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/tether/1.4.0/js/tether.min.js" integrity="sha384-DztdAPBWPRXSA/3eYEEUWrWCy7G5KFbe8fFjk5JAIxUYHKkDx6Qin1DkWx51bBrb" crossorigin="anonymous"></script>
<sly data-sly-include="/apps/pagerendervue/structure/page/renderer.html"/>
<script src="/etc/felibs/blog.js"></script>
<script>
    $peregrineApp.loadComponent('pagerendervue-components-placeholder')
</script>
```

note: we should look at this and either include the blog.css file directly or
make it so there is an extension point that does not need the HTL include. We are
inconsistent here as the js file gets included automatically

compile the project with

```cmd
percli compile -d
```

You should now be able to edit the page with the new style

## making the components authorable

At this point we just have a text field in the editor for each component. We need
to define the data for each component. To do this we can modify the model.json files
in the `fragments/<component>` folders. 

Modify the `fragments/header/model.json` file to make it look like this: 

```json
{
  "definitions": {
    "Header": {
      "type": "object",
      "x-type": "component",
      "properties": {
        "title": {
          "type": "string",
          "x-source": "inject"
        },
        "description": {
          "type": "string",
          "x-source": "inject"
        }
      }
    }
  }
}
```

This defines two data elements in the properties section named title and description. 

To see your change in pcms convert the html to vue js and compile the project by executing

```cmd
percli htmltovue header
percli compile -d
```

If you now add the header component to a page and click on it the editor will show
the two fields

![Alt text](img/blog-header-edit.png?raw=true "Blog Header Edit")

## connect the model to your html

Changes in the editor do not yet update the content on the page. We still have
a static template.html file for each component. We need to let pcms know how
to connect the data to the html by modifying the `htmltovue.js` file for each
fragment. 

Change the `fragments/header/htmltovue.js` file to

```js
module.exports = {
    convert: function($, f) {
        f.bindPath($)
        f.mapField($.find('.blog-title'), 'title')
        f.mapField($.find('.blog-description'), 'description')
    }
}
```

run the conversion and compile the project

```cmd
percli htmltovue header
percli compile -d
```

go back to a page, add the header component to it and edit the fields in the editor.
The content pane now updates while you are editing

![Alt text](img/blog-header-edit-working.png?raw=true "Blog Header Edit Working")

Click ok to save your edit. If you click on another component you loose your edit

## update all components

It's time to do the same modifications for all other components

### component: footer

We can keep the `model.json` for the footer the same and bind the rich text field
defined in the model to the first p tag in our template

```js
module.exports = {
    convert: function($, f) {
        f.bindPath($)
        f.mapRichField($.find('p').first(), 'text')
    }
}
```

convert the component and compile the project to test it out

### component: main

When inspecting the generated html for a page we notice that the children of the
main component are added at the wrong place. We can fix this by altering the
`htmltovue.js` file to indicate where we want the children to be inserted: 

```js
module.exports = {
    convert: function($, f) {
        f.bindPath($)
        f.addChildren($)
        f.addPlaceholders($.find('.blog-main'))
    }
}
```

now the children of the main component correctly show up in the right location

![Alt text](img/blog-main-setup.png?raw=true "Blog Main Setup")

We also notice that we are missing the sidebar and an area to drop in content to
the sidebar. Alter the `template.html` file to

```html
<div class="container">

    <div class="row">

        <div class="col-sm-8 blog-main">
        </div><!-- /.blog-main -->
        <div class="col-sm-3 offset-sm-1 blog-sidebar">
        </div>
    </div><!-- /.row -->

</div><!-- /.container -->
```

change the `htmltovue.js` file to

```js
module.exports = {
    convert: function($, f) {
        f.bindPath($)
        f.addChildren($.find('.blog-main'))
        f.addPlaceholders($.find('.blog-main'))
        f.addChildren($.find('.blog-sidebar'))
        f.addPlaceholders($.find('.blog-sidebar'))
    }
}
```

We now also have a sidebar container but dragging a post into either of the
containers displays the added component in both containers. We will fix this
later. 

### component: masthead

The navigation should be created automatically based on the pages in the system. 
For now we just remove the field in the `model.json` file as there is nothing to
edit here

```json
{
  "definitions": {
    "Masthead": {
      "type": "object",
      "x-type": "component",
      "properties": {
      }
    }
  }
}
```

### component: pagination

The pagination will be created dynamically. For now we will make `Older` and `Newer`
editable. 

`model.json`
```json
{
  "definitions": {
    "Pagination": {
      "type": "object",
      "x-type": "component",
      "properties": {
        "previous": {
          "type": "string",
          "x-source": "inject"
        },
        "next": {
          "type": "string",
          "x-source": "inject"
        }
      }
    }
  }
}
```

`htmltovue.js`

```js
module.exports = {
    convert: function($, f) {
        f.bindPath($)
        f.mapField($.find('.btn-outline-primary'), 'previous')
        f.mapField($.find('.btn-outline-secondary'), 'next')
    }
}
```

### component: post

The post component is a bit more complex. We have a title, a date, an author
and some freeform text. As we declared the component as a container we expect the
text to be provided by dragging in additional components. Let's first fix the html: 

```html
<div class="blog-post">
    <h2 class="blog-post-title">Sample blog post</h2>
    <p class="blog-post-meta">January 1, 2014 by <a href="#">Mark</a></p>
</div>
```

The `model.json` file should describe the fields: 

```json
{
    "definitions": {
        "Post": {
            "type": "object",
            "x-type": "container",
            "properties": {
                "title": {
                    "type": "string",
                    "x-source": "inject"
                },
                "date": {
                    "type": "string",
                    "x-source": "inject"
                },
                "author": {
                    "type": "string",
                    "x-source": "inject"
                }
            }
        }
    }
}
```

`htmltovue.js`

```js
module.exports = {
    convert: function($, f) {
        f.bindPath($)
        f.addPlaceholders($)
        f.mapField($.find('h2'), 'title')
        f.mapField($.find('a'), 'author')
        f.mapField($.find('p'), 'date')
    }
}
```

note: this does not work well due to the html structure. Need a better way to handle 
this use case - for now: 

```js
module.exports = {
    convert: function($, f) {
        f.bindPath($)
        f.addPlaceholders($)
        f.mapField($.find('h2'), 'title')
    }
}
```

```html
<div class="blog-post">
    <h2 class="blog-post-title">Sample blog post</h2>
    <p class="blog-post-meta">{{model.date}} by <a href="#">{{model.author}}</a></p>
</div>
```

### component: sidebar

`template.html`

```html
<div class="sidebar-module">
    <h4>About</h4>
    <p>Etiam porta <em>sem malesuada magna</em> mollis euismod. Cras mattis consectetur purus sit amet fermentum. Aenean lacinia bibendum nulla sed consectetur.</p>
</div>
```

`htmltovue.js`

```js
module.exports = {
    convert: function($, f) {
        f.bindPath($)
        f.mapField($.find('h4'), 'title')
        f.mapRichField($.find('p'), 'text')
        f.bindAttribute($, 'class', 'style')
    }
}
```

`model.json`

```json
{
  "definitions": {
    "Sidebar": {
      "type": "object",
      "x-type": "component",
      "properties": {
        "title": {
          "type": "string",
          "x-source": "inject"
        },
        "text": {
          "type": "string",
          "x-source": "inject",
          "x-form-type": "texteditor"
        },
         "style": {
           "type": "string",
           "x-source": "inject"
         }
      }
    }
  }
}
```

### component: text

`htmltovue.js`

```js
module.exports = {
    convert: function($, f) {
        f.bindPath($)
        f.mapRichField($.find('div'), 'text')
    }
}
```

## test out the components

Make sure you have converted each component with `percli htmltovue <component>` 
and compiled the project with `percli compile -d`.

You can now create a page and test out all the components again. 

## creating object types

We can either create a page for each blog entry or we can opt to use the object
model within pcms to create our blog entries and for example also our authors. 

Since this is a simple blog we'll opt to use objects and only control the additional 
pages as well as the `glass` for our blogroll page in pcms

Objects for our site are located in `ui.apps/src/main/content/jcr_root/apps/blog/objects`.
The objects folder does not exist by default, so let's create it. At the same time lets
also create a folder for the `post` object and the `author` object. 

In the `post` and `author` folder add the following file: 

`.content.xml`

```xml
<jcr:root xmlns:jcr="http://www.jcp.org/jcr/1.0" xmlns:nt="http://www.jcp.org/jcr/nt/1.0" xmlns:per="http://www.peregrine-cms.com/jcr/cms/1.0"
          jcr:primaryType="per:ObjectDefinition"
/>
```

`dialog.json`

```json
{ "fields":[{
  "type": "input",
  "inputType": "input",
  "label": "name",
  "model": "name",
  "placeholder": "name"
},
  {
    "type": "input",
    "inputType": "input",
    "label": "value",
    "model": "value",
    "placeholder": "value"
  }
]}
```

Let's compile our project and look at the objects tab in pcms.

![Alt text](img/objects-tab.png?raw=true "Objects Tab")

We can drill into the `blog` folder and then create an object with the (+) button
in the navigation. The wizard asks us about what object we would like to create
and a name for the object. 

![Alt text](img/object-create.png?raw=true "Object Create")

After the object is created we can click the edit icon next to the object name

![Alt text](img/object-edit.png?raw=true "Object Edit")

The object only has a name and a value field at the moment. We need to change this

We should also create a sub folder for authors and posts in the object console

![Alt text](img/author-posts-subfolders.png?raw=true "Authors/Posts Subfolder")

### creating the author object

`author/dialog.json`

```json
{ "fields":[
  {
    "type": "input",
    "inputType": "input",
    "label": "Author Name",
    "model": "name",
    "placeholder": "name"
  },
  {
    "type":"texteditor",
    "rows":10,
    "placeholder":"about",
    "label":"About the Author",
    "model":"about"
  }
]}
```

compile and use pcms to create an author, sync it back into your project

### creating the post object

`post/dialog.json`

```json
{ "fields":[{
  "type": "input",
  "inputType": "input",
  "label": "Title",
  "model": "title",
  "placeholder": "title"
},
  {
    "type": "input",
    "inputType": "input",
    "label": "Date",
    "model": "date",
    "placeholder": "date"
  },
  {
    "type": "input",
    "inputType": "input",
    "label": "Author",
    "model": "author",
    "placeholder": "author"
  },
  {
    "type":"texteditor",
    "rows":10,
    "placeholder":"text",
    "label":"Text",
    "model":"text"
  }
]}
```

compile and use pcms to create a post, sync it back into your project

todo: make the date field an actual date field :-) 

At the moment we have to type in the author but we already have author objects. It would be 
better to list the available authors and allow the user to select the author instead of typing
in a string every time. To do this we can alter the author field in the `dialog.json` file

change 

```JSON
  {
    "type": "input",
    "inputType": "input",
    "label": "Author",
    "model": "author",
    "placeholder": "author"
  },
```
to
```JSON
  {
    "type": "listselection",
    "label": "Author",
    "model": "author",
    "valuesFrom": "/content/<tenant>/objects/blog/authors.infinity.json"
  },
```

You now get a dropdown where you can choose an author instead of the previous input field. 

![Alt text](img/author-dropdown.png?raw=true "Author Dropdown")

## sync back created content into your project

- talk about intellij and the sling/aem ide plugin
- talk about import from aem
- talk about other options (packages, eclipse, brackets)

## fix the main component

## choose better fields

## create the template for the site

## author the additional pages

## enhancements to the blog sites

- add tags
- add author profile pages
