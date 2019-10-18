<template>
  <div :class="['explorer-preview-content', `preview-${nodeType}`]">

    <template v-if="currentObject">
      <div class="explorer-preview-nav">
        <ul class="nav-left">
          <admin-components-explorerpreviewnavitem
              :icon="Icon.SETTINGS"
              :title="`${nodeType}-info`"
              :class="{'active': isTab(Tab.INFO)}"
              @click="setActiveTab(Tab.INFO)">
          </admin-components-explorerpreviewnavitem>
          <admin-components-explorerpreviewnavitem
              v-if="hasOgTags"
              :icon="Icon.LABEL"
              :title="'og-tags'"
              :class="{'active': isTab(Tab.OG_TAGS)}"
              @click="setActiveTab(Tab.OG_TAGS)">
          </admin-components-explorerpreviewnavitem>
          <admin-components-explorerpreviewnavitem
              v-if="hasReferences"
              :icon="Icon.LIST"
              :title="'references'"
              :class="{'active': isTab(Tab.REFERENCES)}"
              @click="setActiveTab(Tab.REFERENCES)">
          </admin-components-explorerpreviewnavitem>
        </ul>

        <ul class="nav-right">
          <template v-if="allowOperations">
            <admin-components-explorerpreviewnavitem
                :icon="Icon.TEXT_FORMAT"
                :title="`rename ${nodeType}`"
                @click="renameNode()">
            </admin-components-explorerpreviewnavitem>
            <admin-components-explorerpreviewnavitem
                :icon="Icon.COMPARE_ARROWS"
                :title="`move ${nodeType}`"
                @click="moveNode()">
            </admin-components-explorerpreviewnavitem>
            <admin-components-explorerpreviewnavitem
                :icon="Icon.DELETE"
                :title="`delete ${nodeType}`"
                @click="deleteNode()">
            </admin-components-explorerpreviewnavitem>
          </template>
          <template>
            <admin-components-explorerpreviewnavitem
                v-if="edit"
                :icon="Icon.INFO"
                :title="`cancel edit`"
                @click="onCancel()">
            </admin-components-explorerpreviewnavitem>
            <admin-components-explorerpreviewnavitem
                v-else
                :icon="Icon.EDIT"
                :title="`cancel edit`"
                @click="onEdit()">
            </admin-components-explorerpreviewnavitem>
          </template>
        </ul>
      </div>

      <template v-if="isTab([Tab.INFO, Tab.OG_TAGS])">
        <div v-if="hasInfoView && !edit"
             :class="`${nodeType}-info-view`">
          <img v-if="isImage"
               :src="currentObject"
               class="info-view-image"/>
          <iframe
              v-else
              :src="currentObject.show"
              class="info-view-iframe">
          </iframe>
        </div>
        <vue-form-generator
            :class="{'vfg-preview': !edit}"
            :schema="getSchemaByActiveTab()"
            :model="node"
            :options="options"
            @validated="onValidated()">
        </vue-form-generator>
        <div v-if="edit" class="explorer-confirm-dialog">
          <button
              class="btn btn-raised waves-effect waves-light right"
              type="button"
              :title="`save ${nodeType} properties`"
              :disabled="!valid"
              @click.stop.prevent="save()">
            <i class="material-icons">{{Icon.CHECK}}</i>
          </button>
        </div>
      </template>

      <template v-else-if="isTab(Tab.REFERENCES)">
        <ul :class="['collection', 'with-header', `explorer-${nodeType}-referenced-by`]">
          <li class="collection-header">
            referenced in {{referencedBy.length}} locations
          </li>
          <li class="collection-item" v-for="item in referencedBy">
              <span>
                <admin-components-action
                    v-bind:model="{
                      target: item.path,
                      command: 'editPage',
                      tooltipTitle: `edit '${item.name}'`
                    }">
                    <admin-components-iconeditpage></admin-components-iconeditpage>
                </admin-components-action>
              </span>
            <span class="right">{{item.path}}</span>
          </li>
        </ul>
      </template>
    </template>

    <template v-else>
      <div v-if="!currentObject" class="explorer-preview-empty">
        <span>{{ $i18n(`no ${nodeType} selected`) }}</span>
        <i class="material-icons">info</i>
      </div>
    </template>

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
  import {Icon, MimeType, NodeType} from '../../../../../../js/constants';

  const Tab = {
    INFO: 'info',
    OG_TAGS: 'og-tags',
    REFERENCES: 'references'
  };

  const SchemaKey = {
    MODEL: 'model',
    OG_TAGS: 'ogTags'
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
    data() {
      return {
        Icon: Icon,
        Tab: Tab,
        SchemaKey: SchemaKey,
        NodeType: NodeType,
        activeTab: Tab.INFO,
        edit: false,
        valid: {
          state: true,
          errors: null
        },
        isOpen: false,
        selectedPath: null,
        options: {
          validateAfterLoad: true,
          validateAfterChanged: true,
          focusFirstField: true
        },
        nodeTypeGroups: {
          ogTags: [NodeType.PAGE, NodeType.TEMPLATE],
          references: [NodeType.ASSET],
          select: [NodeType.ASSET, NodeType.OBJECT]
        }
      }
    },
    computed: {
      uNodeType() {
        return this.capFirstLetter(this.nodeType);
      },
      currentObject() {
        const obj = $perAdminApp.getNodeFromViewOrNull(`/state/tools/${this.nodeType}`);
        if (this.nodeType === NodeType.ASSET) {
          if (obj && obj.hasOwnProperty('show')) {
            return obj.show;
          }
          return null;
        } else if (this.nodeType === NodeType.OBJECT) {
          if (obj) {
            return obj.data;
          }
        }
        return obj;
      },
      node() {
        return $perAdminApp.findNodeFromPath(this.$root.$data.admin.nodes, this.currentObject);
      },
      allowOperations() {
        return this.currentObject.split('/').length > 4;
      },
      hasOgTags() {
        return this.nodeTypeGroups.ogTags.indexOf(this.nodeType) > -1;
      },
      hasReferences() {
        return this.nodeTypeGroups.references.indexOf(this.nodeType) > -1;
      },
      referencedBy() {
        return $perAdminApp.getView().state.referencedBy.referencedBy
      },
      isImage() {
        const node = $perAdminApp.findNodeFromPath($perAdminApp.getView().admin.nodes,
            this.currentObject);
        if (!node) {
          return false;
        }
        const mime = node.mimeType;
        return Object.values(MimeType.Image).indexOf(mime) >= 0
      },
      hasInfoView() {
        return [NodeType.ASSET].indexOf(this.nodeType) > -1;
      }
    },
    watch: {
      edit(newVal) {
        $perAdminApp.getNodeFromView('/state/tools').edit = newVal;
      }
    },
    methods: {
      getSchema(schemaKey) {
        if (!this.node) {
          return null;
        }
        const view = $perAdminApp.getView();
        let component = this.node.component;
        if (this.nodeType === NodeType.ASSET) {
          component = 'admin-components-assetview';
        }
        if (this.nodeType === NodeType.OBJECT && !component) {
          component = this.currentObject['sling:resourceType']
        }
        let schema = view.admin.componentDefinitions[component][schemaKey];
        if (this.edit) {
          return schema;
        }
        if (!schema) {
          return {};
        }
        schema = JSON.parse(JSON.stringify(schema));
        schema.fields.forEach((field) => {
          field.preview = true;
          field.readonly = true;
          if (field.fields) {
            field.fields.forEach((field) => {
              field.readonly = true;
            });
          }
        });
        return schema;
      },
      getSchemaByActiveTab() {
        if (this.activeTab === Tab.INFO) {
          return this.getSchema(SchemaKey.MODEL);
        } else if (this.activeTab === Tab.OG_TAGS) {
          return this.getSchema(SchemaKey.OG_TAGS);
        } else {
          return {};
        }
      },
      capFirstLetter(string) {
        return string.charAt(0).toUpperCase() + string.slice(1);
      },
      onEdit() {
        this.edit = true;
      },
      onCancel() {
        let payload = {selected: this.node.path};
        this.edit = false;
        if (this.nodeTypeGroups.select.indexOf(this.nodeType) > -1) {
          $perAdminApp.stateAction(`select${this.uNodeType}`, payload)
        } else {
          $perAdminApp.stateAction(`show${this.uNodeType}Info`, payload);
        }
      },
      onValidated(isValid, errors) {
        if (this.edit) {
          return;
        }
        this.valid.state = isValid;
        this.valid.errors = errors;
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
      save() {
        $perAdminApp.stateAction(`save${this.uNodeType}Properties`, this.node);
        this.edit = false;
      },
      setActiveTab(clickedTab) {
        this.activeTab = clickedTab;
      },
      isTab(arg) {
        if (Array.isArray(arg)) {
          return arg.indexOf(this.activeTab) > -1;
        }
        return this.activeTab === arg;
      }
    }
  }
</script>
<style scoped>

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

  .info-view-image {
    margin-top: 1em;
  }

  .info-view-iframe {
    width: 100%;
    height: 60%;
    margin-top: 1em;
  }
</style>
