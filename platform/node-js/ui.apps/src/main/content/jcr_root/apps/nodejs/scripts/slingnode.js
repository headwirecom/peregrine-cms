// we use this to overwrite a coupe of things in the filesystem
const fs = require('fs')
const util = require('util');

var originalReadFileSync = fs.readFileSync
fs.readFileSync = function(path, options) {
    if(path && path.startsWith('/')) {
        // slingnode$javalog('(slingnode.js) File starts with /: ' + path)
        if(slingnode$checkJcrPath(path)) {
            // slingnode$javalog('(slingnode.js) File JCR Checked, load now: ' + path)
            var ret = slingnode$readFromJCR(path)
            // slingnode$javalog('(slingnode.js) File Path: ' + path + ', From JCR: ' + ret)
            return ret;
        } else {
            slingnode$javalog('(slingnode.js) No JCR File: ' + path)
        }
    }
    var ret2 = originalReadFileSync(path, options)
    // slingnode$javalog('(slingnode.js) Original Read File: ' + path + ', Found: ' + ret2)
    return ret2;
}

// not sure if this works correctly
process.env['NODE_PATH'] = process.cwd()
var Module = require('module')
Module._initPaths()

var original_resolveFilename = Module._resolveFilename
Module._resolveFilename = function(request, parent, isMain) {
    slingnode$javalog('(slingnode.js) Resolve File Name: ' + request + ', parent: ' + util.inspect(parent) + ', is main: ' + isMain)
    if(request && request.startsWith('/')) {
        slingnode$javalog('(slingnode.js) File Name starts with a /: ' + request)
        // todo: this should check if the file or path exists in sling
        if(slingnode$checkJcrPath(request)) {
            slingnode$javalog('(slingnode.js) File Name is JCR Path /: ' + request)
            return request;
        }
    }
    slingnode$javalog('(slingnode.js) Try in Node.js')
    slingnode$javalog('(slingnode.js) Current Directory: ' + process.cwd())
    slingnode$javalog('(slingnode.js) Original Resolve File Name Function: ' + original_resolveFilename)
    try {
        // add the root node modules folder
        if(parent.paths.indexOf(process.cwd() + '/node_modules') < 0) {
            parent.paths.push(process.cwd() + '/node_modules')
        }
        var ret = original_resolveFilename(request, parent, isMain)
    } catch(error) {
        slingnode$javalog('(slingnode.js) Resolve File Name failed with error: ' + error)
    }
    slingnode$javalog('(slingnode.js) Resolve File Name: ' + request + ', Resolved to: ' + ret)
    return ret;
}
