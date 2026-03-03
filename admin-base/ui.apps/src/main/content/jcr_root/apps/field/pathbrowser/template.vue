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
    <div class="wrap">
      <template v-if="!schema.preview">
      <div class="picker-container">
        <input
          :id="getFieldID(schema)"
          type="text"
          :value="value"
          :disabled="disabled"
          :maxlength="schema.max"
          :placeholder="schema.placeholder"
          :readonly="schema.readonly"
          :title="value"
          @input="onInputInput"
          @focus="editing = true"
          @blur="editing = false"/>
        <button v-if="!schema.readonly" :disabled="disabled" v-on:click.stop.prevent="browse" class="btn-flat picker-open">
          <icon v-bind="buttonIcon"/>
        </button>
        <button v-if="!schema.readonly" :disabled="disabled" v-on:click.stop.prevent="remove" class="btn-flat picker-remove">
          <icon v-bind="removeButtonIcon"/>
        </button>
      </div>
        <img v-if="isImage(value)" :src="sanitizedValue" />
        <path-browser-component
            v-if="isOpen"
            :isOpen="isOpen"
            :browserRoot="browserRoot"
            :browserType="browserType"
            :currentPath="currentPath"
            :selectedPath="selectedPath"
            :newWindow="newWindow"
            @toggle-newWindow="toggleNewWindow"
            :rel="rel"
            @toggle-rel="toggleRel"
            :withLinkTab="withLinkTab"
            :setCurrentPath="setCurrentPath"
            :setSelectedPath="setSelectedPath"
            :onCancel="onCancel"
            @select="onSelect">
        </path-browser-component>
      </template>
      <p v-else>{{value}}</p>
    </div>
</template>

<script>
import {IconLib, PathBrowser} from '../../../../../js/constants'
import {getBasePath} from '../../../../../js/mixins'

import PathBrowserComponent from '../../admin/components/pathbrowser/template.vue'
import Icon from '../../admin/components/icon/template.vue'


  export default {
        props: ['model'],
        components: {Icon, PathBrowserComponent},
        mixins: [ VueFormGenerator.abstractField , getBasePath],
        data () {
            return {
                isOpen: false,
                browserRoot: '/assets',
                browserType: PathBrowser.Type.ASSET,
                currentPath: '/assets',
                newWindow: false,
                rel: false,
                selectedPath: null,
                withLinkTab: true,
                editing: false
            }
        },
        computed: {
			sanitizedValue: {
				get () {
      		        return this.value ? this.value : ''
				},
				set (newValue) {
					this.value = newValue
				}
			},
          buttonIcon() {
            return this.model.buttonIcon || {icon: 'insert_drive_file', lib: IconLib.MATERIAL_ICONS};
          },
          removeButtonIcon() {
            return this.model.buttonIcon || {icon: 'delete', lib: IconLib.MATERIAL_ICONS};
          }
		},
      created() {
          this.browserRoot = this.getBasePath() + this.browserRoot
          this.currentPath = this.getBasePath() + this.currentPath
      },
      methods: {
           onInputInput(event) {
             if (!this.editing) {
               this.value = event.target.value
             }
           },
            onCancel(){
                this.isOpen = false
            },
            onSelect() {
                this.value = this.selectedPath
                this.$emit('select', this.selectedPath);
                this.isOpen = false
            },
            setCurrentPath(path){
                this.currentPath = path
            },
            setSelectedPath(path){
                this.selectedPath = path
            },
            isImage(path) {
                return /\.(jpg|png|gif)$/.test(path);
            },
            isValidPath(path, root){
                return path && path !== root && path.includes(root)
            },
            browse() {
                // root path is used to limit top lever directory of path browser
                let root = this.schema.browserRoot
                if(!root) {
                    console.warn('browserRoot not defined in schema. All paths are available.')
                    root = '/'
                }
                // browser type is used to limit browsing and show correct file/icon types
                let type = this.schema.browserType
                if(!type) {
                    root === `${this.getBasePath()}/pages` ? type = PathBrowser.Type.PAGE : type = PathBrowser.Type.ASSET
                }
                let selectedPath = this.value
                // current path is the active directory in the path browser
                let currentPath
                // if a selected path is valid, currentPath becomes the selected path's parent
                if(this.isValidPath(selectedPath, root)){
                    currentPath = selectedPath.substr(0, selectedPath.lastIndexOf('/'))
                } else { // if path is invalid
                    currentPath = root
                }
                this.browserRoot = root
                this.browserType = type
                this.currentPath = currentPath
                this.selectedPath = selectedPath

      let options = this.schema.browserOptions
      if (options && options.withLink !== null && options.withLink !== undefined) {
        this.withLinkTab = !!options.withLink
      } else {
        this.withLinkTab = !(type === PathBrowser.Type.IMAGE)
      }
      $perAdminApp.getApi().populateNodesForBrowser(currentPath, 'pathBrowser')
          .then((path, name, data) => {
            this.isOpen = true
          }).catch((err) => {
        $perAdminApp.getApi().populateNodesForBrowser('/content', 'pathBrowser')
      })
    },
    remove() {
      this.value = "";
    },
    toggleNewWindow() {
      this.newWindow = !this.newWindow;
    },
    toggleRel() {
      this.rel = !this.rel;
    }
  }
}
</script>
<style>
.picker-container {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  grid-template-rows: 1fr;
  grid-column-gap: 0px;
  grid-row-gap: 0px;
  margin-bottom: 0.25rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  min-width: 0;

  *  {
    width: 100% !important;
    height: 100% !important;
    min-width: 0;
  }
  input {
    grid-area: 1 / 1 / 2 / 6;
  }
  .picker-open {
    grid-area: 1 / 6 / 2 / 7;
    color: #546e7a !important;
  }
  .picker-remove {
    grid-area: 1 / 7 / 2 / 8;
    color: #546e7a !important;
  }
}
</style>