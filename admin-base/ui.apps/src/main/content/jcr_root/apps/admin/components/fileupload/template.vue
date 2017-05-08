<template>
    <form 
      id="file_upload" 
      method="post" 
      action="" 
      enctype="multipart/form-data"
      v-bind:class="dragStateClass"
      v-on:submit.prevent    ="uploadFile"
      v-on:drag.prevent      ="stopPropagation"
      v-on:dragstart.prevent ="stopPropagation"
      v-on:dragover.prevent  ="addDragOverClass"
      v-on:dragenter.prevent ="addDragOverClass"
      v-on:dragleave.prevent ="removeDragOverClass"
      v-on:dragend.prevent   ="removeDragOverClass"
      v-on:drop.prevent      ="onDropFile">
      <div class="file-upload-inner">
        <template v-if="uploadProgress">
          <div v-if="uploadStatus" class="file-upload-status">
            <i class="material-icons" v-on:click="hideUploadStatus">clear</i>
            {{uploadStatus}}
          </div>
          <progress class="file-upload-progress" v-bind:value="uploadProgress" max="100"></progress>
        </template>
        <template v-else>
          <i class="material-icons">file_download</i>
          <span class="file-upload-text">Drag files anywhere</span>
        </template>
      </div>
    </form>
</template>

<script>
export default {
  props: ['model'],
  data() {
    return {
      dragStateClass: '',
      uploadProgress: 0,
      uploadStatus: false,
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
        files: files,
        cb: this.setPercentCompleted
      })    
    },
    setPercentCompleted(percentCompleted){
      console.log('percentCompleted: ', percentCompleted)
      this.uploadProgress = percentCompleted 
      if(percentCompleted === 100){
        this.showUploadStatus('Success! File uploaded.')
      }
    },
    showUploadStatus(msg){
      this.uploadStatus = msg
    },
    hideUploadStatus(){
      this.uploadStatus = false
      this.uploadProgress = 0
    }
  }
}

</script>
