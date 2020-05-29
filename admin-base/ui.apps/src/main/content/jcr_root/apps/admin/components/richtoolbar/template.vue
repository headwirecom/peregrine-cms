<template>
  <div class="toolbar">
    <admin-components-richtoolbarbtn
        v-for="(btn, i) in buttons"
        :key="getButtonKey(btn, i)"
        :items="btn.items"
        :icon="btn.icon"
        :title="$i18n(btn.title) + (btn.isActive())"
        :active="btn.isActive()"
        @click="exec(btn.cmd)">
    </admin-components-richtoolbarbtn>
  </div>
</template>

<script>
  import {itemIsLink, set} from '../../../../../../js/utils'

  export default {
    name: 'RichToolbar',
    data() {
      return {
        key: 0
      }
    },
    computed: {
      buttons() {
        const buttons = [
          {title: 'undo', icon: 'undo', cmd: 'undo'},
          {title: 'redo', icon: 'redo', cmd: 'redo'},
          {title: 'text format', icon: 'text_format', items: this.formattingItems},
          {
            title: 'bold',
            icon: 'format_bold',
            cmd: 'bold',
            isActive: () => this.queryCmdState('bold')
          },
          {
            title: 'italic',
            icon: 'format_italic',
            cmd: 'italic',
            isActive: () => this.queryCmdState('italic')
          },
          {
            title: 'superscript',
            icon: 'arrow_upward',
            cmd: 'superscript',
            isActive: () => this.queryCmdState('superscript')
          },
          {
            title: 'subscript',
            icon: 'arrow_downward',
            cmd: 'subscript',
            isActive: () => this.queryCmdState('subscript')
          },
          {
            title: 'insert link',
            icon: 'link',
            cmd: 'link',
            isActive: () => itemIsLink(this.getInlineDoc())
          },
          {title: 'insert image', icon: 'insert_photo', cmd: 'insertImage'},
          {title: 'align left', icon: 'format_align_left', cmd: 'justifyLeft'},
          {title: 'align center', icon: 'format_align_center', cmd: 'justifyCenter'},
          {title: 'align right', icon: 'format_align_right', cmd: 'justifyRight'},
          {title: 'justify', icon: 'format_align_justify', cmd: 'justifyFull'},
          {title: 'numbered list', icon: 'format_list_numbered', cmd: 'insertOrderedList'},
          {title: 'bulleted list', icon: 'format_list_bulleted', cmd: 'insertUnorderedList'},
          {title: 'quote', icon: 'format_quote', cmd: 'quote'},
          {title: 'remove format', icon: 'format_clear', cmd: 'removeFormat'}
        ]
        buttons.forEach((btn) => {
          if (!btn.isActive) {
            btn.isActive = () => null
          }
        })
        return buttons
      },
      inline() {
        if (!$perAdminApp.getView() || !$perAdminApp.getView().state) return null
        return $perAdminApp.getView().state.inline
      },
      specialCases() {
        return {
          link: this.link,
          insertImage: this.insertImage,
          quote: this.quote
        }
      },
      formattingItems() {
        const headlines = []
        for (let i = 1; i <= 6; i++) {
          headlines.push({
            label: `${this.$i18n('headline')} ${i}`,
            icon: 'title',
            click: () => {
              this.exec('formatBlock', `h${i}`)
            }
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
      }
    },
    watch: {
      'inline.ping'(val) {
        if (val) {
          this.key++
          set($perAdminApp.getView(), '/state/inline/ping', false)
        }
        console.log('inline.ping', val)
      }
    },
    mounted() {
      set($perAdminApp.getView(), '/state/inline/rich', false)
    },
    methods: {
      getInlineDoc() {
        if (!this.inline) return null
        return this.inline.doc
      },
      execCmd(cmd, value = null, showUi = false) {
        if (!this.getInlineDoc() || !this.getInlineDoc().execCommand) return
        this.getInlineDoc().execCommand(cmd, showUi, value)
      },
      queryCmdState(cmd) {
        if (!this.getInlineDoc() || !this.getInlineDoc().queryCommandState) return
        return this.getInlineDoc().queryCommandState(cmd) || false
      },
      exec(cmd, value = null) {
        if (Object.keys(this.specialCases).indexOf(cmd) >= 0) {
          this.specialCases[cmd]()
        } else {
          this.execCmd(cmd, value)
        }
      },
      link() {
        const uri = prompt('Provide link')
        if (uri && uri.length >= 11) { // e.g. http://a.de (11 symbols)
          this.execCmd('createLink', uri)
        }
      },
      insertImage() {
        const imgUri = prompt('provide image link')
        if (imgUri && imgUri.length >= 11) { // e.g. http://a.de (11 symbols)
          this.execCmd('insertImage', imgUri)
        }
      },
      quote() {
        this.execCmd('formatBlock,', 'pre')
      },
      getButtonKey(btn, index) {
        let key = `btn-${index}-${btn.title}`
        if (btn.isActive() !== null) {
          key += `-${this.key}`
        }
        return key
      }
    }
  }
</script>
