<template>
  <div :class="['explorer-preview-content', `preview-${nodeType}`]">

    <div v-if="currentObject" class="explorer-preview-nav">
      <ul class="nav-left">
        <li>
          <a title="og-tags"
             class="waves-effect waves-light"
             :class="{'active': isTab(Tab.OG_TAGS)}"
             v-on:click.stop.prevent="onTabClick(Tab.OG_TAGS)">
            <i class="editor-icon material-icons">label</i>
          </a>
        </li>
        <li>
          <a :title="`${nodeType}-info`"
             class="waves-effect waves-light"
             :class="{'active': isTab(Tab.INFO)}"
             v-on:click.stop.prevent="onTabClick(Tab.INFO)">
            <i class="editor-icon material-icons">settings</i>
          </a>
        </li>
      </ul>

      <ul class="nav-right">
        <li v-if="allowOperations">
          <a href="#!"
             :title="`rename ${nodeType}`"
             class="waves-effect waves-light"
             v-on:click.stop.prevent="renameNode">
            <i class="svg-icons svg-icon-rename"></i>
          </a>
        </li>
        <li v-if="allowOperations">
          <a href="#!"
             :title="`move ${nodeType}`"
             class="icon-list-btn waves-effect waves-light"
             v-on:click.stop.prevent="moveNode">
            <i class="material-icons">compare_arrows</i>
          </a>
        </li>
        <li v-if="allowOperations">
          <a href="#!"
             :title="`delete ${nodeType}`"
             class="icon-list-btn waves-effect waves-light"
             v-on:click.stop.prevent="deleteNode">
            <i class="material-icons">delete</i>
          </a>
        </li>
        <li v-if="edit">
          <a title="cancel edit"
             class="icon-list-btn waves-effect waves-light"
             v-on:click.stop.prevent="onCancel">
            <i class="editor-icon material-icons">info</i>
          </a>
        </li>
        <li v-if="!edit">
          <a :title="`edit ${nodeType} properties`"
             class="icon-list-btn waves-effect waves-light"
             v-on:click.stop.prevent="onEdit">
            <i class="editor-icon material-icons">edit</i>
          </a>
        </li>
      </ul>
    </div>

    <div v-if="!edit" class="show-overflow">
      <div v-if="isTab(Tab.OG_TAGS)">
        <vue-form-generator
            class="vfg-preview"
            v-on:validated="onValidated"
            v-bind:schema="readOnlyOgTagSchema"
            v-bind:model="node"
            v-bind:options="options">
        </vue-form-generator>
      </div>
      <div v-else-if="isTab(Tab.INFO)">
        <vue-form-generator
            class="vfg-preview"
            v-on:validated="onValidated"
            v-bind:schema="readOnlySchema"
            v-bind:model="node"
            v-bind:options="options">
        </vue-form-generator>
      </div>
    </div>

    <div v-if="edit" class="show-overflow">
      <div v-if="isTab(Tab.OG_TAGS)" class="show-overflow">
        <vue-form-generator
            v-bind:schema="ogTagSchema"
            v-bind:model="node"
            v-bind:options="options">
        </vue-form-generator>
      </div>
      <div v-else-if="isTab(Tab.INFO)" class="show-overflow">
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
    </div>

    <div v-if="!currentObject" class="explorer-preview-empty">
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
  const Tab = {
    INFO: 'info',
    OG_TAGS: 'og-tags'
  };

  export default {
    props: {
      model: {
        type: Object,
        required: true
      },
      nodeType: {
        type: String,
        required: true
      },
      browserRoot: {
        type: String,
        required: true
      },
      currentPath: {
        type: String,
        required: true
      }
    },
    data: function () {
      return {
        Tab: Tab,
        activeTab: Tab.INFO,
        valid: true,
        isOpen: false,
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
        if (!this.node) {
          return null;
        }
        const view = $perAdminApp.getView();
        const component = this.node.component;
        return view.admin.componentDefinitions[component].model;
      },
      ogTagSchema() {
        if (!this.node) {
          return null;
        }
        const view = $perAdminApp.getView();
        const component = this.node.component;
        return view.admin.componentDefinitions[component].ogTags;
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
        this.activeTab = clickedTab;
      },
      isTab(tab) {
        return this.activeTab === tab;
      }
    }
  }
</script>
<style scoped>

  .active {
    background-color: #37474f;
    color: #cfd8dc;
  }

  .show-overflow {
    overflow: auto;
  }

  .editor-icon {
    height: 44px;
    margin-right: 5px;
    margin-left: 5px;
  }

  .explorer .explorer-layout .row .explorer-preview .explorer-preview-nav .nav-right,
  .explorer .explorer-layout .row .explorer-preview .explorer-preview-nav .nav-left {
    margin: 0;
    padding: 0;
    display: flex;
  }

  .explorer .explorer-layout .row .explorer-preview .explorer-preview-nav .nav-left {
    margin-right: auto;
  }

  .explorer .explorer-layout .row .explorer-preview .explorer-preview-nav .nav-right > li,
  .explorer .explorer-layout .row .explorer-preview .explorer-preview-nav .nav-left > li {
    border-left: 1px solid #eceff1;
    border-right: 1px solid #b0bec5;
    height: 100%;
    line-height: 45px;
  }

  .explorer .explorer-layout .row .explorer-preview .explorer-preview-nav .nav-right > li:first-child,
  .explorer .explorer-layout .row .explorer-preview .explorer-preview-nav .nav-left > li:first-child {
    border-left: none;
  }

  .explorer .explorer-layout .row .explorer-preview .explorer-preview-nav .nav-right > li > a:hover,
  .explorer .explorer-layout .row .explorer-preview .explorer-preview-nav .nav-right > li > a:focus,
  .explorer .explorer-layout .row .explorer-preview .explorer-preview-nav .nav-right > li > a:active,
  .explorer .explorer-layout .row .explorer-preview .explorer-preview-nav .nav-left > li > a:hover,
  .explorer .explorer-layout .row .explorer-preview .explorer-preview-nav .nav-left > li > a:focus,
  .explorer .explorer-layout .row .explorer-preview .explorer-preview-nav .nav-left > li > a:active {
    background-color: #b0bec5;
  }

  .explorer .explorer-layout .row .explorer-preview .explorer-preview-nav .nav-right > li > a,
  .explorer .explorer-layout .row .explorer-preview .explorer-preview-nav .nav-left > li > a {
    padding: 0 0.75rem;
    height: 100%;
    display: inline-block;
  }

  .explorer .explorer-layout .row .explorer-preview .explorer-preview-nav .nav-right > li > a .material-icons,
  .explorer .explorer-layout .row .explorer-preview .explorer-preview-nav .nav-left > li > a .material-icons,
  .explorer .explorer-layout .row .explorer-preview .explorer-preview-nav .nav-right > li > a .svg-icon,
  .explorer .explorer-layout .row .explorer-preview .explorer-preview-nav .nav-left > li > a .svg-icon {
    line-height: 45px;
  }
</style>
