<template>
  <div v-bind:data-per-path="model.path">
    <p>Search the Peregrine CMS marketplace for extensions and samples to install</p>
    <form>
      <input type="text" v-model="app.filter">
    </form>
  </div>
</template>

<script>
    export default {
        props: ['model'],
        computed: {
            app() {
                const app = $peregrineApp.getView().app
                if(!app.filter) {
                    Vue.set(app, 'filter', '')
                }
                return $peregrineApp.getView().app
            },
            filter() {
                const app = $peregrineApp.getView().app
                if(!app.filter) {
                    Vue.set(app, 'filter', '')
                }
                return app.filter
            }
        }
    }
</script>

