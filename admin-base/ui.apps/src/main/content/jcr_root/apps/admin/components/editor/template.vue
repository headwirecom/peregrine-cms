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
        <div class="editor-panel-content" v-if="schema !== undefined && dataModel !== undefined">
            <span class="panel-title">Editor</span>
            <span v-if="title"> - {{title}}</span>
            <div v-if="!hasSchema">this component does not have a dialog defined</div>
            <vue-form-generator v-bind:schema="schema" v-bind:model="dataModel" v-bind:options="formOptions">
            </vue-form-generator>
        </div>
        <div class="editor-panel-buttons">
            <button class="waves-effect waves-light btn btn-raised" title="delete" v-on:click.stop.prevent="onDelete">
                <i class="material-icons">delete</i>
            </button>
            <button class="waves-effect waves-light btn btn-raised" title="cancel" v-on:click.stop.prevent="onCancel">
                <i class="material-icons">close</i>
            </button>
            <button v-if="hasSchema" class="waves-effect waves-light btn btn-raised" title="save" v-on:click.stop.prevent="onOk">
                <i class="material-icons">check</i>
            </button>
        </div>
    </div>
</template>

<script>
    export default {
      props: ['model'],
    updated: function() {
        let stateTools = $perAdminApp.getNodeFromView("/state/tools");
        stateTools._deleted = {};
    },
      mounted(){
        this.isTouch = 'ontouchstart' in window || navigator.maxTouchPoints
      },
      data() {
        return {
          isTouch: false,
          formOptions: {
            validateAfterLoad: true,
            validateAfterChanged: true,
            focusFirstField: true
          }
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
        },
          title: function() {
              var view = $perAdminApp.getView()
              var componentName = view.state.editor.component.split('-').join('/')
              const components = view.admin.components.data
              for(let i = 0; i < components.length; i++) {
                  const component = components[i]
                  if(component.path.endsWith(componentName)) {
                      return component.title
                  }
              }
          }
      },
      methods: {
        onOk(e) {
            let data = JSON.parse(JSON.stringify(this.dataModel));
            let _deleted = $perAdminApp.getNodeFromView("/state/tools/_deleted");

            //Merge _deleted child items back into the object that we need to save.
            //Loop through the model for this object/page/asset and find objects that have children
            for ( const key in data) {
                //If data[key] or deleted[key] is an array of objects
                if (( Array.isArray(data[key]) && data[key].length && typeof data[key][0] === 'object') || 
                    ( Array.isArray(_deleted[key]) && _deleted[key].length && typeof _deleted[key][0] === 'object') ) {

                    let node = data[key];

                    //Use an object to easily merge back in deleted nodes
                    let targetNode = {}
                    //Insert deleted children
                    for ( const j in _deleted[key]) {
                        const deleted = _deleted[key][j]
                        targetNode[deleted.name] = deleted;
                    }
                    //Insert children
                    for ( const i in node ) {
                        const child = node[i]
                        targetNode[child.name] = child;
                    }
                    data[key] = Object.values(targetNode);
                }
            }

            var view = $perAdminApp.getView()
            $perAdminApp.action(this, 'onEditorExitFullscreen')
            $perAdminApp.stateAction('savePageEdit', { data: data, path: view.state.editor.path } )
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
      }
//      ,
//      beforeMount: function() {
//        if(!perAdminView.state.editor) this.$set(perAdminView.state, 'editor', { })
//      }
    }
</script>
