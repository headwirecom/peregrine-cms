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
        <input
          :id="getFieldID(schema)"
          type="text"
          :value="sanitizedValue"
          :disabled="disabled"
          :maxlength="schema.max"
          :placeholder="schema.placeholder"
          :readonly="schema.readonly"
          @input="value = $event.target.value" />
        <button v-if="!schema.readonly" :disabled="disabled" v-on:click.stop.prevent="browse" class="btn-flat">
          <i class="material-icons">insert_drive_file</i>
        </button>
        <img v-if="isImage(value)" :src="sanitizedValue" />
        <admin-components-pathbrowser
            v-if="isOpen"
            :isOpen="isOpen"
            :browserRoot="browserRoot"
            :browserType="browserType"
            :currentPath="currentPath"
            :selectedPath="selectedPath"
            :withLinkTab="withLinkTab"
            :setCurrentPath="setCurrentPath"
            :setSelectedPath="setSelectedPath"
            :onCancel="onCancel"
            :onSelect="onSelect">
        </admin-components-pathbrowser>
      </template>
      <p v-else>{{value}}</p>
    </div>
</template>

<script>
  import {PathBrowser} from '../../../../../js/constants';

  export default {
        props: ['model'],
        mixins: [ VueFormGenerator.abstractField ],
        data () {
            return {
                isOpen: false,
                browserRoot: '/assets',
                browserType: PathBrowser.Type.ASSET,
                currentPath: '/assets',
                selectedPath: null,
                withLinkTab: true
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
			}
		},
      created() {
          this.browserRoot = this.getBasePath() + this.browserRoot
          this.currentPath = this.getBasePath() + this.currentPath
      },
      methods: {
            getBasePath() {
              const view = $perAdminApp.getView()
              let tenant = { name: 'example' }
              if (view.state.tenant) {
                tenant = view.state.tenant
              }
              return `/content/${tenant.name}`
            },
            onCancel(){
                this.isOpen = false
            },
            onSelect() {
                this.value = this.selectedPath
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
                if(options && options.withLink){
                    this.withLinkTab = options.withLink
                } else {
                    this.withLinkTab = !(type === PathBrowser.Type.IMAGE)
                }
                $perAdminApp.getApi().populateNodesForBrowser(currentPath, 'pathBrowser')
                    .then( () => {
                        this.isOpen = true
                    }).catch( (err) => {
                        $perAdminApp.getApi().populateNodesForBrowser('/content', 'pathBrowser')
                    })
            }
        }
    }
</script>
