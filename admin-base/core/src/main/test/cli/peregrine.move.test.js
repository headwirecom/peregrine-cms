#!/usr/bin/env node

// includes
const request = require('request-promise-native')
const params  = require('commander')
const cli = require('cli-interact')

// default options
let host = 'localhost'
let port = '8080'
let username = 'admin'
let password = 'admin'
let type = 'child'

// poor mans debugger output
let logDebug = false

function debug(msg) {
    if(logDebug) {
        console.log(msg)
    }
}

function createFolder(folderPath) {
    myPath = `http://${host}:${port}/content/` + folderPath
    return request.post(myPath).form({'jcr:primaryType': 'sling:OrderedFolder'}).auth(username, password, true).then( (data) => {
        console.log('Folder created on: ' + folderPath)
        debug(data)
    })
}

function createNode(folderPath, type, properties) {
    myPath = `http://${host}:${port}/content/` + folderPath
    if(Array.isArray(properties)) {
        var r = request.post(myPath)
        var form = r.form()
        var arrayLength = properties.length / 2
        for (var i = 0; i < arrayLength; i++) {
            console.log('Add Form entry, index: '+ i + ', name: ' + properties[2 * i] + ', value: ' + properties[2 * i + 1])
            form.append(properties[2 * i], properties[2 * i + 1])
        }
        return r.auth(username, password, true).then((data) => {
            console.log('Node created on: ' + folderPath)
            debug(data)
        })
    } else {
        return request.post(myPath).form({'jcr:primaryType': type}, {'jcr:title': properties}).auth(username, password, true).then((data) => {
            console.log('Node created on: ' + folderPath)
            debug(data)
        })
    }
}

// parse the command line parameters
params
    .option('-H, --host', 'server name or ip')
    .option('-P, --port', 'server port')
    .option('-u, --user', 'username')
    .option('-p, --pass', 'password')
    .option('-t, --type', 'type of the move: child, before or after')
    .option('-v, --verbose', 'verbose')
    .parse(process.argv)

// set the options
if(params.host)     { host = params.host }
if(params.port)     { port = params.port }
if(params.user)     { username = params.user }
if(params.password) { password = params.password }
if(params.type)     { type = params.type }
if(params.verbose)  { logDebug = true }

// the actual test we are performing
async function test() {
    console.log('Remove any source or target resource')
    console.log('-------------------------------------------------------------------------------')
    await request.post(`http://${host}:${port}/content/move-resource-test`).form({':operation': 'delete'}).auth(username, password, true).then( (data) => {
        debug(data)
    })

    console.log(`Verify the removal (there should be no entry with name 'move-resource-test`)
    console.log('-------------------------------------------------------------------------------')
    await request.get(`http://${host}:${port}/content.tidy.1.json`).auth(username, password, true).then( (data) => {
        debug(data)
        const res = JSON.parse(data)
        if(res['move-resource-test']) {
            throw `was expecting '/content/move-resource-test' node to be deleted`
        }
    })

    console.log('Create the basic folders')
    console.log('-------------------------------------------------------------------------------')
    await createFolder("move-resource-test")
    await createFolder("move-resource-test/source")
    await createFolder("move-resource-test/target")
    await createFolder("move-resource-test/ref")

    console.log('Create the Initial Resources')
    console.log('-------------------------------------------------------------------------------')
    await createNode('move-resource-test/source/sourceTest1', 'nt:unstructured', 'Source Test 1')
    await createNode('move-resource-test/target/targetTest1', 'nt:unstructured', 'Target Test 1')
    await createNode('move-resource-test/target/targetTest2', 'nt:unstructured', 'Target Test 2')
    await createNode('move-resource-test/target/targetTest3', 'nt:unstructured', 'Target Test 3')
    await createNode('move-resource-test/ref/referenceTest1', 'per:Page', 'Reference Test 1')
    await createNode('move-resource-test/ref/referenceTest1/jcr:content', 'per:PageContent', ['jcr:title', 'Reference-Test-1', 'ref', '/content/move-resource-test/source/sourceTest1' ])

    console.log('Move the Source to the Target Folder')
    console.log('-------------------------------------------------------------------------------')
    var toPath="/content/move-resource-test/target"
    if(type == 'child') {
        toPath="/content/move-resource-test/target"
    } else if(type == 'before') {
        toPath="/content/move-resource-test/target/targetTest2"
    } else if(type == 'after') {
        toPath="/content/move-resource-test/target/targetTest2"
    }

    await request.post(`http://${host}:${port}/api/admin/move.json/path///content/move-resource-test/source/sourceTest1//to//${toPath}//type//${type}`).auth(username, password, true).then( (data) => {
        debug(data)
    })

    console.log('List the Results')
    console.log('-------------------------------------------------------------------------------')
    await request.get(`http://${host}:${port}/content/move-resource-test.tidy.5.json`).auth(username, password, true).then( (data) => {
        debug(data)
    })

    let query = cli.getYesNo;
    let answer = query('Shall we clean the nodes?');
    if(answer) {
        await request.post(`http://${host}:${port}/content/move-resource-test`).form({':operation': 'delete'}).auth(username, password, true).then( (data) => {
            debug(data)
        })
        console.log('All test nodes were removed')
    }
}

// call the test, output error
test().catch( (error) => {
    console.log(error)
})
