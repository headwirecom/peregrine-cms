//
// Read and Execute Script from JCR
//
// The only thing that needs to be done is to use 'require' with an URL
// pointing to the JCR node prefixed by '/' to indicate that the
// script is loaded from a JCR node
//
// This script must be executed through J2V8
//

slingnode$javalog('(master-jcr-script.js) Start')

slingnode$javalog('(master-jcr-script.js) current working directory: ' + process.cwd())

require('/content/nodejs/example/j2v8/sub-jcr-script.js')

slingnode$processOutput('After executing sub script\n');

slingnode$javalog('(master-jcr-script.js) End')
