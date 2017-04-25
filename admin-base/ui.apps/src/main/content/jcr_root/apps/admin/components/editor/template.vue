<template>
    <div>
        <p>editor</p>
        <form>
        <vue-form-generator
            v-if="this.$root.$data.state.editor"
            :schema="this.$root.$data.state.editor.dialog"
            :model="getModel(this.$root.$data.state.editor.path)"
            :options="formOptions">
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
                perHelperModelAction('saveEdit', perAdminView.state.editor.path)
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