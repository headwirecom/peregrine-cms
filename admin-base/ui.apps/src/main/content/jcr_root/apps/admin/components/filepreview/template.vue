<template>
  <admin-components-explorerpreviewcontent
    :model="model"
    :nodeType="nodeType"
    :browserRoot="`${getBasePath()}/${model.browserRoot}`"
    :currentPath="`${getBasePath()}/${model.currentPath}`"
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
      NodeType: NodeType,
      nodeType: null,
    };
  },
  computed: {
    explorerpreview() {
      return get($perAdminApp.getView(), '/state/tools/explorerpreview');
    },
  },
  watch: {
    explorerpreview(val, oldval) {
      console.log(val, oldval)
    }
  },
  created() {
    const resourceType = get(
      $perAdminApp.getView(),
      '/state/tools/explorerpreview',
      null
    );
    this.nodeType = NodeType.FILE;
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
