//
// Example JS Script using node.js package 'camelcase'
//
// In order to test it make sure that the package 'camelcase' is installed
//

slingnode$javalog('(sub-jcr-script.js) Start')

const camelCase = require('camelcase')

var first = 'first test'

slingnode$javalog('(sub-jcr-script.js)Camel Case: '+camelCase(first))

slingnode$processOutput('From Sub-Script: Camel Case: '+camelCase(first) + '\n');

slingnode$javalog('(sub-jcr-script.js) End')
