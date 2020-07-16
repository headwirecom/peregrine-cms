<template>
  <div class="toolbar" :class="{disabled: !inlineRich || preview === 'preview'}">
    <template v-for="(btnGroup, groupName) in btns">
      <div v-if="btnGroup.length > 0" :class="['btn-group', `group-${groupName}`]">
        <admin-components-richtoolbarbtn
            v-for="(btn, i) in btnGroup"
            :key="getButtonKey(btn, i)"
            :items="btn.items"
            :icon="btn.icon"
            :icon-lib="btn.iconLib"
            :label="btn.label"
            :class="btn.class"
            :title="$i18n(btn.title)"
            :active="btn.isActive()"
            @click="exec(btn.cmd)"/>
      </div>
    </template>

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
        :setLinkTitle="setBrowserLinkTitle"
        :currentPath="browser.path.current"
        :setCurrentPath="setBrowserPathCurrent"
        :selectedPath="browser.path.selected"
        :setSelectedPath="setBrowserPathSelected"
        :onCancel="onBrowserCancel"
        :onSelect="onBrowserSelect"/>
  </div>
</template>

<script>
  import {deepClone, get, restoreSelection, saveSelection, set} from '../../../../../../js/utils'
  import {PathBrowser} from '../../../../../../js/constants'

  export default {
    name: 'RichToolbar',
    props: {
      showViewportBtn: {
        type: Boolean,
        default: true
      },
      showPreviewBtn: {
        type: Boolean,
        default: true
      }
    },
    data() {
      return {
        key: 0,
        selection: {
          restore: false,
          buffer: null,
          doc: null,
          container: null,
          content: null
        },
        param: {
          cmd: null,
          value: null
        },
        browser: {
          open: false,
          header: '',
          root: '',
          type: 'image',
          withLinkTab: false,
          newWindow: false,
          linkTitle: '',
          path: {
            current: '',
            selected: null
          }
        }
      }
    },
    computed: {
      btns() {
        const btns = {
          alwaysActives: [],
          actions: [
            {title: 'undo', icon: 'undo', cmd: 'undo'},
            {title: 'redo', icon: 'repeat', cmd: 'redo'}
          ],
          textFormat: [
            {
              title: 'text format',
              icon: 'paragraph',
              items: this.formattingItems,
              isActive: this.formattingIsActive
            }
          ],
          boldItalic: [
            {
              title: 'bold',
              icon: 'bold',
              cmd: 'bold',
              isActive: () => this.queryCmdState('bold')
            },
            {
              title: 'italic',
              icon: 'italic',
              cmd: 'italic',
              isActive: () => this.queryCmdState('italic')
            }
          ],
          superSub: [
            {
              title: 'superscript',
              label: 'A<sup>2</sup>',
              cmd: 'superscript',
              isActive: () => this.itemIsTag('SUP')
            },
            {
              title: 'subscript',
              label: 'A<sub>2</sub>',
              cmd: 'subscript',
              isActive: () => this.itemIsTag('SUB')
            }
          ],
          link: [
            {
              title: () => this.itemIsTag('A') ? 'edit/remove link' : 'insert link',
              icon: 'link',
              cmd: 'link',
              items: () => this.itemIsTag('A') ? this.linkItems : null,
              isActive: () => this.itemIsTag('A')
            }
          ],
          image: [
            {title: 'insert image', icon: 'picture-o', cmd: 'insertImage'}
          ],
          align: [
            {
              title: 'align left',
              icon: 'align-left',
              cmd: 'justifyLeft',
              isActive: () => this.queryCmdState('justifyLeft')
            },
            {
              title: 'align center',
              icon: 'align-center',
              cmd: 'justifyCenter',
              isActive: () => this.queryCmdState('justifyCenter')
            },
            {
              title: 'align right',
              icon: 'align-right',
              cmd: 'justifyRight',
              isActive: () => this.queryCmdState('justifyRight')
            },
            {
              title: 'justify',
              icon: 'align-justify',
              cmd: 'justifyFull',
              isActive: () => this.queryCmdState('justifyFull')
            }
          ],
          list: [
            {
              title: 'numbered list',
              icon: 'list-ol',
              cmd: 'insertOrderedList',
              isActive: () => this.queryCmdState('insertOrderedList')
            },
            {
              title: 'bulleted list',
              icon: 'list-ul',
              cmd: 'insertUnorderedList',
              isActive: () => this.queryCmdState('insertUnorderedList')
            }
          ],
          removeFormat: [
            {
              title: 'remove format',
              icon: 'format_clear',
              iconLib: 'material-icons',
              cmd: 'removeFormat'
            }
          ]
        }
        if (this.showPreviewBtn) {
          btns.alwaysActives.push({
            title: 'preview',
            icon: 'visibility',
            iconLib: 'material-icons',
            cmd: 'preview',
            class: 'always-active separate',
            isActive: () => this.preview === 'preview'
          })
        }
        if (this.showViewportBtn) {
          btns.alwaysActives.push({
            title: 'change viewport',
            icon: this.viewportIcon,
            iconLib: 'material-icons',
            items: this.viewportItems,
            class: 'always-active separate'
          })
        }
        Object.keys(btns).forEach((group) => {
          btns[group].forEach((btn) => {
            if (!btn.isActive) {
              btn.isActive = () => null
            }
          })
        })

        return btns
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
          editImage: this.editImage,
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
      },
      linkItems() {
        return [
          {
            label: this.$i18n('edit link'),
            icon: 'pencil',
            iconLib: 'font-awesome',
            click: () => this.editLink()
          },
          {
            label: this.$i18n('remove link'),
            icon: 'chain-broken',
            iconLib: 'font-awesome',
            click: () => this.removeLink()
          }
        ]
      }
    },
    watch: {
      'inline.ping'(val) {
        if (!val || !val.includes(this._uid)) {
          this.key++
          const newVal = val ? deepClone(val) : []
          newVal.push(this._uid)
          set($perAdminApp.getView(), '/state/inline/ping', newVal)
        }
      },
      'inline.param.cmd'(val) {
        if (val) {
          set($perAdminApp.getView(), '/state/inline/param/cmd', null)
          const paramVal = get($perAdminApp.getView(), '/state/inline/param/val')
          this.exec(val, paramVal)
        }
      }
    },
    methods: {
      getInlineDoc() {
        if (!this.inline) return null
        return this.inline.doc
      },
      getInlineContainer() {
        if (!this.getInlineDoc()) return
        return this.getInlineDoc().querySelector('.inline-edit.inline-editing')
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
          this.specialCases[cmd](value)
        } else {
          this.execCmd(cmd, value)
        }
        this.key++
      },
      link() {
        if (!this.itemIsTag('A')) {
          this.insertLink()
        } else {
          this.removeLink()
        }
      },
      insertLink() {
        const selection = this.getSelection()
        if (!selection) throw 'no selection found'
        const range = selection.getRangeAt(0)
        if (!selection) throw 'no selection-range found'
        const len = range.endOffset - range.startOffset
        const start = range.startOffset
        const text = range.startContainer.textContent.substr(start, len)

        if (selection.anchorNode.parentNode.innerText === text) {
          this.selection.content = selection.anchorNode.parentNode.outerHTML
        } else {
          this.selection.content = text
        }

        this.param.cmd = 'createLink'
        this.browser.header = this.$i18n('Create Link')
        this.browser.path.current = this.roots.pages
        this.browser.withLinkTab = true
        this.browser.newWindow = false
        this.browser.type = PathBrowser.Type.PAGE
        this.saveSelection()
        this.selection.restore = true
        this.startBrowsing()
      },
      editLink() {
        let anchor
        const document = this.getInlineDoc()
        if (!document || !document.defaultView) return false
        const window = document.defaultView
        let selection = window.getSelection()
        if (!selection || selection.rangeCount <= 0) return false

        const range = document.createRange()
        range.setStart(selection.anchorNode, 0)
        range.setEnd(selection.anchorNode, selection.anchorNode.length)
        selection.removeAllRanges()
        selection.addRange(range)
        selection = selection.getRangeAt(0)

        if (selection.startContainer.parentNode.tagName === 'A') {
          anchor = selection.startContainer.parentNode
        } else if (selection.endContainer.parentNode.tagName === 'A') {
          anchor = selection.endContainer.parentNode
        }

        this.selection.content = anchor.innerHTML
        let href = anchor.getAttribute('href')
        const hrefArr = href.substr(0, href.length - 5).split('/')
        this.param.cmd = 'editLink'
        this.browser.header = this.$i18n('Edit Link')
        this.browser.path.selected = hrefArr.join('/')
        hrefArr.pop()
        this.browser.path.current = hrefArr.join('/')
        this.browser.withLinkTab = true
        this.browser.type = PathBrowser.Type.PAGE
        this.browser.path.suffix = '.html'
        this.saveSelection()
        this.selection.restore = true
        this.startBrowsing()
      },
      removeLink() {
        this.saveSelection()
        const document = this.getInlineDoc()
        if (!document || !document.defaultView) return false
        const window = document.defaultView
        let selection = window.getSelection()
        if (!selection || selection.rangeCount <= 0) return false

        const range = document.createRange()
        range.setStart(selection.anchorNode, 0)
        range.setEnd(selection.anchorNode, selection.anchorNode.length)
        selection.removeAllRanges()
        selection.addRange(range)
        this.execCmd('unlink')
        this.selection.container.focus()
        this.selection.doc.body.focus()
        this.$nextTick(() => {
          this.restoreSelection()
        })
      },
      insertImage() {
        this.param.cmd = 'insertImage'
        this.browser.header = this.$i18n('Insert Image')
        this.browser.path.current = this.roots.assets
        this.browser.withLinkTab = true
        this.browser.newWindow = undefined
        this.browser.type = PathBrowser.Type.ASSET
        this.browser.path.suffix = ''
        this.saveSelection()
        this.selection.restore = true
        this.startBrowsing()
      },
      editImage(target) {
        let src = target.getAttribute('src')
        const srcArr = src.split('/')
        this.param.cmd = 'editImage'
        this.browser.header = this.$i18n('Edit Image')
        this.browser.path.selected = srcArr.join('/')
        srcArr.pop()
        this.browser.path.current = srcArr.join('/')
        this.browser.withLinkTab = true
        this.browser.newWindow = undefined
        this.browser.type = PathBrowser.Type.ASSET
        this.startBrowsing()
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
        const selection = this.getSelection(0)
        if (selection) {
          const start = selection.startContainer
          const end = selection.endContainer
          return (start && start.parentNode.tagName === tagName)
              || (end && end.parentNode.tagName === tagName)
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
      saveSelection() {
        this.selection.buffer = saveSelection(this.getInlineContainer(), this.getInlineDoc())
        this.selection.doc = this.getInlineDoc()
        this.selection.container = this.getInlineContainer()
      },
      restoreSelection() {
        this.selection.doc.body.focus()
        this.selection.container.focus()
        this.$nextTick(() => {
          restoreSelection(this.selection.container, this.selection.buffer, this.selection.doc)
        })
      },
      onBrowserCancel() {
        this.browser.open = false
        this.restoreSelection()
      },
      onBrowserSelect() {
        this.browser.open = false

        if (this.selection.restore) {
          this.restoreSelection()
        }

        this.$nextTick(() => {
          if (['editLink', 'createLink'].includes(this.param.cmd)) {
            this.onLinkSelect()
          } else if (['insertImage', 'editImage'].includes(this.param.cmd)) {
            this.onImageSelect()
          }

          this.execCmd(this.param.cmd, this.param.value)
          this.param.cmd = null
          this.param.value = null
          this.browser.path.selected = null
          this.key++

          if (this.selection.restore) {
            this.$nextTick(() => {
              this.restoreSelection()
              this.selection.restore = false
            })
          }
        })
      },
      onLinkSelect() {
        if (this.browser.path.selected.startsWith('/')) {
          this.browser.path.selected += '.html'
        }
        this.param.cmd = 'insertHTML'
        this.param.value =
            `<a href="${this.browser.path.selected}"
                title="${this.browser.linkTitle}"
                target="${this.browser.newWindow ? '_blank' : '_self'}"
                >${this.selection.content}</a>`
      },
      onImageSelect() {
        if (this.param.cmd === 'editImage') {
          const imgEl = get($perAdminApp.getView(), '/state/inline/param/val')
          const linkTitle = this.browser.linkTitle
          imgEl.setAttribute('src', this.browser.path.selected)
          imgEl.setAttribute('alt', linkTitle? linkTitle : '')
          imgEl.setAttribute('title', linkTitle? linkTitle : '')
          $perAdminApp.action(this, 'reWrapEditable')
          $perAdminApp.action(this, 'writeInlineToModel')
        } else {
          this.param.cmd = 'insertHTML'
          this.param.value =
              `<img src="${this.browser.path.selected}"
                  alt="${this.browser.linkTitle}"
                  title="${this.browser.linkTitle}"/>`
        }
      },
      setBrowserPathCurrent(path) {
        this.browser.path.current = path
      },
      setBrowserPathSelected(path) {
        this.browser.path.selected = path
      },
      toggleBrowserNewWindow() {
        this.browser.newWindow = !this.browser.newWindow
      },
      setBrowserLinkTitle(event) {
        this.browser.linkTitle = event.target.value
      },
      getSelection(index = null) {
        const document = this.getInlineDoc()
        if (!document || !document.defaultView) return false
        const window = document.defaultView
        let selection = window.getSelection()
        if (!selection || selection.rangeCount <= 0) return false
        if (index !== null && index >= 0) {
          selection = selection.getRangeAt(index)
        }

        return selection
      }
    }
  }
</script>
