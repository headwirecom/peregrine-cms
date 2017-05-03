<template>
    <form 
      id="file_upload" 
      method="post" 
      action="" 
      enctype="multipart/form-data"
      v-bind:class="dragStateClass"
      v-on:submit.prevent="uploadFile"
      v-on:drag.prevent="stopPropagation"
      v-on:dragstart.prevent="stopPropagation"
      v-on:dragover.prevent="addDragOverClass"
      v-on:dragenter.prevent="addDragOverClass"
      v-on:dragleave.prevent="removeDragOverClass"
      v-on:dragend.prevent="removeDragOverClass"
      v-on:drop.prevent="onDropFile">
        <div class="file-upload-inner">
          <i class="material-icons">file_download</i>
          <span class="file-upload-text">Drag files here</span>
        </div>
    </form>
</template>

<script>
export default {
  props: ['model'],
  data() {
    return {
      dragStateClass: '',
      formModel: { file: '' },
      schema: { 
        fields: [
          {
            type: "input",
            inputType: "file",
            label: "File",
            model: 'file',
            required: false,
            placeholder: "upload file"
          },
          {
            type: "submit",
            buttonText: "upload",
            onSubmit: this.uploadFile,
            model: "upload"
          }
        ]
      },
      formOptions: {
        validateAfterLoad: true,
        validateAfterChanged: true
      }
    }
  }, 
  methods: {
    addDragOverClass(ev){
      ev.stopPropagation()
      this.dragStateClass = 'is-dragover'
    },
    removeDragOverClass(ev){
      ev.stopPropagation()
      this.dragStateClass = ''
    },
    onDropFile(ev){
      ev.stopPropagation()
      this.dragStateClass = ''
      this.uploadFile(ev.dataTransfer.files)
    },
    stopPropagation(ev){
      ev.stopPropagation()
    },
    uploadFile(files) {
      $perAdminApp.stateAction('uploadFiles', { 
        path: $perAdminApp.getView().state.tools.assets, 
        files: files
      })
    }
  }
}

</script>
