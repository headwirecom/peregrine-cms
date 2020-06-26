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
  import {deepClone, get, set} from '../../../../../../js/utils'

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
            suffix: ''
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
        if (!this.itemIsTag('A')) {
          this.insertLink()
        } else {
          this.removeLink()
        }
      },
      insertLink() {
        this.browser.cmd = 'createLink'
        this.browser.header = this.$i18n('Create Link')
        this.browser.path.current = this.roots.pages
        this.browser.withLinkTab = true
        this.browser.type = 'page'
        this.browser.path.suffix = '.html'
        this.startBrowsing()
      },
      editLink() {
        this.insertLink() //TODO
      },
      removeLink() {
        const document = this.getInlineDoc()
        if (!document || !document.defaultView) return false
        const window = document.defaultView
        let selection = window.getSelection()
        if (!selection || selection.rangeCount <= 0) return false

        const range = document.createRange();
        range.setStart(selection.anchorNode, 0);
        range.setEnd(selection.anchorNode, selection.anchorNode.length)
        selection.removeAllRanges()
        selection.addRange(range)
        this.execCmd('unlink')
      },
      insertImage() {
        this.browser.cmd = 'insertImage'
        this.browser.header = this.$i18n('Insert Image')
        this.browser.path.current = this.roots.assets
        this.browser.withLinkTab = false
        this.browser.type = 'image'
        this.browser.path.suffix = ''
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
        this.execCmd(this.browser.cmd, this.browser.path.selected + this.browser.path.suffix)
        this.browser.cmd = null
        this.browser.path.selected = null
        this.key++
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
