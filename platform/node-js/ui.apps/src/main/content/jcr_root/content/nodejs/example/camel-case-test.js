//
// Example JS Script using node.js package 'camelcase'
//
// In order to test it make sure that the package 'camelcase' is installed
// and it must be executed through J2V8
//
console.log('current working directory: %j', process.cwd())
const cc = require('camelcase')

slingnode$httpout('hello world to '+cc('hello world'));
