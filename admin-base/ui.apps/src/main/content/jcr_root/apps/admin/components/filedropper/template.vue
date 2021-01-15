<template>
  <div v-if="showMask" class="file-dropper file-upload" @click.prevent.stop="() => {}">
    <div class="file-upload-inner">
      <i class="material-icons">file_download</i>
      <span class="file-upload-text">Drag &amp; Drop files anywhere</span>
      <div class="progress-bar">
        <div class="progress-bar-value" :style="`width: ${progress}%`"></div>
      </div>
      <div class="progress-text">{{ progress }}%</div>
      <div class="file-upload-action">
        <button
            type="button"
            class="btn"
            :disabled="progress < 100"
            @click.prevent.stop="onOkClick">
          ok
        </button>
      </div>
    </div>
  </div>
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
    },
    path: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      showMask: false,
      progress: 0,
      files: {
        uploaded: []
      }
    }
  },
  watch: {
    progress(val) {
      if (val === 100) {
        this.onUploadDone()
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
    },
    onDragEnter(event) {
      event.preventDefault()
      this.showMask = true
    },
    onDragLeave(event) {
      event.preventDefault()
    },
    onDrop(event) {
      event.preventDefault()
      this.upload(event.dataTransfer.files)
    },
    upload(files) {
      this.files.uploaded = []
      $perAdminApp.stateAction('uploadFiles', {
        path: this.path,
        files: files,
        cb: this.setProgress
      }).then((data) => {
        $perAdminApp.getApi().populateNodesForBrowser(this.path, 'pathBrowser')
      })
      for (let i = 0; i < files.length; i++) {
        this.files.uploaded.push(files[i])
      }
    },
    setProgress(percentCompleted) {
      this.progress = percentCompleted
    },
    onUploadDone() {
      $perAdminApp.getApi().populateNodesForBrowser(this.path, 'pathBrowser')
      this.$emit('upload-done', this.files.uploaded)
    },
    onOkClick() {
      this.showMask = false
      this.progress = 0
    }
  }
}
</script>
