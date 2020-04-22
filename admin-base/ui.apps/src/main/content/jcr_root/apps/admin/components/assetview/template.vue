<template>
  <admin-components-explorerpreviewcontent
      :model="model"
      :nodeType="NodeType.ASSET"
      :browserRoot="`${getBasePath()}/assets`"
      :currentPath="`${getBasePath()}/assets`">
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
    },
    methods: {
      getBasePath() {
        const view = $perAdminApp.getView()
        let tenant = { name: 'example' }
        if (view.state.tenant) {
          tenant = view.state.tenant
        }
        return `/content/${tenant.name}`
      }
    }
  };
</script>
<style scoped>
</style>
