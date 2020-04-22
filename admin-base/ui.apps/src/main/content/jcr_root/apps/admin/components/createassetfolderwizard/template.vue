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
    <form-wizard v-bind:title="'create a folder'" 
                 v-bind:subtitle="''" 
                 @on-complete="onComplete" 
                 color="#37474f">
        <tab-content title="choose folder name" 
                     :before-change="leaveTabOne">
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
                let formModelAssets = $perAdminApp.getNodeFromView('/state/tools/assets')
                return {
                    formmodel: {
                        path: formModelAssets,
                        name: ''
                    },
                    formOptions: {
                        validationErrorClass: "has-error",
                        validationSuccessClass: "has-success",
                        validateAfterChanged: true,
                        focusFirstField: true
                    },
                    nameSchema: {
                        fields: [{
                            type: "input",
                            inputType: "text",
                            label: "Folder Name",
                            placeholder: "Folder Name",
                            model: "name",
                            required: true,
                            validator: [this.nameAvailable, this.validFolderName]
                        }]
                    }
                }

        },
        computed: {
            
        },
        methods: {
            onComplete: function() {
                let payload = { parent: this.formmodel.path, name: this.formmodel.name }
                $perAdminApp.stateAction('createAssetFolder', payload)
            },
            nameAvailable(value) {
                if(!value || value.length === 0) {
                    return ['name is required']
                } else {
                    const folder = $perAdminApp.findNodeFromPath($perAdminApp.getView().admin.nodes, this.formmodel.path)
                    for(let i = 0; i < folder.children.length; i++) {
                        if(folder.children[i].name === value) {
                            return ['name aready in use']
                        }
                    }
                    return []
                }
                return true
            },
            validFolderName(value) {
                if(!value || value.length === 0) {
                    return ['name is required']
                }
                if(value.match(/[^0-9a-zA-Z_-]/)) {
                    return ['folder names may only contain letters, numbers, underscores, and dashes']
                }
                return [];
            },
            leaveTabOne: function() {
                return this.$refs.nameTab.validate()
            }

        }
    }
</script>
