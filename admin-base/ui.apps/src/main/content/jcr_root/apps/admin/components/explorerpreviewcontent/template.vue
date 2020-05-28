<template>
  <div :class="['explorer-preview-content', `preview-${nodeType}`]">

    <template v-if="currentObject">
      <div class="explorer-preview-nav">
        <ul class="nav-left" v-if="hasMultipleTabs">
          <admin-components-explorerpreviewnavitem
              v-if="!!($slots.default)"
              icon="view_list"
              title="component explorer"
              :class="{'active': isTab(Tab.COMPONENTS)}"
              @click="setActiveTab(Tab.COMPONENTS)"/>
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
          <admin-components-explorerpreviewnavitem
              :icon="Icon.MORE_VERT"
              :title="'actions'"
              :class="{'active': isTab(Tab.ACTIONS)}"
              @click="setActiveTab(Tab.ACTIONS)"/>
        </ul>

        <ul class="nav-right"></ul>
      </div>

      <template v-if="isTab([Tab.COMPONENTS])">
        <slot></slot>
      </template>

      <template v-else-if="isTab([Tab.INFO, Tab.OG_TAGS])">
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
            v-if="node && getSchemaByActiveTab()"
            :class="{'vfg-preview': !edit}"
            :schema="getSchemaByActiveTab()"
            :model="node"
            :options="options"
            @validated="onValidated()"
            @model-updated="onModelUpdate">
        </vue-form-generator>
        <div class="explorer-confirm-dialog">
          <template v-if="edit">
            <button
                class="btn btn-raised waves-effect waves-light right"
                type="button"
                :title="`cancel editing ${nodeType} properties`"
                @click.stop.prevent="onCancel()">
              <i class="material-icons">{{Icon.CANCEL}}</i>
            </button>
            <button
                class="btn btn-raised waves-effect waves-light right"
                type="button"
                :title="`save ${nodeType} properties`"
                :disabled="!valid"
                @click.stop.prevent="save()">
              <i class="material-icons">{{Icon.CHECK}}</i>
            </button>
          </template>
          <template v-else>
            <span></span>
            <button
                class="btn btn-raised waves-effect waves-light right"
                type="button"
                :title="`edit ${nodeType} properties`"
                @click.stop.prevent="onEdit()">
              <i class="material-icons">{{Icon.EDIT}}</i>
            </button>
          </template>
        </div>
      </template>

      <template v-else-if="isTab(Tab.REFERENCES)">
        <ul :class="['collection', 'with-header', `explorer-${nodeType}-referenced-by`]">
          <li class="collection-header">
            referenced in {{referencedBy.length}} locations
          </li>
          <li v-for="item in referencedBy" :key="item.path" class="collection-item">
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

      <template v-else-if="isTab(Tab.ACTIONS)">
        <div v-if="allowOperations" class="action-list">
          <div class="action" :title="`rename ${nodeType}`" @click="renameNode()">
            <i class="material-icons">{{Icon.TEXT_FORMAT}}</i>
            Rename {{nodeType}}
          </div>
          <div class="action" :title="`move ${nodeType}`" @click="moveNode()">
            <i class="material-icons">{{Icon.COMPARE_ARROWS}}</i>
            Move {{nodeType}}
          </div>
          <div class="action" :title="`delete ${nodeType}`" @click="deleteNode()">
            <i class="material-icons">{{Icon.DELETE}}</i>
            Delete {{nodeType}}
          </div>
        </div>
      </template>
    </template>

    <template v-else>
      <div v-if="!currentObject" class="explorer-preview-empty">
        <span>{{ $i18n(`no${uNodeType}Selected`) }}</span>
        <i class="material-icons">info</i>
      </div>
    </template>

    <admin-components-pathbrowser
        v-if="isOpen"
        :isOpen="isOpen"
        :header="`Move ${nodeName}`"
        :browserRoot="browserRoot"
        :browserType="nodeType"
        :currentPath="path.current"
        :selectedPath="path.selected"
        :setCurrentPath="setCurrentPath"
        :setSelectedPath="setSelectedPath"
        :onCancel="onMoveCancel"
        :onSelect="onMoveSelect">
    </admin-components-pathbrowser>

  </div>
</template>

<script>
  import {Icon, MimeType, NodeType, SUFFIX_PARAM_SEPARATOR} from '../../../../../../js/constants'
  import {deepClone} from '../../../../../../js/utils'

  const Tab = {
    INFO: 'info',
    OG_TAGS: 'og-tags',
    REFERENCES: 'references',
    COMPONENTS: 'components',
    ACTIONS: 'actions'
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
      },
      tab: {
        type: String,
        default: Tab.INFO
      },
      isEdit: {
        type: Boolean,
        default: false
      }
    },
    data() {
      return {
        Icon: Icon,
        Tab: Tab,
        SchemaKey: SchemaKey,
        NodeType: NodeType,
        activeTab: null,
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
        },
        path: {
          current: null,
          selected: null
        },
        formGenerator: {
          changes: []
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
        if (this.nodeType === NodeType.OBJECT) {
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
      },
      nodeName() {
        let nodeName = this.node.name;
        if (this.nodeType === NodeType.OBJECT) {
          nodeName = this.node.path.split('/').slice(-1).pop()
        }
        return nodeName
      }
    },
    watch: {
      edit(newVal) {
        $perAdminApp.getNodeFromView('/state/tools').edit = newVal;
      }
    },
    created() {
      this.activeTab = this.tab
    },
    mounted() {
      this.path.selected = this.selectedPath
      this.path.current = this.currentPath
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
        const componentDefinitions = view.admin.componentDefinitions
        if (!componentDefinitions) {
          return {}
        }
        const cmpDefinition = view.admin.componentDefinitions[component]
        if (!cmpDefinition) {
          return {}
        }
        let schema = cmpDefinition[schemaKey];
        if (this.edit) {
          return schema;
        }
        if (!schema) {
          return {};
        }
        schema = deepClone(schema);
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
        this.edit = true
        this.formGenerator.original = deepClone(this.node)
      },
      onCancel() {
        const payload = {selected: this.currentObject}
        this.edit = false
        let node = this.node
        this.formGenerator.changes.forEach((ch) => {
          node[ch.key] = ch.oldVal
        })
        this.formGenerator.changes = []
      },
      onModelUpdate(newVal, schemaKey) {
        if (this.edit) {
          this.formGenerator.changes.push({
            key: schemaKey,
            oldVal: this.formGenerator.original[schemaKey],
            newVal: newVal
          })
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
        const newName = prompt(`new name for "${this.nodeName}"`)
        if (newName) {
          const that = this;
          $perAdminApp.stateAction(`rename${this.uNodeType}`, {
            path: this.currentObject,
            name: newName,
            edit: this.isEdit
          }).then(() => {
            if (that.nodeType === 'asset' || that.nodeType === 'object') {
              const currNode = $perAdminApp.getNodeFromView(`/state/tools/${that.nodeType}/show`)
              const currNodeArr = currNode.split('/');
              currNodeArr[currNodeArr.length - 1] = newName
              $perAdminApp.getNodeFromView(`/state/tools/${that.nodeType}`).show = currNodeArr.join(
                  '/')
            } else { // page and template handling
              const currNode = $perAdminApp.getNodeFromView('/state/tools')[that.nodeType]
              const currNodeArr = currNode.split('/');
              currNodeArr[currNodeArr.length - 1] = newName
              $perAdminApp.getNodeFromView('/state/tools')[that.nodeType] = currNodeArr.join('/')
            }
          });
        }
      },
      moveNode() {
        $perAdminApp.getApi().populateNodesForBrowser(this.path.current, 'pathBrowser')
            .then(() => {
              this.isOpen = true;
            }).catch(() => {
          $perAdminApp.getApi().populateNodesForBrowser(`/content/${site.tenant}`, 'pathBrowser');
        });
      },
      deleteNode() {
        const really = confirm(`Are you sure you want to delete this ${this.nodeType}?`);
        if (really) {
          $perAdminApp.stateAction(`delete${this.uNodeType}`, this.node.path).then(() => {
            $perAdminApp.stateAction(`unselect${this.uNodeType}`, {})
          }).then(() => {
            const path = $perAdminApp.getNodeFromView('/state/tools/pages')
            $perAdminApp.loadContent(
                '/content/admin/pages/pages.html/path' + SUFFIX_PARAM_SEPARATOR + path)
          })
          this.isOpen = false;
        }
      },
      setCurrentPath(path) {
        this.path.current = path;
      },
      setSelectedPath(path) {
        this.path.selected = path;
      },
      onMoveCancel() {
        this.isOpen = false;
      },
      onMoveSelect() {
        $perAdminApp.stateAction(`move${this.uNodeType}`, {
          path: this.node.path,
          to: this.path.selected,
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
        let _deleted = $perAdminApp.getNodeFromViewWithDefault('/state/tools/_deleted', {});

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
        $perAdminApp.stateAction('saveObjectEdit', {data: data, path: show}).then(() => {
          $perAdminApp.getNodeFromView('/state/tools')._deleted = {}
        });
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
