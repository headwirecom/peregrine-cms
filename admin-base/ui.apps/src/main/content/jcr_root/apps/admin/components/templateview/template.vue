<template>
  <admin-components-explorerpreviewcontent
      :model="model"
      nodeType="template"
      browserRoot="/content/templates"
      currentPath="/content/templates">
  </admin-components-explorerpreviewcontent>
</template>

<script>
  export default {
    props: ['model']
  }
</script>
<style scoped>
</style>
