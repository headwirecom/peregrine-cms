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
        let site = { name: 'example' }
        if (view.state.site) {
          site = view.state.site
        }
        return `/content/${site.name}`
      }
    }
  };
</script>
<style scoped>
</style>
