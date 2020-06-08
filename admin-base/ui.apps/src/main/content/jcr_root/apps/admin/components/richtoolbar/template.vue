<template>
  <div class="toolbar" :class="{disabled: !inlineRich}">
    <admin-components-richtoolbarbtn
        v-for="(btn, i) in buttons"
        :key="getButtonKey(btn, i)"
        :items="btn.items"
        :icon="btn.icon"
        :class="btn.class"
        :title="$i18n(btn.title)"
        :active="btn.isActive()"
        @click="exec(btn.cmd)"/>

    <admin-components-pathbrowser
        v-if="browser.open"
        :isOpen="browser.open"
        :header="browser.header"
        :browserRoot="browser.root"
        :browserType="browser.type"
        :withLinkTab="browser.withLinkTab"
        :newWindow="browser.newWindow"
        :toggleNewWindow="toggleBrowserNewWindow"
        :linkTitle="browser.linkTitle"
        :setLinkTitle="browser.setLinkTitle"
        :currentPath="browser.path.current"
        :setCurrentPath="setBrowserPathCurrent"
        :selectedPath="browser.path.selected"
        :setSelectedPath="setBrowserPathSelected"
        :onCancel="onBrowserCancel"
        :onSelect="onBrowserSelect"/>
  </div>
</template>

<script>
  import {get, set} from '../../../../../../js/utils'

  export default {
    name: 'RichToolbar',
    data() {
      return {
        key: 0,
        browser: {
          open: false,
          cmd: null,
          header: '',
          root: '',
          type: 'image',
          withLinkTab: false,
          newWindow: false,
          linkTitle: 'TODO - SET TITLE',
          setLinkTitle: () => {
          },
          path: {
            current: '',
            selected: null,
          }
        }
      }
    },
    computed: {
      buttons() {
        const buttons = [
          {title: 'undo', icon: 'undo', cmd: 'undo'},
          {title: 'redo', icon: 'redo', cmd: 'redo'},
          {
            title: 'text format',
            icon: 'text_format',
            items: this.formattingItems,
            isActive: this.formattingIsActive
          },
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
            isActive: () => this.itemIsTag('A')
          },
          {title: 'insert image', icon: 'insert_photo', cmd: 'insertImage'},
          {
            title: 'align left',
            icon: 'format_align_left',
            cmd: 'justifyLeft',
            isActive: () => this.queryCmdState('justifyLeft')
          },
          {
            title: 'align center',
            icon: 'format_align_center',
            cmd: 'justifyCenter',
            isActive: () => this.queryCmdState('justifyCenter')
          },
          {
            title: 'align right',
            icon: 'format_align_right',
            cmd: 'justifyRight',
            isActive: () => this.queryCmdState('justifyRight')
          },
          {
            title: 'justify',
            icon: 'format_align_justify',
            cmd: 'justifyFull',
            isActive: () => this.queryCmdState('justifyFull')
          },
          {
            title: 'numbered list',
            icon: 'format_list_numbered',
            cmd: 'insertOrderedList',
            isActive: () => this.queryCmdState('insertOrderedList')
          },
          {
            title: 'bulleted list',
            icon: 'format_list_bulleted',
            cmd: 'insertUnorderedList',
            isActive: () => this.queryCmdState('insertUnorderedList')
          },
          {
            title: 'quote',
            icon: 'format_quote',
            cmd: 'quote',
            isActive: () => this.itemIsTag('PRE')
          },
          {
            title: 'remove format',
            icon: 'format_clear',
            cmd: 'removeFormat'
          },
          {
            title: 'change viewport',
            icon: this.viewportIcon,
            items: this.viewportItems,
            class: 'separate always-active'
          },
          {
            title: 'preview',
            icon: 'visibility',
            cmd: 'preview',
            class: 'separate always-active',
            isActive: () => this.preview === 'preview'
          }
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
      inlineRich() {
        if (!this.inline) return null
        return this.inline.rich
      },
      viewport() {
        return $perAdminApp.getNodeFromViewOrNull('/state/tools/workspace/view')
      },
      preview() {
        return $perAdminApp.getNodeFromViewOrNull('/state/tools/workspace/preview')
      },
      roots() {
        return $perAdminApp.getNodeFromViewOrNull('/state/tenant/roots')
      },
      specialCases() {
        return {
          link: this.link,
          insertImage: this.insertImage,
          quote: this.quote,
          preview: this.togglePreview
        }
      },
      formattingItems() {
        const headlines = []
        for (let i = 1; i <= 6; i++) {
          headlines.push({
            label: `${this.$i18n('headline')} ${i}`,
            icon: 'title',
            class: () => this.itemIsTag(`H${i}`) ? 'active' : null,
            click: () => this.exec('formatBlock', `h${i}`),
          })
        }
        return [
          {
            label: this.$i18n('paragraph'),
            icon: 'format_textdirection_l_to_r',
            class: () => this.itemIsTag('P') ? 'active' : null,
            click: () => this.exec('formatBlock', 'p')
          },
          ...headlines
        ]
      },
      viewportItems() {
        return [
          {
            id: 'mobile',
            label: this.$i18n('mobile'),
            icon: 'phone_android',
            class: () => this.viewport === 'mobile' ? 'active' : null,
            click: () => this.setViewport('mobile')
          },
          {
            id: 'mobile-landscape',
            label: this.$i18n('mobile-landscape'),
            icon: 'stay_current_landscape',
            class: () => this.viewport === 'mobile-landscape' ? 'active' : null,
            click: () => this.setViewport('mobile-landscape')
          },
          {
            id: 'tablet',
            label: this.$i18n('tablet'),
            icon: 'tablet_android',
            class: () => this.viewport === 'tablet' ? 'active' : null,
            click: () => this.setViewport('tablet')
          },
          {
            id: 'tablet-landscape',
            label: this.$i18n('tablet-landscape'),
            icon: 'tablet',
            class: () => this.viewport === 'tablet-landscape' ? 'active' : null,
            click: () => this.setViewport('tablet-landscape')
          },
          {
            id: 'laptop',
            label: this.$i18n('laptop'),
            icon: 'laptop_windows',
            class: () => this.viewport === 'laptop' ? 'active' : null,
            click: () => this.setViewport('laptop')
          },
          {
            id: 'desktop',
            label: this.$i18n('desktop'),
            icon: 'desktop_windows',
            class: () => !this.viewport || this.viewport === 'desktop' ? 'active' : null,
            click: () => this.setViewport('desktop')
          }
        ]
      },
      viewportIcon() {
        let currentItem = {}
        this.viewportItems.some((item) => {
          if (item.id === this.viewport) {
            return currentItem = item
          }
        })
        return currentItem.icon || 'desktop_windows'
      }
    },
    watch: {
      'inline.ping'(val) {
        if (val) {
          this.key++
          set($perAdminApp.getView(), '/state/inline/ping', false)
        }
      }
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
        this.key++
      },
      link() {
        if (this.itemIsTag('A')) {
          this.execCmd('unlink')
        } else {
          const uri = prompt('Provide link')
          if (uri && uri.length >= 11) { // e.g. http://a.de (11 symbols)
            this.execCmd('createLink', uri)
          }
        }
      },
      insertImage() {
        this.browser.cmd = 'insertImage'
        this.browser.header = this.$i18n('Insert Image')
        this.browser.path.current = this.roots.assets
        this.startBrowsing()
      },
      quote() {
        this.execCmd('formatBlock,', 'pre')
      },
      setViewport(viewport) {
        set($perAdminApp.getView(), '/state/tools/workspace/view', viewport)
      },
      togglePreview() {
        const view = $perAdminApp.getView()
        const current = get(view, '/state/tools/workspace/preview', null)
        $perAdminApp.stateAction('editPreview', current ? null : 'preview')
      },
      getButtonKey(btn, index) {
        let key = `btn-${index}-${btn.title}`
        if (btn.isActive() !== null) {
          key += `-${this.key}`
        }
        return key
      },
      itemIsTag(tagName) {
        const document = this.getInlineDoc()
        tagName = tagName.toUpperCase()
        if (!document || !document.defaultView) return false
        const window = document.defaultView
        let selection = window.getSelection()
        if (!selection || selection.rangeCount <= 0) return false
        selection = selection.getRangeAt(0)
        if (selection) {
          return selection.startContainer.parentNode.tagName === tagName
              || selection.endContainer.parentNode.tagName === tagName
        } else {
          return false
        }
      },
      formattingIsActive() {
        const headlines = []
        for (let i = 1; i <= 6; i++) {
          headlines.push(`H${i}`)
        }
        const tags = ['P', ...headlines]
        return tags.some((tag) => this.itemIsTag(tag))
      },
      startBrowsing() {
        $perAdminApp.getApi()
            .populateNodesForBrowser(this.browser.path.current, 'pathBrowser')
            .then(() => this.browser.open = true)
            .catch((err) => {
              $perAdminApp.getApi().populateNodesForBrowser('/content', 'pathBrowser')
            })
      },
      onBrowserCancel() {
        this.browser.open = false
      },
      onBrowserSelect() {
        this.browser.open = false
        console.log('execCmd: ', this.browser.cmd, this.browser.path.selected)
        this.execCmd(this.browser.cmd, this.browser.path.selected)
        this.browser.cmd = null
        this.browser.selected = null
      },
      setBrowserPathCurrent(path) {
        this.browser.path.current = path
      },
      setBrowserPathSelected(path) {
        this.browser.path.selected = path
      },
      toggleBrowserNewWindow() {
        this.browser.newWindow = !this.browser.newWindow
      }
    }
  }
</script>
