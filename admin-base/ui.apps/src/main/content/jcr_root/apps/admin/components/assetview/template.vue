<template>
  <admin-components-explorerpreviewcontent
      :model="model"
      :nodeType="NodeType.ASSET"
      browserRoot="/content/assets"
      currentPath="/content/assets">
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
