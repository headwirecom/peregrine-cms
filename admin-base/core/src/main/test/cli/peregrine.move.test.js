#!/usr/bin/env node

// includes
const request = require('request-promise-native')
const params  = require('commander')

// default options
let host = 'localhost'
let port = '8080'
let username = 'admin'
let password = 'admin'

// poor mans debugger output
let logDebug = false

function debug(msg) {
    if(logDebug) {
        console.log(msg)
    }
}

// parse the command line parameters
params
    .option('-H, --host', 'server name or ip')
    .option('-P, --port', 'server port')
    .option('-u, --user', 'username')
    .option('-p, --pass', 'password')
    .option('-v, --verbose', 'verbose')
    .parse(process.argv)

// set the options
if(params.host)     { host = params.host }
if(params.port)     { port = params.port }
if(params.user)     { username = params.user }
if(params.password) { password = params.password }
if(params.verbose)  { logDebug = true }

// the actual test we are performing
async function test() {
    console.log(
        `
Remove any source or target resource
-------------------------------------------------------------------------------

`
)
    // curl --user $SLING_USER:$SLING_PASSWORD -F":operation=delete" --silent "http://$SLING_HOST:$SLING_PORT/content/move-resource-test" > /dev/null
    await request.post(`http://${host}:${port}/content/move-resource-test`).form({'operation': 'delete'}).auth(username, password, true).then( (data) => {
        debug(data)
    })

console.log(`
Verify the removal (there should be no entry with name 'move-resource-test'
-------------------------------------------------------------------------------
`)

    //curl --user $SLING_USER:$SLING_PASSWORD "http://$SLING_HOST:$SLING_PORT/content.tidy.1.json"
    await request.get(`http://${host}:${port}/content.tidy.1.json`).auth(username, password, true).then( (data) => {
        debug(data)
        const res = JSON.parse(data)
        if(res['move-resource-test']) {
            throw `was expecting '/content/move-resource-test' node to be deleted`
        }
    })

}

// call the test, output error
test().catch( (error) => {
    console.log(error)
})