<template>
  <admin-components-explorerpreviewcontent
    :model="model"
    :nodeType="nodeType"
    :browserRoot="browserRoot"
    :currentPath="currentPath"
    :onDelete="onDelete"
  />
</template>

<script>
import { NodeType } from '../../../../../../js/constants';
import { get, set } from '../../../../../../js/utils';

export default {
  name: 'FilePreview',
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
  data() {
    return {
      nodeType: NodeType.FILE,
      currentPath: null,
    };
  },
  computed: {
    filePath() {
      return get(
        $perAdminApp.getView(),
        '/state/tools/file',
        this.getBasePath()
      );
    },
    browserRoot() {
      const split = this.filePath.split('/');

      return split.slice(0, split.length - 2).join('/');
    },
  },
  created() {
    this.currentPath = this.browserRoot;
  },
  beforeMount() {
    set($perAdminApp.getView(), '/state/rightPanelFullscreen', false);
    set($perAdminApp.getView(), '/state/rightPanelVisible', true);
  },
  methods: {
    getBasePath() {
      const view = $perAdminApp.getView();
      let tenant = { name: 'example' };
      if (view.state.tenant) {
        tenant = view.state.tenant;
      }
      return `/content/${tenant.name}`;
    },
  },
};
</script>
<style scoped></style>
