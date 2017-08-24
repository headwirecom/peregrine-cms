<template>
  <div v-bind:data-per-path="model.path">
    <form>
      <input type="text" v-model="app.filter">
      <button type="submit">search</button>
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
            }
        }
    }
</script>

