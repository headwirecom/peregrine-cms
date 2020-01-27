<template>
    <admin-components-explorerpreviewcontent
        :model="model"
        :nodeType="NodeType.OBJECT"
        browserRoot="/content/objects"
        currentPath="/content/objects">
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
