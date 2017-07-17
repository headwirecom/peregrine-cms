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
    <div class="editor-panel" ref="editorPanel">
      <div class="editor-panel-content">
        <span class="panel-title">Editor</span>
        <div v-if="!hasSchema">this component does not have a dialog defined</div>
        <vue-form-generator
            v-bind:schema  = "schema"
            v-bind:model   = "dataModel"
            v-bind:options = "formOptions">
        </vue-form-generator>
        <div class="right-align">
            <button class="waves-effect waves-light btn btn-raised" v-on:click.stop.prevent="onDelete">
              <i class="material-icons">delete</button>
            <button class="waves-effect waves-light btn btn-raised" v-on:click.stop.prevent="onCancel">
              <i class="material-icons">close</button>
            <button v-if="hasSchema" class="waves-effect waves-light btn btn-raised" v-on:click.stop.prevent="onOk">
              <i class="material-icons">check</i></button>
        </div>
      <div/>
    </div>
</template>

<script>
    export default {
      props: ['model'],
      mounted(){
        /* is this a touch device */
        this.isTouch = 'ontouchstart' in window || navigator.maxTouchPoints
        if(!this.isTouch){
          this.$nextTick(function() {
             this.focusFirstField()
          })
        }
      },
      updated(){
        if(!this.isTouch){
          this.$nextTick(function() {
            this.focusFirstField()
          })
        }
      },
      data() {
        return {
          isTouch: false
        }
      },
      computed: {
        schema: function() {
            var view = $perAdminApp.getView()
            var component = view.state.editor.component
            var schema = view.admin.componentDefinitions[component]
            return schema
        },
        dataModel: function() {
            var view = $perAdminApp.getView()
            var path = view.state.editor.path
            var model = $perAdminApp.findNodeFromPath(view.pageView.page, path)
            return model
        },
        hasSchema: function() {
            if(this.schema) return true
            return false
        }
      },
      methods: {
        focusFirstField(){
          var field = document.querySelector('.vue-form-generator > fieldset > .form-group')
          if(field.classList.contains('field-texteditor')){
            var input = document.querySelector('.vue-form-generator .ql-editor')
            if(input){
              input.focus()
            }
          } else {
            var input = document.querySelector('.vue-form-generator input:first-of-type ')
            if(input){
              input.focus()
            }
          }
        },
        onOk(e) {
            var view = $perAdminApp.getView()
            $perAdminApp.action(this, 'onEditorExitFullscreen')
            $perAdminApp.stateAction('savePageEdit', { pagePath: view.pageView.path, path: view.state.editor.path } )
        },
        onCancel(e) {
            var view = $perAdminApp.getView()
            $perAdminApp.action(this, 'onEditorExitFullscreen')
            $perAdminApp.stateAction('cancelPageEdit', { pagePath: view.pageView.path, path: view.state.editor.path } )
        },
        onDelete(e) {
            var view = $perAdminApp.getView()
            $perAdminApp.action(this, 'onEditorExitFullscreen')
            $perAdminApp.stateAction('deletePageNode', { pagePath: view.pageView.path, path: view.state.editor.path } )
        }
      },
      data: function() {
        return {
          formOptions: {
            validateAfterLoad: true,
            validateAfterChanged: true
          }
        }
      }
//      ,
//      beforeMount: function() {
//        if(!perAdminView.state.editor) this.$set(perAdminView.state, 'editor', { })
//      }
    }
</script>
