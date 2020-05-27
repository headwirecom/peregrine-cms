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
      v-bind:title="'create a website'"
      v-bind:subtitle="''"
      @on-complete="onComplete"
      error-color="#d32f2f"
      color="#546e7a"
      :key="reloadKey">
        <tab-content title="select theme" :before-change="leaveTabOne">
            <fieldset class="vue-form-generator">
                <div class="form-group required">
                    <label>Select Theme</label>
                    <ul class="collection">
                        <li class="collection-item"
                            v-for="item in themes"
                            v-bind:key="item.name"
                            v-on:click.stop.prevent="selectTheme(null, item.name)"
                            v-bind:class="isSelected(item.name) ? 'active' : ''">
                            <admin-components-action v-bind:model="{ command: 'selectTheme', target: item.name, title: item.name }"></admin-components-action>
                        </li>
                    </ul>
                    <div v-if="formErrors.unselectedThemeError" class="errors">
                        <span track-by="index">Selection required</span>
                    </div>
                </div>
            </fieldset>
            <p>
                This wizard allows you to create a website from an existing theme. If you'd like to create a more complex
                website please use the commandline tool `percli create project &lt;name&gt;` to create a website managed as a
                full project.
            </p>
        </tab-content>
        <tab-content v-if="colorPalettes && colorPalettes.length > 0" title="choose color palette">
            <admin-components-colorpaletteselector
                :palettes="colorPalettes"
                :template-path="formmodel.templatePath"
                @select="onColorPaletteSelect"/>
        </tab-content>
        <tab-content title="choose name" :before-change="leaveTabTwo">
            <vue-form-generator
              :model   ="formmodel"
              :schema  ="nameSchema"
              :options ="formOptions"
              ref      ="nameTab">
            </vue-form-generator>
        </tab-content>
        <tab-content title="verify">
            Creating Site `{{formmodel.name}}` from existing theme `{{formmodel.templatePath}}`
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
                    reloadKey: 0,
                    colorPalettes: [],
                    formErrors: {
                        unselectedThemeError: false
                    },
                    formmodel: {
                        path: $perAdminApp.getNodeFromView('/state/tools/pages'),
                        name: '',
                        title: '',
                        tenantUserPwd: '',
                        templatePath: ''
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
                                label: "Site Title",
                                model: "title",
                                required: true,
                                onChanged: (model, newVal, oldVal, field) => {
                                  if(!this.nameChanged) {
                                      this.formmodel.name = $perAdminApp.normalizeString(newVal, '_');
                                  }
                                }
                            },
                            {
                                type: "input",
                                inputType: "text",
                                label: "Site Name",
                                model: "name",
                                required: true,
                                onChanged: (model, newVal, oldVal, field) => {
                                    this.nameChanged = true;
                                },
                                validator: [this.nameAvailable, this.validSiteName]
                            },
                            {
                                type: "input",
                                inputType: "text",
                                label: "Tenant User Password",
                                model: "tenantUserPwd",
                                required: false,
                                onChanged: (model, newVal, oldVal, field) => {
                                }
                            }
                        ]
                    }
                }

        },
        created: function() {
            //By default select the first item in the list;
            this.selectTheme(this, this.themes[0].name);
        },
        computed: {
            pageSchema: function() {
            },
            themes: function() {
                const themes = $perAdminApp.findNodeFromPath($perAdminApp.getView().admin.nodes, '/content').children
                const siteRootParts = this.formmodel.path.split('/').slice(0,4)
                return themes.filter( (item) => {
                    return item.name.startsWith('theme');
                })
            }
        },
        methods: {
            selectTheme: function(me, target){
                if(me === null) me = this;
                me.formmodel.templatePath = target;
                me.formmodel.colorPalette = null
                me.colorPalettes = []
                this.validateTabOne(me);
                $perAdminApp.getApi().getPalettes(me.formmodel.templatePath).then((data) =>{
                    if (data && data.children && data.children.length > 0) {
                        me.colorPalettes = data.children.reverse()
                    }
                    me.reloadKey++
                })
            },
            isSelected: function(target) {
                return this.formmodel.templatePath === target
            },
            onComplete: function() {
                const payload = {
                    fromName: this.formmodel.templatePath,
                    toName: this.formmodel.name,
                    title: this.formmodel.title,
                    tenantUserPwd: this.formmodel.tenantUserPwd
                }

                if (this.formmodel.colorPalette && this.formmodel.colorPalette.length > 0) {
                    payload.colorPalette = this.formmodel.colorPalette
                }
                $perAdminApp.stateAction('createTenant', payload)
            },
            validateTabOne: function(me) {
                me.formErrors.unselectedThemeError = ('' === '' + me.formmodel.templatePath);

                return !me.formErrors.unselectedThemeError;
            },
            leaveTabOne: function() {
                if('' !== ''+this.formmodel.templatePath) {
//                    $perAdminApp.getApi().populateComponentDefinitionFromNode(this.formmodel.templatePath)
                }

                return this.validateTabOne(this);
            },
            nameAvailable(value) {
                if(!value || value.length === 0) {
                    return ['name is required']
                } else {
                    const folder = $perAdminApp.findNodeFromPath($perAdminApp.getView().admin.nodes, '/content')
                    if(folder && folder.children) {
                        for(let i = 0; i < folder.children.length; i++) {
                            if(folder.children[i].name === value) {
                                return ['name aready in use']
                            }
                        }
                    }
                    return []
                }
            },
            validSiteName(value) {
                if(!value || value.length === 0) {
                    return ['name is required']
                }
                if(value.match(/[^0-9a-z_]/)) {
                    return ['site names may only contain lowercase letters, numbers, and underscores']
                }
                return [];
            },
            leaveTabTwo: function() {
                return this.$refs.nameTab.validate()
            },
            onColorPaletteSelect(colorPalette) {
                this.formmodel.colorPalette = colorPalette
            }
        }
    }
</script>

<style scoped>
    .feature-unavailable {
        display: flex;
        justify-content: center;
    }

    .feature-unavailable .card{
        max-width: 700px;
    }
</style>