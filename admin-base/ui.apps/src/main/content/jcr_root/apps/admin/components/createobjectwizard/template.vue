<template>
<div class="container">
    <form-wizard v-bind:title="'create an object'" v-bind:subtitle="''" @on-complete="onComplete">
        <tab-content title="select template" :before-change="leaveTabOne">
            <ul class="collection">
                <li class="collection-item"
                    v-for="item in objects"
                    v-bind:class="isSelected(item.path) ? 'grey lighten-2' : ''">
                    <admin-components-action v-bind:model="{ command: 'selectItem', target: item.path }">{{item.name}}</admin-components-action>
                </li>
            </ul>
            <div v-if="!formmodel.templatePath">please select an object</div>
        </tab-content>
        <tab-content title="choose name" :before-change="leaveTabTwo">
            <vue-form-generator :model="formmodel"
                                :schema="nameSchema"
                                :options="formOptions"
                                ref="nameTab">

            </vue-form-generator>
        </tab-content>
        <tab-content title="verify">
            <pre v-html="JSON.stringify(formmodel, true, 2)"></pre>
        </tab-content>
    </form-wizard>
</div>
</template>

<script>
    export default {
        props: ['model'],
        data:
            function() {
                return {
                    formmodel: {
                        path: $perAdminApp.getNodeFromView('/state/tools/pages'),
                        name: '',
                        objectPath: ''

                    },
                    formOptions: {
                        validationErrorClass: "has-error",
                        validationSuccessClass: "has-success",
                        validateAfterChanged: true
                    },
                    nameSchema: {
                        fields: [{
                            type: "input",
                            inputType: "text",
                            label: "Page Name",
                            model: "name",
                            required: true,
                            validator: VueFormGenerator.validators.string
                        }
                        ]
                    }
                }

        }
        ,
        computed: {
            objects: function() {
                return $perAdminApp.getNodeFromViewOrNull('/admin/objects/data')
            }
        }
        ,
        methods: {
            selectItem: function(me, target){
                me.formmodel.objectPath = target
            },
            isSelected: function(target) {
                return this.formmodel.objectPath === target
            },
            onComplete: function() {
                let objectPath = this.formmodel.objectPath
                objectPath = objectPath.split('/').slice(2).join('/')
                $perAdminApp.stateAction('createObject', { parent: this.formmodel.path, name: this.formmodel.name, template: objectPath })
            },
            leaveTabOne: function() {
                return ! ('' === ''+this.formmodel.objectPath)
            },
            leaveTabTwo: function() {
                return this.$refs.nameTab.validate()
            }

        }
    }
</script>
