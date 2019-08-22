<template>
  <admin-components-explorerpreviewcontent
      :model="model"
      nodeType="page"
      browserRoot="/content/sites"
      currentPath="/content/sites">
  </admin-components-explorerpreviewcontent>
</template>

<script>
  export default {
    props: ['model']
  }
</script>
<style scoped>
</style>
