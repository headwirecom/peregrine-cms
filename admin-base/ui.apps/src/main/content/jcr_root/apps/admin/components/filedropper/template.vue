<template>
  <div class="file-dropper"></div>
</template>

<script>
export default {
  name: 'FileDropper',
  props: {
    dropContext: {
      default() {
        return window
      },
      validator(val) {
        return !!val.addEventListener && !!val.removeEventListener
      }
    }
  },
  created() {
    this.dropContext.addEventListener('dragover', this.onDragOver)
    this.dropContext.addEventListener('dragenter', this.onDragEnter)
    this.dropContext.addEventListener('dragleave', this.onDragLeave)
    this.dropContext.addEventListener('drop', this.onDrop)
  },
  beforeDestroy() {
    this.dropContext.removeEventListener('dragover', this.onDragOver)
    this.dropContext.removeEventListener('dragenter', this.onDragEnter)
    this.dropContext.removeEventListener('dragleave', this.onDragLeave)
    this.dropContext.removeEventListener('drop', this.onDrop)
  },
  methods: {
    onDragOver(event) {
      event.preventDefault()
      console.log('filedropper: onDragOver')
    },
    onDragEnter(event) {
      event.preventDefault()
      console.log('filedropper: onDragEnter')
    },
    onDragLeave(event) {
      event.preventDefault()
      console.log('filedropper: onDragLeave')
    },
    onDrop(event) {
      event.preventDefault()
      console.log('filedropper: onDrop')
    },
    uploadFiles(files) {
      $perAdminApp.stateAction('uploadFiles', {
        path: $perAdminApp.getView().state.tools.assets,
        files: files,
        cb: this.setUploadProgress
      })
    },
  }
}
</script>
