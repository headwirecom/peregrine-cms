process.env.NODE_PATH = process.cwd()

const Vue = require('vue')
const renderer = require('vue-server-renderer').createRenderer()

const EventBus = {
  install(v, options){
    v.prototype.$eventBus = new Vue();
  }
};
Vue.use( EventBus );

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
