//
// Example JS Script using node.js package 'camelcase'
//
// In order to test it make sure that the package 'camelcase' is installed
//
console.log('current working directory: %j', process.cwd())
const cc = require('camelcase')

console.log('hello world to '+cc('hello world'));
