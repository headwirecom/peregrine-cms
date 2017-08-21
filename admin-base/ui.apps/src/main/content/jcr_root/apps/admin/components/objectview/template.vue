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
    <div class="explorer-preview-content preview-object">
        <template v-if="currentObject">
            <ul class="explorer-preview-nav">
                <template v-if="edit">
                    <li>
                        <a  title="cancel object" 
                            class="waves-effect waves-light" 
                            v-on:click.stop.prevent="onCancel">
                            <i class="material-icons">close</i>
                        </a>
                    </li>
                    <li>
                        <a  title="save object" 
                            v-bind:disabled="!valid" 
                            class="waves-effect waves-light" 
                            v-on:click.stop.prevent="onOk">
                            <i class="material-icons">check</i>
                        </a>
                    </li>
                </template>
                <li v-else>
                    <a  title="edit object" 
                        class="waves-effect waves-light" 
                        v-on:click.stop.prevent="onEdit">
                        <i class="material-icons">edit</i>
                    </a>
                </li>
            </ul>
            <span class="panel-title">Object</span>
            <div v-if="(edit === false || edit === undefined) && schema !== undefined" class="display-json">

                <form v-if="currentObject.data && schema">
                    <vue-form-generator 
                            class="vfg-preview"
                            v-on:validated = "onValidated"
                            v-bind:schema  = "readOnlySchema"
                            v-bind:model   = "currentObject.data"
                            v-bind:options = "formOptions">
                    </vue-form-generator>
                </form>
    <!--
                <div class="row" v-for="field in schema.fields">
                    <template v-if="!field.fields">
                        <div class="col s4"><b>{{field.label}}:</b></div><div class="col s8">{{get(currentObject.data,field.model)}}</div>
                    </template>
                    <template v-else>
                        <div class="col s12"><b>{{field.title}}</b></div>
                        <div class="row" v-for="item in currentObject.data[field.model]">
                            <div v-for="child in field.fields">
                                <div class="col s4"><b>{{child.label}}:</b></div><div class="col s8">{{get(item,child.model)}}</div>
                            </div>
                        </div>
                    </template>
                </div>
    -->
            </div>

            <form v-if="edit && currentObject.data && schema">
                <vue-form-generator
                  v-on:validated = "onValidated"
                  v-bind:schema  = "schema"
                  v-bind:model   = "currentObject.data"
                  v-bind:options = "formOptions">
                </vue-form-generator>
            </form>
        </template>

        <div v-if="currentObject === undefined" class="explorer-preview-empty">
            <span>no object selected</span>
            <i class="material-icons">info</i>
        </div>
    </div>
</template>

<script>
    export default {
        props: ['model'],
        computed: {
            readOnlySchema() {
                if(!this.schema) return {}
                const roSchema = JSON.parse(JSON.stringify(this.schema))
                roSchema.fields.forEach( (field) => {
                    field.preview = true
                    if(field.fields) {
                        field.fields.forEach( (field) => {
                            field.preview = true
                        })
                    }
                })
                return roSchema

            },
          currentObject: function () {
            return $perAdminApp.getNodeFromView("/state/tools/object")
          }, 
          schema: function () {
            let resourceType = this.currentObject.data['component'] ? this.currentObject.data['component'] : this.currentObject.data['sling:resourceType']
            resourceType = resourceType.split('/').join('-')
            return $perAdminApp.getNodeFromView('/admin/componentDefinitions/' + resourceType)
          },
          edit() {
              return $perAdminApp.getNodeFromView('/state/tools').edit
          }
        },
        data: function() {
          return {
            valid: false,
            formOptions: {
              validateAfterLoad: true,
              validateAfterChanged: true,
              focusFirstField: true
            }
          }
        },
        methods: {
            get(obj, path) {
                const segments = path.split('.')
                if(segments.length == 1) {
                    return obj[segments[0]]
                } else {
                    if(obj[segments[0]]) {
                        return obj[segments[0]][segments[1]]
                    }
                }
                return '-'
            },
            onValidated(isValid, errors) {
                this.valid = isValid
            },
          onEdit: function() {
              Vue.set($perAdminApp.getNodeFromView('/state/tools'), 'edit', true)
          },
          onOk: function() {
            // should store the current node
            $perAdminApp.stateAction('saveObjectEdit', { data: this.currentObject.data, path: this.currentObject.show })
              $perAdminApp.getNodeFromView('/state/tools').edit = false
          },
          onCancel: function() {
            $perAdminApp.stateAction('selectObject', { selected: this.currentObject.show })
              $perAdminApp.getNodeFromView('/state/tools').edit = false
          }
        }
    }
</script>
