<template>
  <div class="file-editor">
    <iframe
        v-bind:src="`/content/admin/jsonformeditor/index.html?sitename=${tenantName}&objectname=${objectDefinitionName}`"
        width="100%"
        height="100%"
        border="0">
    </iframe>
  </div>
</template>

<script>

export default {
  props: ['model'],
  data() {
    return {
      tenantName: '',
      objectDefinitionName: ''
    }
  },
  mounted() {
    const path = this.getPath()
    const segments = path.split('/')
    console.log(`# of segments: ${segments.length}, segments: ${segments}`)
    if(segments.length >= 5) {
      this.tenantName = segments[2]
      this.objectDefinitionName = segments[4];
    }
    console.log(`Tenant Name: ${this.tenantName}, Object Definition Name: ${this.objectDefinitionName}`)
  },
  methods: {
    getPath(){
      if( this.$root.$data.pageView){
        if( this.$root.$data.pageView.path ){
          return this.$root.$data.pageView.path;
        } else {
          return '';
        }
      } else {
        return '';
      }
    },
  }
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
