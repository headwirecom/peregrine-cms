<template>
  <div :class="['explorer-preview-content', `preview-${nodeType}`]">
    <template v-if="currentObject">
      <ul class="explorer-preview-nav">
        <li class="icon-list">
          <div class="icons-left">
            <a title="og-tags"
               class="icon-list-tab waves-effect waves-light"
               :class="{'active': (tab==='og-tag')}"
               v-on:click.stop.prevent="onTabClick('og-tag')">
              <i class="editor-icon material-icons">label</i>
            </a>
            <a :title="`${nodeType}-info`"
               class="icon-list-tab waves-effect waves-light"
               :class="{'active': (tab==='info')}"
               v-on:click.stop.prevent="onTabClick('info')">
              <i class="editor-icon material-icons">settings</i>
            </a>
          </div>

          <div class="icons-right">
            <template v-if="allowOperations">
              <a href="#!"
                 :title="`rename ${nodeType}`"
                 class="icon-list-btn waves-effect waves-light"
                 v-on:click.stop.prevent="renameNode">
                <i class="svg-icons svg-icon-rename"></i>
              </a>
              <a href="#!"
                 :title="`move ${nodeType}`"
                 class="icon-list-btn waves-effect waves-light"
                 v-on:click.stop.prevent="moveNode">
                <i class="material-icons">compare_arrows</i>
              </a>
              <a href="#!"
                 :title="`delete ${nodeType}`"
                 class="icon-list-btn waves-effect waves-light"
                 v-on:click.stop.prevent="deleteNode">
                <i class="material-icons">delete</i>
              </a>
            </template>
            <template>
              <a v-if="edit"
                 title="cancel edit"
                 class="icon-list-btn waves-effect waves-light"
                 v-on:click.stop.prevent="onCancel">
                <i class="editor-icon material-icons">info</i>
              </a>
              <a v-else
                 :title="`edit ${nodeType} properties`"
                 class="icon-list-btn waves-effect waves-light"
                 v-on:click.stop.prevent="onEdit">
                <i class="editor-icon material-icons">edit</i>
              </a>
            </template>
          </div>
        </li>
      </ul>
      <div v-if="!edit" class="show-overflow">
        <div v-if="tab==='og-tag'">
          <vue-form-generator
              class="vfg-preview"
              v-on:validated="onValidated"
              v-bind:schema="readOnlyOgTagSchema"
              v-bind:model="node"
              v-bind:options="options">
          </vue-form-generator>
        </div>
        <div v-else-if="tab==='info'">
          <vue-form-generator
              class="vfg-preview"
              v-on:validated="onValidated"
              v-bind:schema="readOnlySchema"
              v-bind:model="node"
              v-bind:options="options">
          </vue-form-generator>
        </div>
      </div>
      <template v-else>
        <div v-if="tab==='og-tag'" class="show-overflow">
          <vue-form-generator
              v-bind:schema="ogTagSchema"
              v-bind:model="node"
              v-bind:options="options">
          </vue-form-generator>
        </div>
        <div v-else-if="tab==='info'" class="show-overflow">
          <vue-form-generator
              v-bind:schema="schema"
              v-bind:model="node"
              v-bind:options="options">
          </vue-form-generator>
        </div>
        <div class="explorer-confirm-dialog">
          <button
              type="button"
              :title="`save ${nodeType} properties`"
              v-bind:disabled="!valid"
              class="btn btn-raised waves-effect waves-light right"
              v-on:click.stop.prevent="onOk">
            <i class="material-icons">check</i>
          </button>
        </div>
      </template>
    </template>
    <div v-else class="explorer-preview-empty">
      <span>{{ $i18n(`no ${nodeType} selected`) }}</span>
      <i class="material-icons">info</i>
    </div>

    <admin-components-pathbrowser
        v-if="isOpen"
        :isOpen="isOpen"
        :browserRoot="browserRoot"
        :browserType="nodeType"
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
    props: {
      model: {
        type: Object,
        required: true
      },
      nodeType: {
        type: String,
        required: true
      }
    },
    data: function () {
      return {
        tab: 'info',
        valid: true,
        isOpen: false,
        browserRoot: `/content/${this.nodeType}s`,
        currentPath: `/content/${this.nodeType}s`,
        selectedPath: null,
        options: {
          validateAfterLoad: true,
          validateAfterChanged: true,
          focusFirstField: true
        }
      }
    },
    computed: {
      uNodeType() {
        return this.capFirstLetter(this.nodeType);
      },
      edit() {
        return $perAdminApp.getNodeFromView('/state/tools').edit;
      },
      readOnlySchema() {
        if (!this.schema) {
          return {};
        }
        const roSchema = JSON.parse(JSON.stringify(this.schema));
        roSchema.fields.forEach((field) => {
          field.preview = true;
          field.readonly = true;
          if (field.fields) {
            field.fields.forEach((field) => {
              field.readonly = true;
            })
          }
        });
        return roSchema;

      },
      readOnlyOgTagSchema() {
        if (!this.ogTagSchema) {
          return {};
        }
        const roSchema = JSON.parse(JSON.stringify(this.ogTagSchema));
        roSchema.fields.forEach((field) => {
          field.preview = true;
          field.readonly = true;
          if (field.fields) {
            field.fields.forEach((field) => {
              field.readonly = true;
            })
          }
        });
        return roSchema;

      },
      currentObject() {
        return $perAdminApp.getNodeFromViewOrNull(`/state/tools/${this.nodeType}`);
      },
      node() {
        return $perAdminApp.findNodeFromPath(this.$root.$data.admin.nodes, this.currentObject);
      },
      allowOperations() {
        return this.currentObject.split('/').length > 4;
      },
      schema() {
        const view = $perAdminApp.getView();
        const component = this.node.component;
        return view.admin.componentDefinitions[component].model;
      },
      ogTagSchema() {
        const view = $perAdminApp.getView();
        if (this.node) {
          const component = this.node.component;
          return view.admin.componentDefinitions[component].ogTags;
        }
      }
    },
    methods: {
      capFirstLetter(string) {
        return string.charAt(0).toUpperCase() + string.slice(1);
      },
      onEdit: function () {
        Vue.set($perAdminApp.getNodeFromView('/state/tools'), 'edit', true);
      },
      onValidated(isValid, errors) {
        this.valid = isValid;
      },
      renameNode() {
        let newName = prompt('new name for ' + this.node.name);
        if (newName) {
          $perAdminApp.stateAction(`rename${this.uNodeType}`, {
            path: this.node.path,
            name: newName
          });
          let newPath = this.currentObject.split('/');
          newPath.pop();
          newPath.push(newName);
          $perAdminApp.stateAction(`show${this.uNodeType}Info`, {
            selected: newPath.join('/')
          });
        }
      },
      moveNode() {
        $perAdminApp.getApi().populateNodesForBrowser(this.currentPath, 'pathBrowser')
        .then(() => {
          this.isOpen = true;
        }).catch((err) => {
          $perAdminApp.getApi().populateNodesForBrowser('/content', 'pathBrowser');
        });
      },
      deleteNode() {
        $perAdminApp.stateAction(`delete${this.uNodeType}`, this.node.path);
        $perAdminApp.stateAction(`unselect${this.uNodeType}`, {});
        this.isOpen = false;
      },
      setCurrentPath(path) {
        this.currentPath = path;
      },
      setSelectedPath(path) {
        this.selectedPath = path;
      },
      onMoveCancel() {
        this.isOpen = false;
      },
      onMoveSelect() {
        $perAdminApp.stateAction(`move${this.uNodeType}`, {
          path: this.node.path,
          to: this.selectedPath,
          type: 'child'
        });
        $perAdminApp.stateAction(`unselect${this.uNodeType}`, {});
        this.isOpen = false;
      },
      onOk() {
        $perAdminApp.stateAction(`save${this.uNodeType}Properties`, this.node);
        $perAdminApp.getNodeFromView('/state/tools').edit = false;
      },
      onCancel: function () {
        $perAdminApp.stateAction(`show${this.uNodeType}Info`, {selected: this.node.path});
        $perAdminApp.getNodeFromView('/state/tools').edit = false;
      },
      onTabClick(clickedTab) {
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
