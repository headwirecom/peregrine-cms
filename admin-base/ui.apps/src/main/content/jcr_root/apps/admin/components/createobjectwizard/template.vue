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
    <form-wizard v-bind:title="'create an object'" v-bind:subtitle="''" @on-complete="onComplete" color="#37474f">
        <tab-content title="select template" :before-change="leaveTabOne">
            <ul class="collection">
                <li class="collection-item"
                    v-for="item in objects"
                    v-on:click.stop.prevent="selectItem(null, item.path)"
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
                        path: $perAdminApp.getNodeFromView('/state/tools/objects'),
                        name: '',
                        objectPath: ''

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
                            label: "Object Name",
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
                const path = $perAdminApp.getNodeFromView('/state/tools/objects')
                const node = $perAdminApp.findNodeFromPath($perAdminApp.getView().admin.nodes, path)
                const objects = $perAdminApp.getNodeFromViewOrNull('/admin/objects/data')
                if(node.allowedObjects) {
                    let ret = []
                    for(let i = 0; i < objects.length; i++) {
                        if(node.allowedObjects.indexOf(objects[i].name) >= 0) {
                            ret.push(objects[i])
                        }
                        return ret
                    }
                }
                return objects
            }
        }
        ,
        methods: {
            selectItem: function(me, target){
                if(me === null) me = this
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
