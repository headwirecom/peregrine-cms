<template>
    <div class="editor-panel blue-grey lighten-5">
        <span class="panel-title">Editor</span>
        <div v-if="!hasSchema">this component does not have a dialog defined</div>
        <vue-form-generator
            v-bind:schema  = "schema"
            v-bind:model   = "dataModel"
            v-bind:options = "formOptions">
        </vue-form-generator>
        <div>
            <button v-if="hasSchema" class="btn-flat" v-on:click.stop.prevent="onOk">ok</button>
            <button class="btn-flat" v-on:click.stop.prevent="onCancel">cancel</button>
            <button class="btn-flat" v-on:click.stop.prevent="onDelete">delete</button>
        </div>
    </div>
</template>

<script>
    export default {
        props: ['model'],
        computed: {
            schema: function() {
                var view = $perAdminApp.getView()
                var component = view.state.editor.component
                var schema = view.admin.componentDefinitions[component]
                return schema
            },
            dataModel: function() {
                var view = $perAdminApp.getView()
                var path = view.state.editor.path
                var model = $perAdminApp.findNodeFromPath(view.pageView.page, path)
                return model
            },
            hasSchema: function() {
                if(this.schema) return true
                return false
            }


        },
        methods: {
            onOk: function(e) {
                var view = $perAdminApp.getView()
                $perAdminApp.stateAction('savePageEdit', { pagePath: view.pageView.path, path: view.state.editor.path } )
            },
            onCancel: function(e) {
                var view = $perAdminApp.getView()
                $perAdminApp.stateAction('cancelPageEdit', { pagePath: view.pageView.path, path: view.state.editor.path } )
            },
            onDelete: function(e) {
                var view = $perAdminApp.getView()
                $perAdminApp.stateAction('deletePageNode', { pagePath: view.pageView.path, path: view.state.editor.path } )
            }

        },
        data: function() {
            return {
            formOptions: {
              validateAfterLoad: true,
              validateAfterChanged: true
            }

          }
      }
//      ,
//      beforeMount: function() {
//        if(!perAdminView.state.editor) this.$set(perAdminView.state, 'editor', { })
//      }
    }
</script>
