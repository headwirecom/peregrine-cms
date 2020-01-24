<template>
  <admin-components-explorerpreviewcontent
      :model="model"
      :nodeType="NodeType.TEMPLATE"
      browserRoot="/content/templates"
      currentPath="/content/templates">
  </admin-components-explorerpreviewcontent>
</template>

<script>
  import {NodeType} from '../../../../../../js/constants';

  export default {
    props: ['model'],
    data() {
      return {
        NodeType: NodeType
      };
    }
  };
</script>
<style scoped>
</style>
