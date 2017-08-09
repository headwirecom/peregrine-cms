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
    <div>

    <div v-if="currentObject" :class="`object-editor ${isFullscreen ? 'fullscreen' : 'narrow'}`">
      <div class="object-editor-content">
        <button 
          v-if="isFullscreen"
          type="button" 
          class="toggle-fullscreen" 
          title="exit fullscreen"
          v-on:click.prevent="onPreviewExitFullscreen">
          <i class="material-icons">fullscreen_exit</i>
        </button>
        <button 
          v-if="!isFullscreen"
          type="button" 
          class="toggle-fullscreen" 
          title="enter fullscreen"
          v-on:click.prevent="onPreviewFullscreen">
          <i class="material-icons">fullscreen</i>
        </button>

        <span class="panel-title">Object</span>
        <div v-if="(edit === false || edit === undefined) && schema !== undefined" class="display-json">
            <div class="row" v-for="field in schema.fields">
                <template v-if="!field.fields">
                    <div class="col s4"><b>{{field.label}}:</b></div><div class="col s8">{{currentObject.data[field.model]}}</div>
                </template>
                <template v-else>
                    <div class="col s12"><b>{{field.title}}</b></div>
                    <div class="row" v-for="item in currentObject.data[field.model]">
                        <div v-for="child in field.fields">
                            <div class="col s4"><b>{{child.label}}:</b></div><div class="col s8">{{item[child.model]}}</div>
                        </div>
                    </div>
                </template>
            </div>
        </div>

        <form v-if="edit && currentObject.data && schema">
            <vue-form-generator
              v-on:validated = "onValidated"
              v-bind:schema  = "schema"
              v-bind:model   = "currentObject.data"
              v-bind:options = "formOptions">
            </vue-form-generator>
        </form>
        <div class="right-align">
          <button v-if="!edit" title="edit" class="btn btn-raised" v-on:click.stop.prevent="onEdit">
            <i class="material-icons">edit</i>
          </button>
          <button v-if="edit" title="save" v-bind:disabled="!valid" class="btn btn-raised" v-on:click.stop.prevent="onOk">
            <i class="material-icons">check</i>
          </button>
          <button v-if="edit" title="cancel" class="btn btn-raised" v-on:click.stop.prevent="onCancel">
            <i class="material-icons">close</i>
          </button>
        </div>
        </div>
    </div>
        <div v-if="currentObject === undefined" class="asset-preview">
            <div class="no-asset-selected">
              <span>no object selected</span>
              <i class="material-icons">info</i>
            </div>
        </div>
    </div>

</template>

<script>
    export default {
        props: ['model'],
        computed: {
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
            formOptions: {
              validateAfterLoad: true,
              validateAfterChanged: true,
              focusFirstField: true
            },
            isFullscreen: false,
              valid: false
          }
        },
        methods: {
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
          },
          onPreviewExitFullscreen(){
            this.isFullscreen = false
          },
          onPreviewFullscreen(){
            this.isFullscreen = true
          }
        }
    }
</script>
