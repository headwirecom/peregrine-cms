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
        <tab-content title="select component" :before-change="leaveTabOne">
            <fieldset class="vue-form-generator">
                <div class="form-group required">
                    <label>Select Component</label>
                    <ul class="collection">
                        <li class="collection-item"
                            v-for="component in components"
                            v-on:click.stop.prevent="selectComponent(null, component.path)"
                            v-bind:class="isSelected(component.path) ? 'active' : ''">
                            <admin-components-action v-bind:model="{ command: 'selectComponent', target: component.path, title: component.name }"></admin-components-action>
                        </li>
                    </ul>
                    <div v-if="formErrors.unselectedComponentError" class="errors">
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
                const path = $perAdminApp.getNodeFromView('/state/tools/templates')
                const siteName = path.split('/')[3]
                return {
                    formErrors: {
                        unselectedComponentError: false
                    },
                    formmodel: {
                        path: path,
                        name: '',
                        title: '',
                        component: null
                    },
                    formOptions: {
                        validationErrorClass: "has-error",
                        validationSuccessClass: "has-success",
                        validateAfterChanged: true,
                        focusFirstField: true
                    },
                    nameChanged: false,
                    nameSchema: {
                        fields: [
                            {
                                type: "input",
                                inputType: "text",
                                label: "Template Title",
                                model: "title",
                                required: true,
                                onChanged: (model, newVal, oldVal, field) => {
                                    if(!this.nameChanged) {
                                        this.formmodel.name = $perAdminApp.normalizeString(newVal);
                                    }
                                }
                            },
                            {
                            type: "input",
                            inputType: "text",
                            label: "Template Name",
                            model: "name",
                            required: true,
                            onChanged: (model, newVal, oldVal, field) => {
                                this.nameChanged = true;
                            },
                            validator: [this.nameAvailable, this.validTemplateName]
                        }
                        ]
                    }
                }

        },
        created: function() {
            //By default select the first item in the list;
            this.selectComponent(this, this.components[0].path);
        },
        computed: {
            components: function() {
                const tenant = $perAdminApp.getView().state.tenant || {name: 'example'}
                const components = $perAdminApp.getNodeFromViewOrNull('/admin/components/data')
                const siteRootParts = this.formmodel.path.split('/').slice(0,4)
                siteRootParts[1] = 'apps'
                siteRootParts[2] = tenant.name
                const siteRoot = siteRootParts.slice(0,3).join('/')
                return components.filter( (component) =>
                    component.path.startsWith(siteRoot)
                    && (component.name === 'page' || component.templateComponent)
                )
            }
        }
        ,
        methods: {
            selectComponent: function(me, target){
                if(me === null) me = this
                me.formmodel.component = target;

                this.validateTabOne(me);
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
            validTemplateName(value) {
                if(!value || value.length === 0) {
                    return ['name is required']
                }
                if(value.match(/[^0-9a-zA-Z_-]/)) {
                    return ['template names may only contain letters, numbers, underscores, and dashes']
                }
                return [];
            },
            isSelected: function(target) {
                return this.formmodel.component === target
            },
            onComplete: function() {
                const path = this.formmodel.path
                const siteName = path.split('/')[3]
                const component = this.formmodel.component.substring(this.formmodel.component.indexOf('/',1)+1)
                $perAdminApp.stateAction('createTemplate', { parent: this.formmodel.path, name: this.formmodel.name, component: component, title: this.formmodel.title })
            },
            validateTabOne: function(me) {
                me.formErrors.unselectedComponentError = (!me.formmodel.component);

                return !me.formErrors.unselectedComponentError;
            },
            leaveTabOne: function() {
                return this.validateTabOne(this);
            },
            leaveTabTwo: function() {
                return this.$refs.nameTab.validate()
            }


        }
    }
</script>
