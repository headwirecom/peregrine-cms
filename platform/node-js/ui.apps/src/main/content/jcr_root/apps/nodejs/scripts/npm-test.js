var process = require('process')
console.log = function() {
    slingnode$httpout(arguments[0].toString())
}
var myargs = process.argv
console.log('# og Arguments: ' + myargs.length + '\n')
console.log('Arguments: ' + myargs + '\n')
if(myargs.length > 2) {
    process.argv = ['node', 'npm', 'list', '--json', myargs[1], myargs[2]]
} else if(myargs.length > 1) {
    process.argv = ['node', 'npm', 'list', '--json', myargs[1]]
} else {
    process.argv = ['node', 'npm', 'list', '--json']
}
var os = require('os')
require(os.homedir() + '/node_modules/npm/bin/npm-cli.js')