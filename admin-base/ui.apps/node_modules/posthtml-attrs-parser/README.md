# PostHTML Attrs Parser
[![npm version](https://badge.fury.io/js/posthtml-attrs-parser.svg)](http://badge.fury.io/js/posthtml-attrs-parser)
[![Build Status](https://travis-ci.org/maltsev/posthtml-attrs-parser.svg?branch=master)](https://travis-ci.org/maltsev/posthtml-attrs-parser)

[PostHTML](https://github.com/posthtml/posthtml) helper that gives a better API to work with tag's attrs.


## Usage
```js
var posthtml = require('posthtml'),
    parseAttrs = require('posthtml-attrs-parser');

posthtml()
    .use(function (tree) {
        var div = tree[0],
            attrs = parseAttrs(div.attrs);

        attrs.style['font-size'] = '15px';
        attrs.class.push('title-sub');

		// Compose attrs back to PostHTML-compatible format
        div.attrs = attrs.compose();
    })
    .process('<div class="title" style="font-size: 14px">Hello!</div>')
    .then(function (result) {
        console.log(result.html);
    });

// <div class="title title-sub" style="font-size: 15px">Hello!</div>
```


## Attributes
Only `style` and `class` attributes are parsed by default (as object and array, respectively).
For other attributes the parsing rules should be specified (see "Custom parsing rule" below).


### Default attributes
#### `style`
```js
// <div style="color: red; font-size: 14px; color: blue"></div>
var attrs = parseAttrs(div.attrs);
console.log(attrs.style);
/*
{
    // If there is several properties with the same name,
    // then the values are packed in array
    'color': ['red', 'blue'],
    'font-size': '14px'
}
*/
```


#### `class`
```js
// <div class="title title-sub"></div>
var attrs = parseAttrs(div.attrs);
console.log(attrs.class);
// ['title', 'title-sub']
```


### Custom parsing rule
You can also define the parsing rule for other attributes.

#### Array-like attribute
```js
// <div data-ids="1  2 4 5   6"></div>
var attrs = parseAttrs(div.attrs, {
    rules: {
		'data-ids': {
			delimiter: /\s+/,
            // Optional parameter for stringifying attribute's values
            // If not set, glue = delimiter
			glue: ' '
		}
	}
});
console.log(attrs['data-ids']);
// ['1', '2', '4', '5', '6']

console.log(attrs.compose()['data-ids']);
// 1 2 3 4 5 6
```


#### Object-like attribute
```js
// <div data-config="TEST=1;ENV=debug;PATH=."></div>
var attrs = parseAttrs(div.attrs, {
	rules: {
		'data-config': {
            // Delimiter for key-value pairs
			delimiter: ';',
            // Delimiter for a key-value
			keyDelimiter: '=',

            // Optional parameter for stringifying key-value pairs
            // If not set, keyGlue = keyDelimiter
			glue: '; ',
            // Optional parameter for stringifying a key-value
            // If not set, glue = delimiter
            keyGlue: ' = '
		}
	}
});
console.log(attrs['data-config']);
// {TEST: '1', ENV: 'debug', PATH: '.'}

console.log(attrs.compose()['data-config']);
// TEST = 1; ENV = debug; PATH = .
```
