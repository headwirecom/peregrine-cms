<!--
  #%L
  admin base - UI Apps
  %%
  Copyright (C) 2017 headwire inc.
  %%
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at
  
  http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
  #L%
  -->
<template>
<div class="container">
    <form-wizard 
      v-bind:title="'create a template'" 
      v-bind:subtitle="''" @on-complete="onComplete" 
      error-color="#d32f2f"
      color="#546e7a">
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
                            label: "Template Name",
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
