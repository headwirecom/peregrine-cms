const fs = require('fs')
const path = require('path');
console.log('=== copying node_modules dependencies ========================================');

//Find dependencies in node_modules and copy them to target

var basePath = './src/main/content/jcr_root';
var felibsPath = '/etc/felibs/admin/dependencies';
var distFelibsPath = './target/classes/etc/felibs/admin/dependencies';


const lines = fs.readFileSync(basePath + felibsPath + '/node_modules.txt', 'utf-8')
    .split(/\r?\n/)
    .filter(Boolean);

for( let dep of lines ) {
  let copyTarget; 
  let modulePath = `node_modules/${dep}`;

  //If given a file
  if (fs.lstatSync(modulePath).isFile()) {
    copyTarget = modulePath;
  }
  //If given a directory attempt to find module
  else {
    const file = fs.readFileSync(`${modulePath}/package.json`);
    const package = JSON.parse(file);

    let entry;
    entry = package.main ? package.main : 'index.js';

    // Find minified version if it exists
    if (!entry.includes('.min.js')) {
      const noExt = package.main.replace('.js', '');
      entry = noExt + (fs.existsSync(`node_modules/${dep}/${noExt}.min.js`) ? '.min.js' : '.js');
    }

    copyTarget = `${modulePath}/${entry}`;
  }

  //Copy the library into target
  const filename = path.basename(copyTarget);
  fs.copyFileSync(copyTarget, distFelibsPath + '/' + filename);

  //Update js.txt
  const jsTxt = distFelibsPath + '/js.txt';
  const cssTxt = distFelibsPath + '/css.txt';
  fs.appendFileSync( /.css$/.test(modulePath) ? cssTxt : jsTxt, filename + '\n');

  console.log(`Copying\t${copyTarget} \nto\t${distFelibsPath}/${filename}\n`)

}


