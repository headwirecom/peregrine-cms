<template>
  <admin-components-explorerpreviewcontent
      :key="getBasePath()"
      :model="model"
      :tab="tab"
      :is-edit="isEditPage"
      :nodeType="NodeType.PAGE"
      :browserRoot="`${getBasePath()}/pages`"
      :currentPath="`${getBasePath()}/pages`">
    <admin-components-componentexplorer
        v-if="isEditPage"
        :model="{text: 'Components', source: '/admin/components'}"/>
  </admin-components-explorerpreviewcontent>
</template>

<script>
  import {NodeType} from '../../../../../../js/constants'
  import {set} from '../../../../../../js/utils';

  export default {
    props: ['model'],
    data() {
      return {
        NodeType: NodeType
      }
    },
    computed: {
      isEditPage() {
        return this.model.path === '/jcr:content/workspace/right-panel'
      },
      tab() {
        return this.isEditPage? 'components' : 'info'
      }
    },
    beforeMount(){
      set($perAdminApp.getView(), '/state/rightPanelFullscreen', false)
      set($perAdminApp.getView(), '/state/rightPanelVisible', true)
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
  }
</script>
