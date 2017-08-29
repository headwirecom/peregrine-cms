//
// Example JS Script using node.js package 'camelcase'
//
// In order to test it make sure that the package 'camelcase' is installed
// and it must be executed through J2V8
//

slingnode$javalog('(camel-case-test.js) Start -> load camel case')

const cc = require('camelcase')

slingnode$processOutput('hello world to '+cc('hello world'));

slingnode$javalog('(camel-case-test.js) Start -> Done')
