<template>
  <div class="mein-kram" id="das-ist-ein-test">
    <admin-components-explorerpreviewcontent
      :model="model"
      :nodeType="NodeType.OBJECT_DEFINITION_FILE"
      :browserRoot="`${getBasePath()}/object-definitions`"
      :currentPath="`${getBasePath()}/object-definitions`"
      :onDelete="onDelete"
    />
  </div>
</template>

<script>
import { NodeType } from '../../../../../../js/constants';

export default {
  name: 'ObjectDefinitionFilePreview',
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
    };
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
