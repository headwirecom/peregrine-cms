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
  <div :class="[
      `peregrine-workspace`,
      {
        'right-panel-visible': state.rightPanelVisible,
        'editor-visible': state.editorVisible
      }
    ]">
    <component
        :is="getChildByPath('contentview').component"
        :model="getChildByPath('contentview')">
    </component>

    <admin-components-action
        :class="['right-panel-toggle', {'hide-right-panel': state.rightPanelVisible, 'show-right-panel': !state.rightPanelVisible}]"
        :model="{
                target: 'rightPanelVisible',
                command: 'showHide',
                tooltipTitle: state.rightPanelVisible? $i18n('hideComponentsPanel') : $i18n('showComponentsPanel')
            }">
      <i class="material-icons" v-if="state.rightPanelVisible">keyboard_arrow_right</i>
      <i class="material-icons" v-else>keyboard_arrow_left</i>
    </admin-components-action>

    <aside :class="[
        `explorer-preview`,
        `right-panel`,
        {
          'fullscreen': rightPanelFullscreen,
          'narrow': !rightPanelFullscreen,
        }
      ]">

      <button
          v-if="state.editorVisible && rightPanelFullscreen"
          type="button"
          class="toggle-fullscreen"
          title="exit fullscreen"
          @click.prevent="onEditorExitFullscreen">
        <i class="material-icons">fullscreen_exit</i>
      </button>
      <button
          v-if="state.editorVisible && !rightPanelFullscreen"
          type="button"
          class="toggle-fullscreen"
          :title="$i18n('enterFullscreen')"
          @click.prevent="onEditorEnterFullscreen">
        <i class="material-icons">fullscreen</i>
      </button>

      <component
          v-if="state.editorVisible && getChildByPath('editor')"
          :is="getChildByPath('editor').component"
          :model="getChildByPath('editor')">
      </component>

      <component
          v-else-if="getChildByPath('right-panel')"
          :is="getChildByPath('right-panel').component"
          :model="getChildByPath('right-panel')">
      </component>

      <component
          v-else-if="getChildByPath('components')"
          :is="getChildByPath('components').component"
          :model="getChildByPath('components')">
      </component>

      <div v-else>missing panel</div>

    </aside>
  </div>
</template>

<script>
import {set} from '../../../../../../js/utils'


export default {
  props: ['model'],
  computed: {
    state: function () {
      return $perAdminApp.getView().state
    },
    editorVisible: function () {
      return $perAdminApp.getNodeFromView('/state/editorVisible')
    },
    getRightPanelClasses: function () {
      return `right-panel ${$perAdminApp.getView().state.rightPanelVisible ? 'visible' : ''}`
    },
    rightPanelFullscreen: {
      get() {
        return this.state.rightPanelFullscreen
      },
      set(fullscreen) {
        set($perAdminApp.getView(), '/state/rightPanelFullscreen', fullscreen)
      }
    }
  },
  watch: {
    'state.editorVisible'(val) {
      this.fullscreen = this.state.rightPanelFullscreen
    }
  },
  mounted() {
    this.fullscreen = this.state.rightPanelFullscreen
  },
  methods: {
    getChildByPath(childName) {
      var path = this.model.path + '/' + childName
      for (var i = 0; i < this.model.children.length; i++) {
        if (this.model.children[i].path === path) {
          var ret = this.model.children[i]
          ret.classes = 'col fullheight s4'
          return ret
        }
      }
      return null
    },

    showHide(me, name) {
      $perAdminApp.getView().state.rightPanelVisible = $perAdminApp.getView().state.rightPanelVisible
          ? false : true
    },

    showComponentEdit(me, target) {
      const view = $perAdminApp.getView()
      const editorPath = view.state.editor ? view.state.editor.path : null

      if (editorPath && editorPath === target) {
        return this.openEditor(target)
      }

      if (!editorPath) {
        return this.openEditor(target)
      }

      const originalData = view.state.editor ? view.state.editor.originalData : null
      const currentNode = originalData ? $perAdminApp.findNodeFromPath(view.pageView.page, editorPath) : null
      const replacer = (k, v) => k === '_opDeleteProps' || k === 'children' || v === null || v === '' ? undefined : v
      const originalStr = JSON.stringify(originalData, replacer)
      const currentStr = JSON.stringify(currentNode, replacer)
      if (originalStr !== currentStr) {
          let diffIdx = -1
          for (let i = 0; i < Math.max(originalStr.length, currentStr.length); i++) {
            if (originalStr[i] !== currentStr[i]) { diffIdx = i; break; }
          }
        }
      if (!originalData || !currentNode || originalStr === currentStr) {
        return this.openEditor(target)
      }

      const contentview = me.$children.find(child => typeof child.wrapEditableAroundElement === 'function')
      const savedPreviousTarget = contentview ? contentview.previousTarget : null

      return new Promise((resolve) => {
        $perAdminApp.askUser('Save Page Edit?', 'Would you like to save your page edits?', {
          defaultFocus: 'keepEditing',
          yesText: 'Save',
          noText: 'Discard Changes',
          keepEditingText: 'Keep Editing',
          yes: () => {
            const page = view.pageView.page
            const path = view.state.editor.path
            const data = $perAdminApp.findNodeFromPath(page, path)
            $perAdminApp.stateAction('savePageEdit', { pagePath: view.pageView.path, path, data}).then(() => {
              this.openEditor(target).then(resolve)
            })
          },
          no: () => {
            $perAdminApp.getApi().populatePageView(view.pageView.path).then(() => {
              this.openEditor(target).then(resolve)
            })
          },
          keepEditing: () => {
            if (contentview && savedPreviousTarget) {
              contentview.target = savedPreviousTarget
              contentview.wrapEditableAroundElement(contentview.findComponentEl(savedPreviousTarget))
            }
            resolve()
          }
        })
      })
    },

    openEditor(target) {
      return $perAdminApp.stateAction('editComponent', target).then(() => {
        set($perAdminApp.getView(), `/state/editorVisible`, true)
      })
    },

    onEditorEnterFullscreen() {
      this.rightPanelFullscreen = true
    },

    onEditorExitFullscreen() {
      this.rightPanelFullscreen = false
    }
  }
}
</script>
