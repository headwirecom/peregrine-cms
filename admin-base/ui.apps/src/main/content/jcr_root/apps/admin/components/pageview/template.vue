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
  <div class="explorer-preview-content preview-page">
    <template v-if="currentObject">
      <ul class="explorer-preview-nav">
        <template v-if="allowOperations">
          <li>
            <a  href="#!"
                title="rename page"
                class="waves-effect waves-light"
                v-on:click.stop.prevent="renamePage">
              <admin-components-iconrename></admin-components-iconrename>
            </a>
          </li>
          <li>
            <a
              href="#!"
              title="move page"
              class="waves-effect waves-light"
              v-on:click.stop.prevent="movePage">
              <i class="material-icons">compare_arrows</i>
            </a>
          </li>
          <li>
            <a
              href="#!"
              title="delete page"
              class="waves-effect waves-light"
              v-on:click.stop.prevent="deletePage">
              <i class="material-icons">delete</i>
            </a>
          </li>
        </template>
        <li class="icon-list">
          <div class="icons-left">
            <a title="og-tags"
               class="icon-list-tab waves-effect waves-light"
               :class="{'active': (tab==='og-tag')}"
               v-on:click.stop.prevent="onTabClick('og-tag')">
              <i class="editor-icon material-icons">label</i>
            </a>
            <a title="page-info"
               class="icon-list-tab waves-effect waves-light"
               :class="{'active': (tab==='info')}"
               v-on:click.stop.prevent="onTabClick('info')">
              <i class="editor-icon material-icons">settings</i>
            </a>
          </div>
          <div class="icons-right">
            <a  v-if="edit"
                title="cancel edit"
                class="icon-list-btn waves-effect waves-light"
                v-on:click.stop.prevent="onCancel">
              <i class="editor-icon material-icons">info</i>
            </a>
            <a  v-else
                title="edit page properties"
                class="icon-list-btn waves-effect waves-light"
                v-on:click.stop.prevent="onEdit">
              <i class="editor-icon material-icons">edit</i>
            </a>
          </div>
        </li>

      </ul>
      <div v-if="!edit" class="show-overflow">
        <div v-if="tab==='og-tag'">
          <vue-form-generator
            class="vfg-preview"
            v-on:validated = "onValidated"
            v-bind:schema  = "readOnlyOgTagSchema"
            v-bind:model   = "page"
            v-bind:options = "options">
          </vue-form-generator>
        </div>
        <div v-else-if="tab==='info'">
          <vue-form-generator
            class="vfg-preview"
            v-on:validated = "onValidated"
            v-bind:schema  = "readOnlySchema"
            v-bind:model   = "page"
            v-bind:options = "options">
          </vue-form-generator>
        </div>
      </div>
      <template v-else>
        <div v-if="tab==='og-tag'" class="show-overflow">
          <vue-form-generator
            v-bind:schema="ogTagSchema"
            v-bind:model="page"
            v-bind:options="options">
          </vue-form-generator>
        </div>
        <div v-else-if="tab==='info'" class="show-overflow">
          <vue-form-generator
            v-bind:schema="schema"
            v-bind:model="page"
            v-bind:options="options">
          </vue-form-generator>
        </div>
        <div class="explorer-confirm-dialog">
          <button
            type="button"
            title="save page properties"
            v-bind:disabled="!valid"
            class="btn btn-raised waves-effect waves-light right"
            v-on:click.stop.prevent="onOk">
            <i class="material-icons">check</i>
          </button>
        </div>
      </template>
    </template>
    <div v-else class="explorer-preview-empty">
      <span>{{$i18n('no page selected')}}</span>
      <i class="material-icons">info</i>
    </div>

    <admin-components-pathbrowser
      v-if="isOpen"
      :isOpen="isOpen"
      :browserRoot="browserRoot"
      :browserType="'page'"
      :currentPath="currentPath"
      :selectedPath="selectedPath"
      :setCurrentPath="setCurrentPath"
      :setSelectedPath="setSelectedPath"
      :onCancel="onMoveCancel"
      :onSelect="onMoveSelect">
    </admin-components-pathbrowser>
  </div>
</template>

<script>
export default {
  props: ['model'],
  updated: function() {
    let stateTools = $perAdminApp.getNodeFromView("/state/tools");
    stateTools._deleted = {};
  },
  computed: {
    edit() {
      return $perAdminApp.getNodeFromView('/state/tools').edit
    },
    readOnlySchema() {
      if(!this.schema) return {}
      const roSchema = JSON.parse(JSON.stringify(this.schema))
      roSchema.fields.forEach( (field) => {
        field.preview = true
        field.readonly = true
        if(field.fields) {
          field.fields.forEach( (field) => {
            field.readonly = true
          })
        }
      })
      return roSchema

    },
    readOnlyOgTagSchema() {
      if(!this.ogTagSchema) return {}
      const roSchema = JSON.parse(JSON.stringify(this.ogTagSchema))
      roSchema.fields.forEach( (field) => {
        field.preview = true
        field.readonly = true
        if(field.fields) {
          field.fields.forEach( (field) => {
            field.readonly = true
          })
        }
      })
      return roSchema

    },
    currentObject() {
      return $perAdminApp.getNodeFromViewOrNull("/state/tools/page")
    },
    page() {
      return $perAdminApp.findNodeFromPath(this.$root.$data.admin.nodes, this.currentObject)
    },
    allowOperations() {
      return this.currentObject.split('/').length > 4
    },
    schema() {
      const view = $perAdminApp.getView()
      if(this.page) {
        const component = this.page.component
        const schema = view.admin.componentDefinitions[component].model;
        return schema
      }
    },
    ogTagSchema() {
      const view = $perAdminApp.getView();
      if(this.page) {
        const component = this.page.component;
        const ogTagSchema = view.admin.componentDefinitions[component].ogTags;
        return ogTagSchema;
      }
    },

  },
  data: function() {
    return {
      isOpen: false,
      tab: "info",
      browserRoot: '/content/sites',
      currentPath: '/content/sites',
      selectedPath: null,
      valid: true,
      options: {
        validateAfterLoad: true,
        validateAfterChanged: true,
        focusFirstField: true
      }
    }
  },
  methods: {
    onEdit: function() {
      Vue.set($perAdminApp.getNodeFromView('/state/tools'), 'edit', true)
    },
    onValidated(isValid, errors) {
      this.valid = isValid
    },
    renamePage() {
      let newName = prompt('new name for '+this.page.name)
      if(newName) {
        $perAdminApp.stateAction('renamePage', { path: this.page.path, name: newName})
        let newPath = this.currentObject.split('/')
        newPath.pop()
        newPath.push(newName)
        $perAdminApp.stateAction('showPageInfo', { selected: newPath.join('/') })
      }
    },
    deletePage() {
      $perAdminApp.stateAction('deletePage', this.page.path)
//                $perAdminApp.stateAction('showPageInfo', { selected: null })
    },
    setCurrentPath(path){
      this.currentPath = path
    },
    setSelectedPath(path){
      this.selectedPath = path
    },
    movePage() {
      $perAdminApp.getApi().populateNodesForBrowser(this.currentPath, 'pathBrowser')
        .then( () => {
          this.isOpen = true
        }).catch( (err) => {
        $perAdminApp.getApi().populateNodesForBrowser('/content', 'pathBrowser')
      })
    },
    onMoveCancel(){
      this.isOpen = false
    },
    onMoveSelect() {
      $perAdminApp.stateAction('movePage', { path: this.page.path, to: this.selectedPath, type: 'child'})
      $perAdminApp.stateAction('unselectPage', { })
      this.isOpen = false
    },
    onCancel: function() {
      $perAdminApp.stateAction('showPageInfo', { selected: this.page.path  })
      $perAdminApp.getNodeFromView('/state/tools').edit = false
    },
    onOk() {
      let data = JSON.parse(JSON.stringify(this.page));
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
      $perAdminApp.stateAction('savePageProperties', data)
      $perAdminApp.getNodeFromView('/state/tools').edit = false
    },
    onTabClick( clickedTab ){
      this.tab = clickedTab;
    }
  }
}
</script>

<style scoped>
  .icon-list {
    width: 100%;
  }

  .icon-list-tab {
    height: 44px;
    margin-top: -2px;
    padding: 6px 5px 5px 5px;
  }
  .icon-list-btn {
    height: 44px;
    width: 44px;
    margin-top: -2px;
    padding: 6px 5px 5px 5px;
  }

  .icons-left {
    float: left;
    height: 44px;
    margin-left: 10px;
  }

  .active {
    background-color: #37474f;
    color: #cfd8dc;
  }

  .show-overflow {
    overflow: auto;
  }

  .icons-right {
    float: right;
    height: 44px;
    margin-right: 10px;
  }
  .editor-icon {
    /*width: 44px;*/
    height: 44px;
    margin-right: 5px;
    margin-left: 5px;
  }
</style>
