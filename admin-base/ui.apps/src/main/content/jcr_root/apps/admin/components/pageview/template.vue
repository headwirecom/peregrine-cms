<template>
  <admin-components-explorerpreviewcontent
      :key="getBasePath()"
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
      if (this.isEditPage) {
        this.$root.$emit('explorerpreviewcontent-tab-update', 'components')
      }
    },
    methods: {
      getBasePath() {
        const view = $perAdminApp.getView()
        let tenant = {name: 'themeclean'}
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
