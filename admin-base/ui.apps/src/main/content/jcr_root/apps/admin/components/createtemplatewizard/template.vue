<template>
<div class="container">
    <form-wizard v-bind:title="'create a page'" v-bind:subtitle="''" @on-complete="onComplete">
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
                        path: $perAdminApp.getNodeFromView('/state/tools/templates'),
                        name: ''
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
        methods: {
            onComplete: function() {
                $perAdminApp.stateAction('createTemplate', { parent: this.formmodel.path, name: this.formmodel.name })
            }

        }
    }
</script>
