<template>
  <div class="toolbar">
    <button class="btn" :title="$i18n('undo')">
      <i class="material-icons" @click="exec('undo')">undo</i>
    </button>
    <button class="btn" :title="$i18n('redo')">
      <i class="material-icons" @click="exec('redo')">redo</i>
    </button>
    <admin-components-materializedropdown
        :below-origin="true"
        :items="formattingItems">
      <button class="btn" :title="$i18n('remove format')">
        <i class="material-icons">text_format</i><span class="caret-down"></span>
      </button>
    </admin-components-materializedropdown>
    <button class="btn" :title="$i18n('bold')">
      <i class="material-icons" @click="exec('bold')">format_bold</i>
    </button>
    <button class="btn" :title="$i18n('italic')">
      <i class="material-icons" @click="exec('italic')">format_italic</i>
    </button>
    <button class="btn" :title="$i18n('superscript')">
      <i class="material-icons" @click="exec('superscript')">format_superscript</i>
    </button>
    <button class="btn" :title="$i18n('subscript')">
      <i class="material-icons" @click="exec('subscript')">format_subscript</i>
    </button>
    <admin-components-materializedropdown
        :below-origin="true"
        :items="linkItems">
      <button class="btn" :title="$i18n('insert link')">
        <i class="material-icons">link</i><span class="caret-down"></span>
      </button>
    </admin-components-materializedropdown>
    <button class="btn" :title="$i18n('insert image')">
      <i class="material-icons" @click="insertImage">insert_photo</i>
    </button>
    <button class="btn" :title="$i18n('align left')">
      <i class="material-icons" @click="exec('justifyLeft')">format_align_left</i>
    </button>
    <button class="btn" :title="$i18n('align center')">
      <i class="material-icons" @click="exec('justifyCenter')">format_align_center</i>
    </button>
    <button class="btn" :title="$i18n('align right')">
      <i class="material-icons" @click="exec('justifyRight')">format_align_right</i>
    </button>
    <button class="btn" :title="$i18n('align right')">
      <i class="material-icons" @click="exec('justifyFull')">format_align_justify</i>
    </button>
    <button class="btn" :title="$i18n('numbered list')">
      <i class="material-icons" @click="exec('insertOrderedList')">format_list_numbered</i>
    </button>
    <button class="btn" :title="$i18n('bulleted list')">
      <i class="material-icons" @click="exec('insertUnorderedList')">format_list_bulleted</i>
    </button>
    <button class="btn" :title="$i18n('quote')">
      <i class="material-icons" @click="exec('formatBlock')">format_quote</i>
    </button>
    <button class="btn" :title="$i18n('remove format')">
      <i class="material-icons" @click="exec('removeFormat')">format_clear</i>
    </button>
  </div>
</template>

<script>
  export default {
    name: 'RichToolbar',
    computed: {
      view() {
        return $perAdminApp.getView()
      },
      formattingItems() {
        const headlines = []
        for (let i = 1; i <= 6; i++) {
          headlines.push({
            label: `${this.$i18n('headline')} ${i}`,
            icon: 'title',
            click: () => this.exec('formatBlock', `h${i}`)
          })
        }
        return [
          {
            label: this.$i18n('Paragraph'),
            icon: 'format_textdirection_l_to_r',
            click: () => this.exec('formatBlock', 'p')
          },
          ...headlines
        ]
      },
      linkItems() {
        return [
          {
            label: this.$i18n('insert link'),
            icon: 'add',
            click: this.link
          },
          {
            label: this.$i18n('unlink'),
            icon: 'clear',
            click: () => this.exec('unlink')
          }
        ]
      }
    },
    methods: {
      getInlineDoc() {
        if (this.view && this.view.state && this.view.state.inline) {
          return this.view.state.inline.doc
        }
        return null
      },
      exec(cmd, value = null, showUi = false) {
        this.getInlineDoc().execCommand(cmd, showUi, value)
      },
      clear() {
        if (confirm('Are you sure you want to clear all content?')) {
          this.exec('selectAll')
          this.exec('delete')
        }
      },
      link() {
        const uri = prompt('Provide link')
        this.exec('createLink', uri)
      },
      unlink() {
        this.exec('unlink')
      },
      insertImage() {
        const imgUri = prompt('provide image link')
        this.exec('insertImage', imgUri)
      }
    }
  }
</script>
