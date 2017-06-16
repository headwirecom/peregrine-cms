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
let index = 1

// poor mans debugger output
let logDebug = false

function debug(msg) {
    if(logDebug) {
        console.log(msg)
    }
}

function createFolder(folderPath) {
    myPath = contentUrl + folderPath
    return request.post(myPath).form({'jcr:primaryType': 'sling:OrderedFolder'}).auth(username, password, true).then( (data) => {
        console.log('Folder created on: ' + folderPath)
        debug(data)
    })
}

function createNode(folderPath, type, properties) {
    myPath = contentUrl + folderPath
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
    .option('-H, --host [host]', 'server name or ip')
    .option('-P, --port <port>', 'server port', parseInt)
    .option('-u, --user [user]', 'username')
    .option('-p, --pass [password]', 'password')
    .option('-i, --index <n>', 'index (1 - 3) of the source node to be rename', parseInt)
    .option('-v, --verbose', 'verbose')
    .parse(process.argv)

// set the options
if(params.host)     { host = params.host }
if(!isNaN(params.port)) {
    if(params.port > 0) {
        port = params.port
    } else {
        console.log('Port: \'' + params.port + '\' is not valid')
        return 1
    }
}
if(params.user)     { username = params.user }
if(params.password) { password = params.password }
if(params.verbose)  { logDebug = true }
if(params.index)    {
    if(params.index >= 1 && params.index <= 3) {
        index = params.index
    } else {
        console.log('Index: \'' + params.index + '\' is not valid')
        return 1
    }
}

// console.log('Host: ' + host + ', Port: ' + port + ', User: ' + username + ', Passsword: ' + password + ', Index: ' + index + ', Verbose: ' + logDebug)

let rootUrl = `http://${host}:${port}`
let contentUrl = `${rootUrl}/content/`
let testFolderName = 'rename-resource-test'

// the actual test we are performing
async function test() {
    console.log('Remove any source or target resource')
    console.log('-------------------------------------------------------------------------------')
    await request.post(contentUrl + testFolderName).form({':operation': 'delete'}).auth(username, password, true).then( (data) => {
        debug(data)
    })

    console.log(`Verify the removal (there should be no entry with name '${testFolderName}'`)
    console.log('-------------------------------------------------------------------------------')
    await request.get(rootUrl + '/content.tidy.1.json').auth(username, password, true).then( (data) => {
        debug(data)
        const res = JSON.parse(data)
        if(res[testFolderName]) {
            throw `was expecting '/content/${testFolderName}' node to be deleted`
        }
    })

    console.log('Create the basic folders')
    console.log('-------------------------------------------------------------------------------')
    await createFolder(testFolderName)
    await createFolder(testFolderName + "/source")
    await createFolder(testFolderName + "/target")
    await createFolder(testFolderName + "/ref")

    console.log('Create the Initial Resources')
    console.log('-------------------------------------------------------------------------------')
    await createNode(testFolderName + '/source/sourceTest1', 'nt:unstructured', 'Source Test 1')
    await createNode(testFolderName + '/source/sourceTest2', 'nt:unstructured', 'Source Test 2')
    await createNode(testFolderName + '/source/sourceTest3', 'nt:unstructured', 'Source Test 3')
    await createNode(testFolderName + '/target/targetTest1', 'nt:unstructured', 'Target Test 1')
    await createNode(testFolderName + '/target/targetTest2', 'nt:unstructured', 'Target Test 2')
    await createNode(testFolderName + '/target/targetTest3', 'nt:unstructured', 'Target Test 3')
    await createNode(testFolderName + '/ref/referenceTest1', 'per:Page', 'Reference Test 1')
    await createNode(testFolderName + '/ref/referenceTest1/jcr:content', 'per:PageContent', ['jcr:title', 'Reference-Test-1', 'ref', '/content/' + testFolderName + '/source/sourceTest1' ])

    console.log('Move the Source to the Target Folder')
    console.log('-------------------------------------------------------------------------------')

    await request.post(rootUrl + `/api/admin/rename.json/path///content/${testFolderName}/source/sourceTest${index}//to//renameTest${index}`).auth(username, password, true).then( (data) => {
        debug(data)
    })

    console.log('List the Results')
    console.log('-------------------------------------------------------------------------------')
    await request.get(contentUrl + testFolderName + '.tidy.5.json').auth(username, password, true).then( (data) => {
        console.log(data)
    })

    let query = cli.getYesNo;
    let answer = query('Shall we clean the nodes?');
    if(answer) {
        await request.post(contentUrl + testFolderName).form({':operation': 'delete'}).auth(username, password, true).then( (data) => {
            debug(data)
        })
        console.log('All test nodes were removed')
    }
}

// call the test, output error
test().catch( (error) => {
    console.log(error)
})
