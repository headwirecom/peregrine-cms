// we use this to overwrite a coupe of things in the filesystem
const fs = require('fs')

var originalReadFileSync = fs.readFileSync
fs.readFileSync = function(path, options) {
    // console.log('read file sync: '+path)
    if(path.startsWith('/')) {
        slingnode$javalog('File starts with /: ' + path)
        if(slingnode$checkJcrPath(path)) {
            slingnode$javalog('File JCR Checked, load now: ' + path)
            var ret = slingnode$readFromJCR(path)
            slingnode$javalog('File Path: ' + path + ', From JCR: ' + ret)
            return ret;
        } else {
            slingnode$javalog('No JCR File: ' + path)
        }
    }
    var ret2 = originalReadFileSync(path, options)
    slingnode$javalog('Original Read File: ' + path + ', Found: ' + ret)
    return ret2;
}

var Module = require('module')
var original_resolveFilename = Module._resolveFilename
Module._resolveFilename = function(request, parent, isMain) {
    // console.log('resolveFilename: '+request)
    slingnode$javalog('Resolve File Name: ' + request + ', parent: ' + parent + ', is main: ' + isMain)
    if(request.startsWith('/')) {
        slingnode$javalog('File Name starts with a /: ' + request)
        // todo: this should check if the file or path exists in sling
        if(slingnode$checkJcrPath(request)) {
            slingnode$javalog('File Name is JCR Path /: ' + request)
            return request;
        }
    }
    slingnode$javalog('Try in Node.js')
    slingnode$javalog('Current Directory: ' + process.cwd())
    var ret = original_resolveFilename(request, parent, isMain)
    slingnode$javalog('Resolve File Name: ' + request + ', Resolved to: ' + ret)
    return ret;
}
