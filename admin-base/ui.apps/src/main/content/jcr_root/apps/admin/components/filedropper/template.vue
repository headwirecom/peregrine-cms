<template>
  <div v-if="showMask"
       class="file-dropper file-upload"
       :class="{dragging}"
       @click.prevent.stop="() => {}"
       @dragover.prevent="dragging = true"
       @dragenter.stop.prevent="dragging = true"
       @dragleave.prevent="dragging = false"
       @drop.prevent="onDrop">
    <div class="file-upload-inner">
      <icon icon="file_download"/>
      <span class="file-upload-text">Drag &amp; Drop files anywhere</span>
      <div class="progress-bar">
        <div class="progress-bar-value" :style="`width: ${progress}%`"></div>
      </div>
      <div class="progress-text">{{ progress }}%</div>
      <div class="file-upload-action">
        <button
            type="button"
            class="btn"
            :disabled="progress < 100 && progress !== 0"
            @click.prevent.stop="onOkClick">
          ok
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import Icon from '../icon/template.vue'

export default {
  name: 'FileDropper',
  components: {
    Icon
  },
  emits: ['upload-done'],
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
      },
      dragging: false
    }
  },
  mounted() {
    this.$nextTick(() => { //ensure that this.dropContext element is present
      this.dropContext.addEventListener('dragenter', this.onDropContextDragEnter)
    })
  },
  beforeDestroy() {
    this.dropContext.removeEventListener('dragenter', this.onDropContextDragEnter)
  },
  methods: {
    onDropContextDragEnter(event) {
      event.preventDefault()
      this.showMask = true
    },
    onDrop(event) {
      this.upload(event.dataTransfer.files)
      this.dragging = false
    },
    upload(files) {
      this.files.uploaded = []
      $perAdminApp.stateAction('uploadFiles', {
        path: this.path,
        files: files,
        cb: this.setProgress
      }).then((data) => {
        $perAdminApp.getApi().populateNodesForBrowser(this.path, 'pathBrowser')
        for (let i = 0; i < files.length; i++) {
          this.files.uploaded.push(files[i])
        }
        this.$emit('upload-done', this.files.uploaded)
      })
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