<template>
<div class="container">
    <form-wizard v-bind:title="'create a page'" v-bind:subtitle="''" @on-complete="onComplete">
        <tab-content title="select template" :before-change="leaveTabOne">
            <fieldset class="vue-form-generator">
                <div class="form-group required">
                    <label>Select Template</label>
                    <ul class="collection">
                        <li class="collection-item"
                            v-for="template in templates"
                            v-bind:class="isSelected(template.path) ? 'grey lighten-2' : ''">
                            <admin-components-action v-bind:model="{ command: 'selectTemplate', target: template.path }">{{template.name}}</admin-components-action>
                        </li>
                    </ul>
                    <div v-if="!formmodel.templatePath" class="errors">
                        <span track-by="index">selection required</span>
                    </div>
                </div>
            </fieldset>
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
                        templatePath: ''

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
            templates: function() {
                return $perAdminApp.getNodeFromViewOrNull('/admin/templates/data')
            }
        }
        ,
        methods: {
            selectTemplate: function(me, target){
                me.formmodel.templatePath = target
            },
            isSelected: function(target) {
                return this.formmodel.templatePath === target
            },
            onComplete: function() {
                $perAdminApp.stateAction('createPage', { parent: this.formmodel.path, name: this.formmodel.name, template: this.formmodel.templatePath })
            },
            leaveTabOne: function() {
                return ! ('' === ''+this.formmodel.templatePath)
            },
            leaveTabTwo: function() {
                return this.$refs.nameTab.validate()
            }

        }
    }
</script>
