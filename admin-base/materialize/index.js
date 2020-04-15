var sass = require('node-sass');
var fs = require('fs-extra');

fs.mkdirs('./target/classes/etc/felibs/materialize')
sass.render({file: 'peregrine.scss'}, (err, result) => {
  if (err) {
    console.error(err)
  }

  fs.writeFileSync(
      './target/classes/etc/felibs/materialize/materialize.css',
      result.css
  )
})
