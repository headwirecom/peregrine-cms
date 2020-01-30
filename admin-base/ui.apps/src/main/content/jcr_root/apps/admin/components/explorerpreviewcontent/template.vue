<template>
  <div :class="['explorer-preview-content', `preview-${nodeType}`]">

    <template v-if="currentObject">
      <div class="explorer-preview-nav">
        <ul class="nav-left" v-if="hasMultipleTabs">
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
                v-if="allowMove"
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
              :src="currentObject"
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
          selectStateAction: [NodeType.ASSET, NodeType.OBJECT],
          showProp: [NodeType.ASSET, NodeType.OBJECT],
          allowMove: [NodeType.PAGE, NodeType.TEMPLATE, NodeType.ASSET]
        }
      }
    },
    computed: {
      uNodeType() {
        return this.capFirstLetter(this.nodeType);
      },
      rawCurrentObject() {
        return $perAdminApp.getNodeFromViewOrNull(`/state/tools/${this.nodeType}`);
      },
      currentObject() {
        const obj = this.rawCurrentObject;
        if (this.nodeTypeGroups.showProp.indexOf(this.nodeType) > -1) {
          if (obj && obj.hasOwnProperty('show')) {
            return obj.show;
          } else {
            return null;
          }
        }
        return obj;
      },
      node() {
        if(this.nodeType === NodeType.OBJECT) {
              return this.rawCurrentObject.data
        }
        return $perAdminApp.findNodeFromPath(this.$root.$data.admin.nodes, this.currentObject);
      },
      allowOperations() {
        return this.currentObject.split('/').length > 4;
      },
      allowMove() {
        return this.nodeTypeGroups.allowMove.indexOf(this.nodeType) > -1;
      },
      hasOgTags() {
        return this.nodeTypeGroups.ogTags.indexOf(this.nodeType) > -1;
      },
      hasReferences() {
        return this.nodeTypeGroups.references.indexOf(this.nodeType) > -1;
      },
      hasMultipleTabs() {
        return this.hasOgTags || this.hasReferences;
      },
      referencedBy() {
        return $perAdminApp.getView().state.referencedBy.referencedBy
      },
      isImage() {
        const node = $perAdminApp.findNodeFromPath(
            $perAdminApp.getView().admin.nodes, this.currentObject);
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
        if (this.nodeType === NodeType.OBJECT) {
          component = this.getObjectComponent();
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
      getObjectComponent() {
        let resourceType = this.rawCurrentObject.data['component'];
        if (!resourceType) {
          resourceType = this.rawCurrentObject.data['sling:resourceType'];
        }
        return resourceType.split('/').join('-');
      },
      capFirstLetter(string) {
        return string.charAt(0).toUpperCase() + string.slice(1);
      },
      onEdit() {
        this.edit = true;
      },
      onCancel() {
        const payload = {selected: this.currentObject};
        this.edit = false;
        if (this.nodeTypeGroups.selectStateAction.indexOf(this.nodeType) > -1) {
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
        let nodeName = this.node.name;
        if (this.nodeType === NodeType.OBJECT){
          nodeName = this.node.path.split('/').slice(-1).pop()
        }
        if (prompt(`new name for "${nodeName}"`)) {
          $perAdminApp.stateAction(`rename${this.uNodeType}`, {
            path: this.currentObject,
            name: newName
          });
          $perAdminApp.getNodeFromView('/state/tools')[this.nodeType] = null;
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
        const really = confirm(`Are you sure you want to delete this ${this.nodeType}?`);
        if (really) {
          $perAdminApp.stateAction(`delete${this.uNodeType}`, this.node.path);
          $perAdminApp.stateAction(`unselect${this.uNodeType}`, {});
          this.isOpen = false;
        }
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
        if (this.nodeType === NodeType.OBJECT) {
          this.saveObject();
        } else {
          $perAdminApp.stateAction(`save${this.uNodeType}Properties`, this.node);
          this.edit = false;
        }
      },
      saveObject() {
        let data = this.node;
        let {show} = this.rawCurrentObject;
        let _deleted = $perAdminApp.getNodeFromView("/state/tools/_deleted") || {};

        //Find child nodes with subchildren for our edited object
        for (const key in data) {
          if (!data.hasOwnProperty(key)) {
            continue;
          }
          //If node (or deleted node) is an array of objects then we have a child node
          if ((Array.isArray(data[key]) && data[key].length && typeof data[key][0] === 'object') ||
              (Array.isArray(_deleted[key]) && _deleted[key].length && typeof _deleted[key][0]
                  === 'object')) {

            let node = data[key];

            //loop through children
            let targetNode = {};
            //Insert deleted children
            for (const j in _deleted[key]) {
              if (!_deleted[key].hasOwnProperty(j)) {
                continue;
              }
              const deleted = _deleted[key][j];
              targetNode[deleted.name] = deleted;
            }
            //Insert children
            for (const i in node) {
              if (!node.hasOwnProperty(i)) {
                continue;
              }
              const child = node[i];
              targetNode[child.name] = child;
            }
            data[key] = targetNode;
          }
        }
        $perAdminApp.stateAction('saveObjectEdit', {data: data, path: show});
        $perAdminApp.stateAction('selectObject', {selected: show})
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
  }

  .info-view-iframe {
    width: 100%;
    height: 60%;
  }

  .explorer .explorer-layout .row .explorer-preview .explorer-preview-content.preview-asset .asset-info-view {
    max-height: 50%;
    height: unset;
  }

  .explorer .explorer-layout .row .explorer-preview .explorer-preview-content.preview-asset .asset-info-view img {
    max-height: 100%;
    padding-top: 1em;
  }
</style>
