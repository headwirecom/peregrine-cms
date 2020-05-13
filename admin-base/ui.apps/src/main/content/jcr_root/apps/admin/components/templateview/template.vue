<template>
  <admin-components-explorerpreviewcontent
      :key="getBasePath()"
      :model="model"
      :tab="tab"
      :is-edit="isEditPage"
      :nodeType="NodeType.TEMPLATE"
      :browserRoot="`${getBasePath()}/templates`"
      :currentPath="`${getBasePath()}/templates`">
    <admin-components-componentexplorer
        v-if="isEditPage"
        :model="{text: 'Components', source: '/admin/components'}"/>
  </admin-components-explorerpreviewcontent>
</template>

<script>
  import {NodeType} from '../../../../../../js/constants'

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
