process.env.NODE_PATH = process.cwd()

const Svelte = require('svelte')
const renderer = require('svelte-server-renderer').createRenderer()

const app = new Svelte({
    template: `<div>{{path}}</div>`,
    data: {
        path: 'test'
    }
})

renderer.renderToString(app, (err, html) => {
    if (err) throw err
    slingnode$httpout(html)
})