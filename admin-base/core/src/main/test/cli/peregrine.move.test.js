/*-
 * #%L
 * admin base - Core
 * %%
 * Copyright (C) 2017 headwire inc.
 * %%
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * #L%
 */
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
    .option('-t, --type [type]', 'type of the move - can be one of these: child, before or after')
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
if(params.type)     {
    if(params.type == 'child' || params.type == 'before' || params.type == 'after') {
        type = params.type
    } else {
        console.log('Type: \'' + params.type + '\' is not valid')
        return 1
    }
}
if(params.verbose)  { logDebug = true }

let rootUrl = `http://${host}:${port}`
let contentUrl = `${rootUrl}/content/`
let testFolderName = 'move-resource-test'

// console.log('Host: ' + host + ', Port: ' + port + ', User: ' + username + ', Passsword: ' + password + ', Type: ' + type + ', Verbose: ' + logDebug)

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
    await createNode(testFolderName + '/target/targetTest1', 'nt:unstructured', 'Target Test 1')
    await createNode(testFolderName + '/target/targetTest2', 'nt:unstructured', 'Target Test 2')
    await createNode(testFolderName + '/target/targetTest3', 'nt:unstructured', 'Target Test 3')
    await createNode(testFolderName + '/ref/referenceTest1', 'per:Page', 'Reference Test 1')
    await createNode(testFolderName + '/ref/referenceTest1/jcr:content', 'per:PageContent', ['jcr:title', 'Reference-Test-1', 'ref', '/content/' + testFolderName + '/source/sourceTest1' ])

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

    await request.post(rootUrl + `/perapi/admin/move.json/path///content/${testFolderName}/source/sourceTest1//to//${toPath}//type//${type}`).auth(username, password, true).then( (data) => {
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
