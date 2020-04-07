<template>
  <admin-components-explorerpreviewcontent
      :model="model"
      :nodeType="NodeType.PAGE"
      :browserRoot="`${getBasePath()}/pages`"
      :currentPath="`${getBasePath()}/pages`">
    <admin-components-componentexplorer
        v-if="isEditPage"
        :model="{text: 'Components', source: '/admin/components'}"/>
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
    computed: {
      isEditPage() {
        return this.model.path === '/jcr:content/workspace/right-panel'
      }
    },
    mounted() {
      console.log(this.model)
    },
    methods: {
      getBasePath() {
        const view = $perAdminApp.getView()
        let tenant = {name: 'example'}
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
