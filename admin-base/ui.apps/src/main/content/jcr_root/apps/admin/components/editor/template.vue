<template>
    <div class="editor-panel blue-grey lighten-5">
        <span class="panel-title">Editor</span>
        <form>
        <vue-form-generator
            v-if           = "this.$root.$data.state.editor"
            v-bind:schema  = "this.$root.$data.state.editor.dialog"
            v-bind:model   = "getModel(this.$root.$data.state.editor.path)"
            v-bind:options = "formOptions">
        </vue-form-generator>
        <button v-if="this.$root.$data.state.editor.dialog" class="btn" v-on:click.stop.prevent="onOk">ok</button>
        </form>
    </div>
</template>

<script>
    export default {
        props: ['model'],
        methods: {
            getModel: function(path) {
                if(path) {
                    if(perAdminView.pageView.page) {
                        return perHelperFindNodeFromPath(perAdminView.pageView.page, path)
                    }
                }
                return {}
            },
            onOk: function(e) {
                perHelperModelAction('saveEdit', { pagePath: perAdminView.pageView.path, path: perAdminView.state.editor.path } )
            }
        },
        data: function() {
            return {
            formOptions: {
              validateAfterLoad: true,
              validateAfterChanged: true
            }

          }
      },
      beforeMount: function() {
        if(!perAdminView.state.editor) this.$set(perAdminView.state, 'editor', { })
      }
    }
</script>
