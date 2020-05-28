process.env.NODE_PATH = process.cwd()

const Vue = require('vue')
const renderer = require('vue-server-renderer').createRenderer()

const app = new Vue({
    template: `<div>{{path}}</div>`,
    data: {
        path: 'test'
    }
})

renderer.renderToString(app, (err, html) => {
    if (err) throw err
    slingnode$httpout(html)
})