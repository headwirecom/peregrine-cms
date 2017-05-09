<template>
<div class="container">
    <form-wizard v-bind:title="'create a page'" v-bind:subtitle="''" @on-complete="onComplete">
        <tab-content title="select template">
            <ul>
                <li v-bind:class="isSelected('/content/templates/example') ? 'active' : ''"><admin-components-action v-bind:model="{ command: 'selectTemplate', target: '/content/templates/example' }">example</admin-components-action></li>
            </ul>
        </tab-content>
        <tab-content title="choose name">
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
                            required: true
//                    ,
//                    validator: VueFormGenerator.validators.string
                        }
                        ]
                    }
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
            }
        }
    }
</script>

<style>
    .active {
        font-weight: bold;
    }
</style>