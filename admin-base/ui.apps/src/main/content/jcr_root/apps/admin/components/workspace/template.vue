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
  <div v-bind:class="[
      `peregrine-workspace`,
      {
        'right-panel-visible': state.rightPanelVisible,
        'editor-visible': state.editorVisible
      }
    ]">
    <component
        v-bind:is="getChildByPath('contentview').component"
        v-bind:model="getChildByPath('contentview')">
    </component>

    <admin-components-action
        v-bind:class="['right-panel-toggle', {'hide-right-panel': state.rightPanelVisible, 'show-right-panel': !state.rightPanelVisible}]"
        v-bind:model="{
                target: 'rightPanelVisible',
                command: 'showHide',
                tooltipTitle: state.rightPanelVisible? $i18n('hideComponentsPanel') : $i18n('showComponentsPanel')
            }">
      <i class="material-icons" v-if="state.rightPanelVisible">keyboard_arrow_right</i>
      <i class="material-icons" v-else>keyboard_arrow_left</i>
    </admin-components-action>

    <aside v-bind:class="[
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
          v-on:click.prevent="onEditorExitFullscreen">
        <i class="material-icons">fullscreen_exit</i>
      </button>
      <button
          v-if="state.editorVisible && !rightPanelFullscreen"
          type="button"
          class="toggle-fullscreen"
          v-bind:title="$i18n('enterFullscreen')"
          v-on:click.prevent="onEditorEnterFullscreen">
        <i class="material-icons">fullscreen</i>
      </button>

      <component
          v-if="state.editorVisible && getChildByPath('editor')"
          v-bind:is="getChildByPath('editor').component"
          v-bind:model="getChildByPath('editor')">
      </component>

      <component
          v-else-if="getChildByPath('right-panel')"
          v-bind:is="getChildByPath('right-panel').component"
          v-bind:model="getChildByPath('right-panel')">
      </component>

      <component
          v-else-if="getChildByPath('components')"
          v-bind:is="getChildByPath('components').component"
          v-bind:model="getChildByPath('components')">
      </component>

      <div v-else>missing panel</div>

    </aside>
  </div>
</template>

<script>
import { set } from '../../../../../../js/utils';


export default {
  props: ['model'],
  data() {
    return {
      //rightPanelFullscreen: false
    };
  },
  computed: {
    state: function () {
      return $perAdminApp.getView().state;
    },
    editorVisible: function () {
      return $perAdminApp.getNodeFromView('/state/editorVisible');
    },
    getRightPanelClasses: function () {
      // rightPanelVisible: true/false
      return `right-panel ${ $perAdminApp.getView().state.rightPanelVisible ? 'visible' : '' }`;
    },
    rightPanelFullscreen: {
      get() {
        return this.state.rightPanelFullscreen;
      },
      set(fullscreen) {
        set($perAdminApp.getView(), '/state/rightPanelFullscreen', fullscreen);
      }
    }
  },
  watch: {
    'state.editorVisible'(val) {
      this.fullscreen = this.state.rightPanelFullscreen;
    }
  },
  mounted() {
    this.fullscreen = this.state.rightPanelFullscreen;
  },
  methods: {
    getChildByPath(childName) {
      var path = this.model.path + '/' + childName;
      for (var i = 0; i < this.model.children.length; i++) {
        if (this.model.children[i].path === path) {
          var ret = this.model.children[i];
          ret.classes = 'col fullheight s4';
          return ret;
        }
      }
      return null;
    },

    showHide(me, name) {
      $perAdminApp.getView().state.rightPanelVisible = $perAdminApp.getView().state.rightPanelVisible
          ? false : true;
    },

    showComponentEdit(me, target) {

      set($perAdminApp.getView(), `/state/editorVisible`, false);
      // only trigger state action if another component is selected
      if ($perAdminApp.getNodeFromView('/state/editor/path') !== target) {
        return $perAdminApp.stateAction('editComponent', target).then(() => {
          set($perAdminApp.getView(), `/state/editorVisible`, true);
        });
      } else {
        return new Promise((resolve) => {
          resolve();
        });
      }
    },

    onEditorEnterFullscreen() {
      this.rightPanelFullscreen = true;
    },

    onEditorExitFullscreen() {
      this.rightPanelFullscreen = false;
    }
  }
};
</script>
