<template>
  <div class="toolbar">
    <button class="btn" :title="$i18n('clear')">
      <i class="material-icons" @click="clear">clear</i>
    </button>
    <button class="btn" :title="$i18n('undo')">
      <i class="material-icons" @click="undo">undo</i>
    </button>
    <button class="btn" :title="$i18n('redo')">
      <i class="material-icons" @click="redo">redo</i>
    </button>
    <button class="btn" :title="$i18n('remove format')">
      <i class="material-icons" @click="removeFormat">format_clear</i>
    </button>
    <button class="btn" :title="$i18n('bold')">
      <i class="material-icons" @click="bold">format_bold</i>
    </button>
    <button class="btn" :title="$i18n('italic')">
      <i class="material-icons" @click="italic">format_italic</i>
    </button>
    <button class="btn" :title="$i18n('underline')">
      <i class="material-icons" @click="underline">format_underline</i>
    </button>
    <button class="btn" :title="$i18n('align left')">
      <i class="material-icons" @click="alignLeft">format_align_left</i>
    </button>
    <button class="btn" :title="$i18n('align center')">
      <i class="material-icons" @click="alignCenter">format_align_center</i>
    </button>
    <button class="btn" :title="$i18n('align right')">
      <i class="material-icons" @click="alignRight">format_align_right</i>
    </button>
    <button class="btn" :title="$i18n('numbered list')">
      <i class="material-icons" @click="numberedList">format_list_numbered</i>
    </button>
    <button class="btn" :title="$i18n('bulleted list')">
      <i class="material-icons" @click="bulletedList">format_list_bulleted</i>
    </button>
    <button class="btn" :title="$i18n('quote')">
      <i class="material-icons" @click="quote">format_quote</i>
    </button>
    <button class="btn" :title="$i18n('indent')">
      <i class="material-icons" @click="indent">format_indent_increase</i>
    </button>
    <button class="btn" :title="$i18n('outdent')">
      <i class="material-icons" @click="outdent">format_indent_decrease</i>
    </button>
    <button class="btn" :title="$i18n('insert link')">
      <i class="material-icons" @click="link">insert_link</i>
    </button>
    <button class="btn" :title="$i18n('cut')">
      <i class="material-icons" @click="cut">content_cut</i>
    </button>
    <button class="btn" :title="$i18n('copy')">
      <i class="material-icons" @click="copy">content_copy</i>
    </button>
    <button class="btn" :title="$i18n('paste')">
      <i class="material-icons" @click="paste">content_paste</i>
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
      undo() {
        this.exec('undo')
      },
      redo() {
        this.exec('redo')
      },
      removeFormat() {
        this.exec('removeFormat')
      },
      bold() {
        this.exec('bold')
      },
      italic() {
        this.exec('italic')
      },
      underline() {
        this.exec('underline')
      },
      alignLeft() {
        this.exec('justifyLeft')
      },
      alignCenter() {
        this.exec('justifyCenter')
      },
      alignRight() {
        this.exec('justifyRight')
      },
      numberedList() {
        this.exec('insertOrderedList')
      },
      bulletedList() {
        this.exec('insertUnorderedList')
      },
      quote() {
        this.exec('formatBlock')
      },
      indent() {
        this.exec('indent')
      },
      outdent() {
        this.exec('outdent')
      },
      link() {
        const uri = prompt('Insert your link')
        this.exec('createLink', uri)
      },
      cut() {
        this.exec('cut')
      },
      copy() {
        this.exec('copy')
      },
      paste() {
        this.exec('paste')
      }
    }
  }
</script>
