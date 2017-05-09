<template>
    <div v-if="currentObject">
        <button class="btn-flat" v-on:click.stop.prevent="onView">view</button>
        <button class="btn-flat" v-on:click.stop.prevent="onEdit">edit</button>

        <div v-if="!edit">
            <pre>{{currentObject.data}}</pre>
        </div>
        <form v-else>

            <vue-form-generator
                    v-bind:schema  = "schema"
                    v-bind:model   = "currentObject.data"
                    v-bind:options = "formOptions">
            </vue-form-generator>
            <button class="btn-flat" v-on:click.stop.prevent="onOk">ok</button>
            <button class="btn-flat" v-on:click.stop.prevent="onCancel">cancel</button>
        </form>

    </div>
</template>

<script>
    export default {
        props: ['model'],
        computed: {
            currentObject: function () {
                return $perAdminApp.getNodeFromView("/state/tools/object")
            },
            schema: function () {
                let resourceType = this.currentObject.data['sling:resourceType']
                resourceType = resourceType.split('/').join('-')
                return $perAdminApp.getNodeFromView('/admin/componentDefinitions/' + resourceType)
            }
        }
        ,
        data: function() {
            return {
                formOptions: {
                    validateAfterLoad: true,
                    validateAfterChanged: true
                },
                edit: true

            }
        },
        methods: {
            onView: function() {
                this.edit = false
            },
            onEdit: function() {
                this.edit = true
            },
            onOk: function() {
                // should store the current node
                $perAdminApp.stateAction('saveObjectEdit', { data: this.currentObject.data, path: this.currentObject.show } )
            },
            onCancel: function() {
                $perAdminApp.stateAction('selectObject', { selected: this.currentObject.show } )
            }
        }
    }
</script>
