var fs = require('fs-extra');

fs.mkdirs('./target/classes/etc/graphiql')
fs.copySync('node_modules/graphiql/graphiql.min.js', 'target/classes/etc/graphiql/graphiql.min.js')
fs.copySync('node_modules/graphiql/graphiql.min.js.map', 'target/classes/etc/graphiql/graphiql.min.js.map')
fs.copySync('node_modules/graphiql/graphiql.min.css', 'target/classes/etc/graphiql/graphiql.min.css')

fs.copySync('node_modules/react/cjs/react.development.js', 'target/classes/etc/graphiql/cjs/react.development.js')
fs.copySync('node_modules/react/cjs/react.production.min.js', 'target/classes/etc/graphiql/cjs/react.min.js')
fs.copySync('node_modules/react/umd/react.development.js', 'target/classes/etc/graphiql/umd/react.development.js')
fs.copySync('node_modules/react/umd/react.production.min.js', 'target/classes/etc/graphiql/umd/react.min.js')

fs.copySync('node_modules/react-dom/cjs/react-dom.development.js', 'target/classes/etc/graphiql/cjs/react-dom.development.js')
fs.copySync('node_modules/react-dom/cjs/react-dom.production.min.js', 'target/classes/etc/graphiql/cjs/react-dom.min.js')
fs.copySync('node_modules/react-dom/umd/react-dom.development.js', 'target/classes/etc/graphiql/umd/react-dom.development.js')
fs.copySync('node_modules/react-dom/umd/react-dom.production.min.js', 'target/classes/etc/graphiql/umd/react-dom.min.js')
