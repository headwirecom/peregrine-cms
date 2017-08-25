process.env.NODE_PATH = "./node_modules";
process.argv.push('admin')
process.argv.push('upload')
require("module").Module._initPaths();

const build = require('../../../../../buildscripts/buildvue.js')
