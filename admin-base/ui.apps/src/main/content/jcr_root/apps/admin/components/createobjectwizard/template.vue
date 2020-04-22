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
        <tab-content title="select object type" :before-change="leaveTabOne">
            <ul class="collection">
                <li class="collection-item"
                    v-for="item in objects"
                    v-bind:key="item.path"
                    v-on:click.stop.prevent="selectItem(null, item.path)"
                    v-bind:class="isSelected(item.path) ? 'grey lighten-2' : ''">
                    <admin-components-action v-bind:model="{ command: 'selectItem', target: item.path, title: item.name }"></admin-components-action>
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
        <tab-content title="values">
            <div>Provide the values for this object</div>
            <vue-form-generator :model="formmodel"
                                :schema="objectSchema"
                                :options="formOptions"
                                ref="verifyTab">

            </vue-form-generator>
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
                        path: $perAdminApp.getNodeFromView(this.model.dataFrom),
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
                            validator: [this.nameAvailable, this.validObjectName]
                        }
                        ]
                    }
                }

        }
        ,
        computed: {
            objectSchema: function() {
                if(this.formmodel.objectPath !== '') {
                    const path = this.formmodel.objectPath.split('/')
                    const componentName = path.slice(2).join('-')
                    const definitions = $perAdminApp.getNodeFromView('/admin/componentDefinitions')
                    console.log(componentName, definitions)
                    if(definitions &&  definitions[componentName]) {
                        return definitions[componentName].model
                    }
                }
            },
            objects: function() {
                const path = $perAdminApp.getNodeFromView(this.model.dataFrom)
                const node = $perAdminApp.findNodeFromPath($perAdminApp.getView().admin.nodes, path)
                const objects = $perAdminApp.getNodeFromViewOrNull('/admin/objects/data')
                const allowedObjects = this.findAllowedObjects(path)
                if(allowedObjects) {
                    let ret = []
                    for(let i = 0; i < objects.length; i++) {
                        if(allowedObjects.indexOf(objects[i].name) >= 0) {
                            ret.push(objects[i])
                        }
                    }
                    return ret
                }
                const tenant = $perAdminApp.getView().state.tenant;
                return objects.filter( object => { 
                    return object.path.startsWith('/apps/admin/') || (tenant && object.path.startsWith(`/apps/${tenant.name}/`))
                })
            }
        },
        created: function() {
            //By default select the first item in the list;
            if(this.objects.length > 0) {
                this.selectItem(null, this.objects[0].path)
            }
        },
        methods: {
            findAllowedObjects(path) {

                const pathSegments = path.split('/')
                while(pathSegments.length > 1) {
                    const node = $perAdminApp.findNodeFromPath($perAdminApp.getView().admin.nodes, pathSegments.join('/'))
                    if(node.allowedObjects) {
                        return node.allowedObjects
                    }
                    pathSegments.pop()
                }
                return undefined
            },
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
                $perAdminApp.stateAction('createObject', { parent: this.formmodel.path, name: this.formmodel.name, template: objectPath, data: this.formmodel, returnTo: this.model.returnTo })
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
            },
            validObjectName(value) {
                if(!value || value.length === 0) {
                    return ['name is required']
                }
                if(value.match(/[^0-9a-zA-Z_-]/)) {
                    return ['object names may only contain letters, numbers, underscores, and dashes']
                }
                return [];
            },
            leaveTabOne: function() {
                if('' !== ''+this.formmodel.objectPath) {
                    $perAdminApp.getApi().populateComponentDefinitionFromNode(this.formmodel.objectPath)
                }
                return ! ('' === ''+this.formmodel.objectPath)
            },
            leaveTabTwo: function() {
                return this.$refs.nameTab.validate()
            }

        }
    }
</script>
