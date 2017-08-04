//
// Example JS Script using node.js package 'camelcase'
//
// In order to test it make sure that the package 'camelcase' is installed
//
const camelCase = require('camelcase')

var first = 'first test'

console.log('Camel Case: '+camelCase(first))
slingnode$httpout('From Sub-Script: Camel Case: '+camelCase(first) + '\n');
