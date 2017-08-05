var process = require('process')
console.log = function() {
    slingnode$processOutput(arguments[0].toString())
}
console.error = function() {
    slingnode$processError(arguments[0].toString())
}
var args = ['node', 'npm', 'list', '--json']

//
// Expected Parameters
//
// 1a. Package Name
// 1b. Package Name followed by Version
//
for(var i = 0; i < process.argv.length; i++) {
    args[i + 4] = process.argv[i];
}
process.argv = args
var os = require('os')
require(os.homedir() + '/node_modules/npm/bin/npm-cli.js')