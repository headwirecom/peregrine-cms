<template>
  <admin-components-explorerpreviewcontent
    :key="nodeType"
    :model="model"
    :tab="tab"
    :is-edit="isEditPage"
    :nodeType="nodeType"
    :browserRoot="`${getBasePath()}/pages`"
    :currentPath="`${getBasePath()}/pages`"
    :onDelete="onDelete"
  >
    {{ nodeType }}
    <admin-components-componentexplorer
      v-if="isEditPage"
      :model="{ text: 'Components', source: '/admin/components' }"
    />
  </admin-components-explorerpreviewcontent>
</template>

<script>
import { NodeType } from '../../../../../../js/constants';
import { get, set } from '../../../../../../js/utils';

export default {
  props: {
    model: {
      type: Object,
      required: true,
    },
    onDelete: {
      type: Function,
      default: (type, path) => new Promise(),
    },
  },
  computed: {
    isEditPage() {
      return this.model.path === '/jcr:content/workspace/right-panel';
    },
    tab() {
      return this.isEditPage ? 'components' : 'info';
    },
    explorerpreview() {
      return get($perAdminApp.getView(), '/state/tools/explorerpreview', null);
    },
  },
  created() {
    if (this.explorerpreview && this.explorerpreview.resourceType === 'nt:file') {
      this.nodeType = NodeType.FILE;
    } else {
      this.nodeType = NodeType.PAGE;
    }
  },
  beforeMount() {
    set($perAdminApp.getView(), '/state/rightPanelFullscreen', false);
    set($perAdminApp.getView(), '/state/rightPanelVisible', true);
  },
  methods: {
    getBasePath() {
      const view = $perAdminApp.getView();
      let tenant = { name: 'themeclean' };
      if (view.state.tenant) {
        tenant = view.state.tenant;
      }
      return `/content/${tenant.name}`;
    },
  },
};
</script>
