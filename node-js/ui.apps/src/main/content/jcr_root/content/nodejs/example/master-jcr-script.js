//
// Read and Execute Script from JCR
//
// The only thing that needs to be done is to use 'require' with an URL
// pointing to the JCR node prefixed by '/' to indicate that the
// script is loaded from a JCR node
//
// This script must be executed through J2V8
//

console.log('current working directory: %j', process.cwd())

require('/content/nodejs/example/sub-jcr-script.js')

slingnode$httpout('After executing sub script\n');
console.log('Script is done')
