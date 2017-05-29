# front end developer

## create a new component 

### from scratch

- create a definition
- create a stub
- update the html of the stub

### from pre existing html

- create a definition
- extract html from existing html

### build

### use

### making a component ready for the editor

#### data-per-page

Peregrine-cms uses an attribute `data-per-page` on your root element of a component
to identify a section of the generated html as editable. 

#### empty components

When adding a component to a page and rendering it the first time without any data
your component may collapse to a zero height. Due to the zero height and author is
not able to select the component for further editing as there is no area where the
author could click to edit the component. To mitigate this problem, peregrine-cms
sets a variable on the page to indicate if the page is currently in editing mode. 
A component should check this variable and render a special placeholder div allowing
the author to select the component. 

