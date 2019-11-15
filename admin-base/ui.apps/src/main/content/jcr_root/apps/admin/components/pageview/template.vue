<template>
  <admin-components-explorerpreviewcontent
      :model="model"
      :nodeType="NodeType.PAGE"
      browserRoot="/content/sites"
      currentPath="/content/sites">
  </admin-components-explorerpreviewcontent>
</template>

<script>
  import {NodeType} from '../../../../../../js/constants';

  export default {
    props: ['model'],
    data() {
      return  {
        NodeType: NodeType
      };
    }
  };
</script>
<style scoped>
</style>
