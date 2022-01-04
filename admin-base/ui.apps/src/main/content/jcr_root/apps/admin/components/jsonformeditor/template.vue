<template>
  <div class="file-editor">
    <iframe src="/content/admin/jsonformeditor/index.html" width="100%" height="100%" border="0"></iframe>
  </div>
</template>

<script>

export default {
  props: ['model']
};
</script>

<style scoped>
.file-editor {
  width: calc(100% - 340px) !important;
  margin: 0;
  border: 1px solid #607d8b;
  padding: 0;
  border-radius: 3px;
  max-height: 100%;
  overflow: auto;
}
</style>
