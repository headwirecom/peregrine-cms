//
// Read and Execute Script from JCR
//
// The only thing that needs to be done is to use 'require' with an URL
// pointing to the JCR node prefixed by '/' to indicate that the
// script is loaded from a JCR node
//
// This script can only be executed through J2V8
//

// console.log('current working directory: %j', process.cwd())

// This does not work with NodeJS as it cannot find the node.
// require('/content/nodejs/example/nodejs/sub-jcr-script.js')

console.log('This does not work in NodeJS -> use J2V8 instead\n');
