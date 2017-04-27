var sass = require('node-sass');
var fs = require('fs-extra');

fs.mkdirs('./target/classes/etc/felibs/materialize')
sass.render({
  file: 'peregrine.scss',
}, function(err, result) { 
	fs.writeFileSync('./target/classes/etc/felibs/materialize/materialize.css', result.css) 
});
