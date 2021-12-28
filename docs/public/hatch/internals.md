# Percli Hatch Internals

This is a document about the internals of the Percli Hatch command and how the piece work together.

## Percli Hatch Structure

Percli Hatch takes either a component name or all denoted by a '*' (keep in mind that you probably need to enclose
a star into string delimeter like ' or " to avoid issues in a shell). It will call htmltovue() on libs/htmltovue.js
for a component name otherwise htmltovueall().

For now wwe will only cover htmltovue() as the all function will just loop over all components and call htmltovue() for
each of them. The only noticable difference is that when the compile flag is set the all build will call Maven to build
and install onto Sling / Peregrine.

## Script htmltovue.js

This is just a basic overview of this script but will give some insights on how it is converting HTML to Vue to better
deal with issues or develop a custom hatch.js script file.

First major step is that it checks if the component provides an augmented functions.js script which provides updated
functions scripts. The file must be named **functions.js** and be placed in the component folder in the fragments
folder.

Then it loads the ./.properties/settings.json script which provides the appname.

Now it checks if there is a blockgenerator.txt file and if it will forgo the html to vue generation.

After that it will obtain the path to the path to the component in the UI Apps apps component, the
Core Sling Model package as well as loading the **model.json** file in the component and parse it
as JSon.

To start the conversion it will look for a **hatch.js** script in the component and if not found look for
a **htmltovue.js** script. It loads the **template.hml** file in the component using **cheerio.load()**
and look for the first child of the **body** of the cheerio load which is the content of the template.html file.

Finally we call the hatch or htmltovue script's **convert** function handing over the template.html content as first
parameter and f of functions(model.json) call.

**Note**: see below on the execution of the **hatch.js** script.

After the conversion we obtain the content of the cheerio content and convert it into HTML and take out
the content of the **body**. Then we wrap it into a &lt;template/> tag and pretty print it as
HTML.

Then we read the template.vue content in the component folder, replace the **template** section with
the newly converted content as well as escape all single quotes (') with its escaped version (\').
Write out the new template.vue file in the component as well as in the JCR Apps folder.

Handle the sample JSon files if provided and if not then and the flag for an empty sample file is
set create one. If more than one sample file exists creates a versioned XML content otherwise jsut creates
a Content XML file.

Load the **json-schema-ref-parser** and handle the model.json and dialog.json file as well as compiling it
if needed. It will also create the Sling Model that maps the model.

## Handling of the Hatch Script

The Hatch script two parameters:
1. The **Content** of the **template.html** file
2. The **f** object of the **functions.js** script created with the **model.json** JSon object

In **Themeclean-Flex** the first step is to wrap the entire content in a **themecleanflex-components-block**
tag and set the **vue model** (v-bind:model) to **model**.

After that the scripts goes through the HTML tags and handles them as necessary (see below
for more info about that).

At the end it will add an **else** branch that will show a given message in the component if the
content is edited and empty.

**Note**: the hatch scripts use either **first()** or **eq(0)** to obtain the first found tag from the
query (find()). These two usages are the same and can be used interchangeably.

## HTML Tag Discovery

There are two ways to find a HTML tag:
1. Find it globally using **$.find(...).first()**
2. Find it inside another already found tag
```js
const table = $find('table').first()
const row = table.find('tr').first()
```

If an HTML tag is not searched and not intentionally removed by the script it will be in the
resulting HTML.

## Conversion Handling Functions

Most of the **hatch.js** scripts are using conversion functions provides by the **functions.js** script
in Percli. This provides a common handling and makes it easier to write a new hatch script or to investigate
issues with it.

### Add Custom Attributes

Beside using the functions below attributes can be added manually.

Adds an attribute to the given tag:

**&lt;tag>.attr(attributeName, attributeValue)**

Example:
```js
const tr = $.find('tr.my-row').first()
tr.attr('v-for', `(data, j) in storageData`)
```
will result in:
```html
<tr class="my-row" v-for="(data, j) in storageData">
    ...
</tr>
```

Here we want to loop over data but use a different counter than the default of **i** that
**addFor()** would provide.

### Add If

Add a **v-if** attribute to the given tag:

**f.addIf(tag, rule)**

Example:
```js
const table = $.find('table.selected').first()
f.addIf(table, 'model.mobiletablestyle === ""')
```
will result in:
```html
<table v-if="model.mobiletablestyle === &quot;&quot;">
    ...
</table>
```

### Add Else

Add a **v-else** attribute to the given tag:

**f.addElse(tag, rule)

Example:
```js
const table = $.find('table.notselected').first()
f.addElse(table)
```
will result in:
```html
<table v-else="">
    ...
</table>
```

**Note**: if no rule is provided an empty rule is added.

### Add Style

Adds a **v-bind:style** to the tag and removes an existing **style** attribute

**f.addStyle(tag, cssName, value, extension)

**Note**: extension is optional

Example:
```js
const td = $.find('td').first()
f.addStyle(td, 'color', "active[j] ? 'var(--text-secondary-color) !important' : ''")
```
will result in:
```html
<td v-bind:style="`background:${active[j] ? 'var(--color-red-500) !important' : ''};">
    ...
</td>
```

**Note**: you can add multiple style attributes to the same tag not just one.

### Add For

Adds a **v-for** and **:key** attribute to the tag to handle looping over an array of data.

**f.addFor(tag, name, item, remove)**

**Note**: remove is optinal and the default is **true**

Example:
```js
const tr = $.find('tr').first()
f.addFor(tr, 'storageData', 'data')
f.mapField(tr.find('td').first(), 'data[j]', false)
```
will result in:
```html
<tr v-for="(data, i) in storageData" :key="data.path || i">
    <td>{{data[i}}</td>
    ...
</tr>
```

**Attention**: the 3rd parameter indicates if any following sibling tags are removed and the
default is true. So if for example in a table there are columns following the column we loop
over and the 3rd parameter is omitted or true then these columns are removed.

### Bind Attribute

Adds an attribute to the given tag:

**f.bindAttribute(tag, attributeName, attributeValue)**

Example:
```js
const table = $.find('table').first()
f.bindAttribute(table, 'class', 'my-class')
```
will result in:
```html
<table v-bind:class="my-class">
    ...
</table>
```

### Bind Event

Adds an event to a tag most like to add a Javascript function.

**f.bindEvent(tag, event, function)**

Example:
```js
const table = $.find('table').first()
f.bindEvent(table, 'click', 'toggleColors()')
```
will result in:
```html
<table v-on:click="toggleColors()">
    ...
</table>
```

**Note**: if the function takes not arguments the '()' can be omitted. So we could use **toggleColors**
in our example.

### Map Field

Adds the name surrounded by the **{{}}** to the tag and if
* 3rd parameter is undefined adds an attribute *data-per-inline* on the tag with the given name as value
* 3rd is false it will not do anything
* ELSE will add *v-bind:data-per-inline* attribute with value '<3rd parameter>.<4th parameter>'

**f.mapField(tag, name, inlineName, inlineChildName)**

**Note**: if inlineName is false attributes are omitted.

Example:
```js
const td = $.find('td').first()
f.mapField(td, 'data[j]', false)
```
will result in:
```html
<td>
    {{data[j]}}
</td>
```
