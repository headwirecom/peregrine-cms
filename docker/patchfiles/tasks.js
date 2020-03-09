const fs        = require('fs')
const http      = require('http')
const { spawn, spawnSync } = require('child_process')
const open      = require('open')
const { fetch } = require('./fetch')
const maven     = require('maven')
const os        = require('os')
const path      = require('path')
var inquirer = require('inquirer')

let port = 8080

function whatSling() {
    if(fs.existsSync('out/sling-11.jar')) {
        return 11;
    } else if(fs.existsSync('out/sling-9.jar')) {
        return 9;
    } else {
        throw('only supporting sling-9.jar and sling-11.jar');
    }
}

/**
    * Starts up the sling instance
    * @param {string} type - the run mode(standalone, author, publish)
*/
function startSling(type, debug) {
    console.log('startSling')
    require('./available.js')(['java'], function(success, tech) {
        if(success) {

            const sling = whatSling();

            if(sling === 9 && parseInt(tech.java.version[0]) >= 11) {
                console.log(`sling-9 needs java-8`);
                process.exit(0);
            }

            const args = [];

            if(parseInt(tech.java.version[0]) >= 11) {
                args.push('-XX:+UseParallelGC');
                args.push('--add-opens=java.desktop/com.sun.imageio.plugins.jpeg=ALL-UNNAMED'); 
                args.push('--add-opens=java.base/sun.net.www.protocol.jrt=ALL-UNNAMED'); 
                args.push('--add-opens=java.naming/javax.naming.spi=ALL-UNNAMED');
                args.push('--add-opens=java.xml/com.sun.org.apache.xerces.internal.dom=ALL-UNNAMED'); 
                args.push('--add-opens=java.base/java.lang=ALL-UNNAMED ');
                args.push('--add-opens=java.base/jdk.internal.loader=ALL-UNNAMED'); 
                args.push('--add-opens=java.base/java.net=ALL-UNNAMED'); 
                args.push('-Dnashorn.args=--no-deprecation-warning');
            }

	    // Fix Sling9 os.version parsing bug on GKE (Peregrine-CMS issue #308)
            let osRelease = os.release().replace('+', '');
            args.push(`-Dos.version"=${osRelease}"`);

            args.push( '-jar');
            args.push(`out/sling-${sling}.jar`);

            args.push('start');

            if(debug) {
                args.unshift("-agentlib:jdwp=transport=dt_socket,address=30303,server=y,suspend=n")
                args.unshift("-Xdebug")
            }
        
            if(type === 'standalone') {
                // do nothing
            } else if(type === 'author') {
                args.push("-Dsling.run.modes=author,notshared")
            } else if(type === 'publish') {
                args.push("-Dsling.run.modes=publish,notshared")
                args.push("-p")
                args.push("8180")
                port = 8180
            }
        
            console.log('#CMD# java',args.join(' '))
        
            const child = spawn('java', args, {
                detached: true,
                shell: true,
                stdio: 'inherit'
            })
            child.unref()
        } else {
            console.log('java is not installed, please go to https://adoptopenjdk.net/ and download Java8 LTS or Java11 LTS');
            throw('install java');
        }
    });
}

/**
    * Stops the sling instance
*/
function stopSling() {

    const sling = whatSling();
    const args = ['-jar',`out/sling-${sling}.jar`, 'stop'];

    console.log('#CMD#',args.join(' '))

    const child = spawn('java', args, {
        detached: true,
        shell: true,
        stdio: 'inherit'
    })
    child.unref()
}

/**
    * Checks if the sling instance is running by calling a GET to the system console
    * @param {boolean} errorWhenRunning - true/false on whether to throw error when running
*/
function checkIfRunning(errorWhenRunning = true, path = '/system/console/bundles.json') {
    return new Promise((resolve, reject) => {
        http.get('http://admin:admin@localhost:'+port+path, function(res) {
            let rawData = '';
            res.on('data', chunk => { rawData += chunk; });
            res.on('end', () => {
                try {
                    passed = false;
                    if(path.endsWith('.json')) {
                        let status = JSON.parse(rawData).s
                        if(status[3] === 0 && status[4] === 0) {
                            passed = true;
                        }
                        console.log('status', status)
                    } else {
                        console.log('status code of',path,res.statusCode)
                        passed = res.statusCode < 400
                    }
                    if(passed) {
                        if(errorWhenRunning) { 
                            reject() 
                        } else { 
                            resolve() 
                        }
                    } else {
                        if(errorWhenRunning) { 
                            resolve() 
                        } else { 
                            reject() 
                        }
                    }
                } catch (e) {
                    console.error(e.message);
                    reject()
                }
            })
        }).on('error', e => {
            if(e.code === 'ECONNREFUSED') {
                if(errorWhenRunning) { 
                    resolve() 
                } else { 
                    reject() 
                }
            }
            //console.error(`Got error: ${JSON.stringify(e)}`);
        })
    })
}

/**
    * Calls a timeout for the given amount of time
    * @param {number} time - the amount of time
*/
async function wait(time){
    return new Promise((resolve, reject) => {
        setTimeout(resolve, time)
    })
}

/**
    * Checks if sling has started by calling the checkIfRunning() function every 1000ms
*/
async function waitUntilRunning() {
    let stopped = true
    while(stopped){
        await wait(1000)
        await checkIfRunning(false)
            .then( () => {
                stopped = false
                console.log('sling already running')
            })
            .catch( () => {
                console.log('waiting for sling to start');  
            })

    }
    stopped = true
    while(stopped) {
        await wait(1000)
        await checkIfRunning(false, '/')
            .then( () => {
                stopped = false
                console.log('sling responding with good status')
            })
            .catch( () => {
                console.log('waiting for sling to start');  
            })
    }
}

/**
    * Gets the pcms settings file path
    * @return {String} the file path
*/
function getSettings() {
    return path.join(os.homedir(), '.pcms-servers')
}

/**
    * Lists all peregrine-cms servers on this computer
*/
function listServers() {
    const settingsFile = getSettings()
    if(fs.existsSync(settingsFile)) {
        console.log()
        console.log('[INFO] list of all peregrine-cms servers on this computer')
        const settings = JSON.parse(fs.readFileSync(getSettings()).toString())
        console.log()
        settings.forEach( (server) => {
            console.log('-', server.name, server.path)
        })
    } else {
        console.error('[ERROR] no settings file found at', settingsFile)
    }
}

/**
    * Adds a peregrine-cms server
    * @param {String} name - the server name
    * @param {String} path - the server path
*/
function addServer(name, path) {
    const settingsFile = getSettings()
    let settings = []
    if(fs.existsSync(settingsFile)) {
        console.log('list of all peregrine-cms servers on this computer')
        settings = JSON.parse(fs.readFileSync(getSettings()).toString())
    }
    settings.push( {name: name, path: path})
    fs.writeFileSync(settingsFile, JSON.stringify(settings, true, 2))
}

/**
    * Gets the server path
    * @param {String} name - the server name
    * @return {string} the sever path
*/
function getPathForServer(name) {
    const settingsFile = getSettings()
    let settings = []
    if(fs.existsSync(settingsFile)) {
        settings = JSON.parse(fs.readFileSync(getSettings()).toString())
        for(let i = 0; i < settings.length; i++) {
            if(settings[i].name === name) {
                return settings[i].path
            }
        }
    }
}

/**
    * Checks if the given path is registered in the settings
    * @param {string} path - the path
    * @return {boolean} true/false if the path is registered
*/
function pathRegistered(path) {
    const settingsFile = getSettings()
    let settings = []
    if(fs.existsSync(settingsFile)) {
        settings = JSON.parse(fs.readFileSync(getSettings()).toString())
        for(let i = 0; i < settings.length; i++) {
            if(settings[i].path === path) {
                return true
            }
        }
    }
}

/**
    * Prompts the user to enter a server name
    * @param {string} path - the path
    * @return {string} the server name that the user entered
*/
function promptAddServer(path) {
    const prompt = inquirer.createPromptModule();
    const question = [
      {
        type: 'input',
        name: 'servername',
        message: 'Register a server name to this path:',
        validate: nameRegistered
      }
    ]
    return new Promise(resolve => {
      prompt(question).then(answer => resolve(answer['servername']))
     })
}

/**
    * Validates that the server name is not already registered
    * @param {string} name - the name
    * @return {string} true if the name is not registered, otherwise the failed validation message
*/
const nameRegistered = name => {
    const settingsFile = getSettings()
    let settings = []
    if(fs.existsSync(settingsFile)) {
        settings = JSON.parse(fs.readFileSync(getSettings()).toString())
        for(let i = 0; i < settings.length; i++) {
            if(settings[i].name === name) {
                return ' This name has already been registered. Please choose a different name.'
            }
        }
    }
    return true
}

module.exports = {

    /**
        * Starts up the peregrine instance
        * @param {String} type - the run mode
        * @return {string} a new promise
    */
    startPeregrine(type,debug) {
        return new Promise((resolve, reject) => checkIfRunning().then(() => {
            console.log('sling is not running')
            let started = true;
            try {
                startSling(type,debug)
            } catch(error) {
                started = false;
                reject(error);
            }
            if(started) {
                waitUntilRunning().then((msg) => {
                    console.log(msg)
                    setTimeout( () => { open("http://localhost:"+port+"/") }, 2000);
                    resolve()
                })
            }
        }).catch((error) => {
            if(!error) {
                console.log('found an already running sling instance, aborting start')
            }
            reject(error)
        })
        )
    },

    /**
        * Stops the peregrine instance
        * @return {string} a new promise
    */
    stopPeregrine() {
        return new Promise((resolve, reject) => {
            stopSling()
        }).catch(() => {
            reject()
        })
    },

    /**
        * Gets the package list
        * @return {string} the package list
    */
    getPackageList(sling) {
        const packagesUrl = sling === 9 ?   'https://vagrant.headwire.com/peregrine/packages.txt' :
                                            'https://vagrant.headwire.com/peregrine/sling11/packages.txt'; 
        return new Promise( (resolve, reject) => {
            fetch(packagesUrl)
                .then(res => { resolve(res.data.split('\n').filter(line => line.length > 0)) })
                .catch(err => { reject(`failed to download ${packagesUrl}`) })
        })
    },

    /**
        * Installs the file to the sling instance
        * @param {String} mvn - the maven object
        * @param {string} line - the line
        * @return {string} the maven execution
    */
    installFile: async function(mvn, line) {
        // return mvn.execute(
        //     ['io.wcm.maven.plugins:wcmio-content-package-maven-plugin:install'], 
        //     {
        //         "sling.port": port,
        //         "vault.file": "out/" + line,
        //         "vault.serviceURL": "http://localhost:"+port+"/bin/cpm/package.service.html"
        //     }
        // )
        return spawnSync( 'node', [__dirname+'/../node_modules/@peregrinecms/slingpackager/bin/slingpackager', 'upload','-i', 'out/'+line] ,{
            shell: true,
            stdio: 'inherit'
    })
    },

    /**
        * Installs the packages
        * @param {array} packages - the packages
    */
    installPackages: async function(packages){
        let mvn = maven.create()
        for(let i = 0; i < packages.length; i++) {
            await module.exports.installFile(mvn, packages[i])
        }
    },

    /**
        * Sets the port
        * @param {string} port - the port
    */
    setPort: function(newPort) {
        port = newPort
    },

    /**
        * Lists all peregrine-cms servers on this computer
    */
    listServers: function() {
        listServers()
    },

    /**
        * Adds a peregrine-cms server
        * @param {String} name - the server name
        * @param {String} path - the server path
    */
    addServer: function(name, path) {
        addServer(name, path)
    },

    /**
        * Gets the server path
        * @param {string} name - the server name
        * @return {string} the path for the server
    */
    getPathForServer(name) {
        return getPathForServer(name)
    },

    /**
        * Checks if the given path is registered in the settings
        * @param {string} path - the path
        * @return {boolean} true/false if the path is registered
    */
    pathRegistered(path) {
        return pathRegistered(path)
    },

    /**
        * Prompts the user to enter a server name
        * @param {string} path - the path
        * @return {string} the server name that the user entered
    */
    promptAddServer: function(path) {
        return promptAddServer(path)
    },

    wait: wait
}
