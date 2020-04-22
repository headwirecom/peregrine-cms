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
    <div :class="`peregrine-workspace ${state.rightPanelVisible ? 'right-panel-visible' : ''}`">
        <component
          v-bind:is    = "getChildByPath('contentview').component"
          v-bind:model = "getChildByPath('contentview')">
        </component>

        <admin-components-action v-if="!state.rightPanelVisible" v-bind:model="{
            classes: 'show-right-panel',
            target: 'rightPanelVisible',
            command: 'showHide',
            tooltipTitle: $i18n('showComponentsPanel')
            }"><i class="material-icons">keyboard_arrow_left</i>
        </admin-components-action>

        <aside v-bind:class="`explorer-preview right-panel ${isFullscreen ? 'fullscreen' : 'narrow'}`">
            <admin-components-action v-if="!state.editorVisible" v-bind:model="{
                classes: 'hide-right-panel',
                target: 'rightPanelVisible',
                command: 'showHide',
                tooltipTitle: $i18n('hideComponentsPanel')
            }">
                <i class="material-icons">highlight_off</i>
            </admin-components-action>
            
            <button 
              v-if="state.editorVisible && isFullscreen"
              type="button" 
              class="toggle-fullscreen" 
              title="exit fullscreen"
              v-on:click.prevent="onEditorExitFullscreen">
              <i class="material-icons">fullscreen_exit</i>
            </button>
            <button 
              v-if="state.editorVisible && !isFullscreen"
              type="button" 
              class="toggle-fullscreen" 
              v-bind:title="$i18n('enterFullscreen')"
              v-on:click.prevent="onEditorFullscreen">
              <i class="material-icons">fullscreen</i>
            </button>

            <component
              v-if         = "state.editorVisible"
              v-bind:is    = "getChildByPath('editor').component"
              v-bind:model = "getChildByPath('editor')">
            </component>

            <component
              v-else-if    = "getChildByPath('right-panel')"
              v-bind:is    = "getChildByPath('right-panel').component"
              v-bind:model = "getChildByPath('right-panel')">
            </component>

            <component
                v-else
                v-bind:is    = "getChildByPath('components').component"
                v-bind:model = "getChildByPath('components')">
            </component>

        </aside>
    </div>
</template>

<script>
    export default {
        props: ['model'],
        beforeMount(){
          Vue.set($perAdminApp.getView().state, 'rightPanelFullscreen', false)
          Vue.set($perAdminApp.getView().state, 'rightPanelVisible', true)
        },
        computed: {
            state: function() {
                return $perAdminApp.getView().state
            },
            editorVisible: function() {
                return $perAdminApp.getNodeFromView('/state/editorVisible')
            },
            getRightPanelClasses: function() {
                // rightPanelVisible: true/false
                return `right-panel ${$perAdminApp.getView().state.rightPanelVisible ? 'visible' : ''}`
            },
            isFullscreen(){
              return $perAdminApp.getView().state.rightPanelFullscreen || false
            }
        },
        methods: {
            getChildByPath(childName) {
                var path = this.model.path+'/'+childName
                for(var i = 0; i < this.model.children.length; i++) {
                    if(this.model.children[i].path === path) {
                        var ret = this.model.children[i]
                        ret.classes = 'col fullheight s4'
                        return ret
                    }
                }
                return null
            },

            showHide(me, name) {
                $perAdminApp.getView().state.rightPanelVisible = $perAdminApp.getView().state.rightPanelVisible ? false: true
            },

            showComponentEdit(me, target) {
              // only trigger state action if another component is selected
              if($perAdminApp.getNodeFromView('/state/editor/path') !== target) {
                $perAdminApp.stateAction('editComponent', target)
              }
            },

            onEditorExitFullscreen(){
              $perAdminApp.getView().state.rightPanelFullscreen = false
            },

            onEditorFullscreen(){
              $perAdminApp.getView().state.rightPanelFullscreen = true
            }
        }
    }
</script>
