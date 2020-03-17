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
      v-bind:title="'create a page'" 
      v-bind:subtitle="''" @on-complete="onComplete"
      error-color="#d32f2f"
      color="#546e7a">
        <tab-content title="select template" :before-change="leaveTabOne">
            <fieldset class="vue-form-generator">
                <div class="form-group required">
                    <div class="row">
                        <div class="col s6">
                            <label>Select Template</label>
                            <ul class="collection">
                                <li class="collection-item"
                                    v-for="template in templates"
                                    v-on:click.stop.prevent="selectTemplate(null, template.path)"
                                    v-bind:class="isSelected(template.path) ? 'active' : ''"
                                    v-bind:key="template.path">
                                    <admin-components-action v-bind:model="{ command: 'selectTemplate', target: template.path, title: template.name }"></admin-components-action>
                                </li>
                            </ul>
                            <div v-if="skeletonPages && skeletonPages.length > 0">
                                OR<br/>
                                <label>Select Skeleton-Page</label>
                                <ul class="collection">
                                    <li class="collection-item"
                                        v-for="skeletonPage in skeletonPages"
                                        v-on:click.stop.prevent="selectSkeletonPage(null, skeletonPage.path)"
                                        v-bind:class="isSelected(skeletonPage.path) ? 'active' : ''"
                                        v-bind:key="skeletonPage.path">
                                        <admin-components-action v-bind:model="{ command: 'selectSkeletonPage', target: skeletonPage.path, title: skeletonPage.name }"></admin-components-action>
                                    </li>
                                </ul>
                            </div>
                            <div v-if="formErrors.unselectedTemplateError" class="errors">
                                <span track-by="index">selection required</span>
                            </div>
                        </div>
                        <div class="col s6">
                            <label>Preview</label>
                            <div class="iframe-container">
                                <iframe v-if="formmodel.skeletonPagePath"
                                        v-bind:src="formmodel.skeletonPagePath + '.html'" data-per-mode="preview">
                                </iframe>
                                <iframe v-if="formmodel.templatePath"
                                        v-bind:src="formmodel.templatePath + '.html'" data-per-mode="preview">
                                </iframe>
                            </div>
                        </div>
                    </div>
                </div>
            </fieldset>
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
            Creating Page `{{formmodel.name}}` from
            <span v-if="formmodel.templatePath">template `{{formmodel.templatePath}}`</span>
            <span v-else-if="formmodel.skeletonPagePath">skeleton page `{{formmodel.skeletonPagePath}}`</span>
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
                    formErrors: {
                        unselectedTemplateError: false
                    },
                    formmodel: {
                        path: $perAdminApp.getNodeFromView('/state/tools/pages'),
                        name: '',
                        title: '',
                        templatePath: '',
                        skeletonPagePath: ''
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
                              label: "Page Title",
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
                            label: "Page Name",
                            model: "name",
                            required: true,
                            onChanged: (model, newVal, oldVal, field) => {
                                this.nameChanged = true;
                            },
                            validator: [this.nameAvailable, this.validPageName]
                        }
                      ]
                    }
                }

        }
        ,
        created: function() {
            //By default select the first item in the list;
            this.selectTemplate(this, this.templates[0].path);
        },
        computed: {
            pageSchema: function() {
                if(this.formmodel.templatePath !== '') {
                    const definitions = $perAdminApp.getNodeFromView('/admin/componentDefinitions')
                    if(definitions) {
                        // todo: component should be resolved through the template
                        const comp = 'pagerender-vue-structure-page'
                        const def = $perAdminApp.getNodeFromView('/admin/componentDefinitions')[comp]
                        return def
                    }
                }
            },
            templates: function() {
                const templates = $perAdminApp.getNodeFromViewOrNull('/admin/templates/data')
                const siteRootParts = this.formmodel.path.split('/').slice(0,4)
                siteRootParts[2] = 'templates'
                const siteRoot = siteRootParts.join('/')
                return templates.filter( (item) => item.path.startsWith(siteRoot))
            },
            skeletonPages: function() {
                const siteRoot = this.formmodel.path.split('/').slice(0,4).join('/') + '/skeleton-pages'
                const skeletonPageRoot = $perAdminApp.findNodeFromPath(this.$root.$data.admin.nodes, siteRoot)
                if(skeletonPageRoot) {
                    return skeletonPageRoot.children
                }
                return []
            }
        }
        ,
        methods: {
            selectTemplate: function(me, target){
                if(me === null) me = this
                me.formmodel.templatePath = target
                me.formmodel.skeletonPagePath = ''
                this.validateTabOne(me);
            },
            selectSkeletonPage: function(me, target){
                if(me === null) me = this
                me.formmodel.skeletonPagePath = target
                me.formmodel.templatePath = ''
                this.validateTabOne(me);
            },
            isSelected: function(target) {
                return this.formmodel.templatePath === target || this.formmodel.skeletonPagePath === target
            },
            onComplete: function() {
                if(this.formmodel.templatePath) {
                    $perAdminApp.stateAction('createPage', { parent: this.formmodel.path, name: this.formmodel.name, template: this.formmodel.templatePath, data: this.formmodel })
                }
                else {
                    $perAdminApp.stateAction('createPageFromSkeletonPage', { parent: this.formmodel.path, name: this.formmodel.name, skeletonPagePath: this.formmodel.skeletonPagePath, data: this.formmodel })
                }
            },
            validateTabOne: function(me) {
                me.formErrors.unselectedTemplateError = ('' === '' + me.formmodel.templatePath && '' === '' + me.formmodel.skeletonPagePath);

                return !me.formErrors.unselectedTemplateError;
            },
            leaveTabOne: function() {
                if('' !== ''+this.formmodel.templatePath) {
                    $perAdminApp.getApi().populateComponentDefinitionFromNode(this.formmodel.templatePath)
                }
                if('' !== ''+this.formmodel.skeletonPagePath) {
                    const skeletonPage = this.skeletonPages.find(bp => {
                        return bp.path == this.formmodel.skeletonPagePath
                    })
                    $perAdminApp.getApi().populateComponentDefinitionFromNode(skeletonPage.templatePath)
                }

                return this.validateTabOne(this);
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
            validPageName(value) {
                if(!value || value.length === 0) {
                    return ['name is required']
                }
                if(value.match(/[^0-9a-zA-Z_-]/)) {
                    return ['page names may only contain letters, numbers, underscores, and dashes']
                }
                return [];
            },
            leaveTabTwo: function() {
                return this.$refs.nameTab.validate()
            }
        }
    }
</script>

<style>
.iframe-container {
    overflow: hidden;
    padding-top: 56.25%;
    position: relative;
}

.iframe-container iframe {
    border: 0;
    height: 200%;
    left: 0;
    position: absolute;
    top: 0;
    width: 200%;
    transform: scale(0.5) translate(-50%, -50%);
}


</style>