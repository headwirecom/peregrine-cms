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
    <template v-for="(group, groupIndex) in groups">
      <admin-components-richtoolbargroup
          v-if="group.items.length > 0 && groupAllowed(group)"
          :icon="group.icon"
          :iconLib="group.iconLib"
          :collapse="!group.noCollapse && (group.collapse || forceCollapse)"
          :label="group.label"
          :title="group.title"
          :active="groupIsActive(group)"
          :items="group.items"
          :class="group.class"
          @click="exec($event.btn.cmd)"/>
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
  import {
    actionsGroup,
    alignGroup,
    alwaysActiveGroup,
    boldItalicGroup,
    imageGroup,
    linkGroup,
    listGroup,
    removeFormatGroup,
    superSubScriptGroup,
    textFormatGroup
  } from './groups'
  import {get, restoreSelection, saveSelection, set} from '../../../../../../js/utils'
  import {PathBrowser} from '../../../../../../js/constants'

  export default {
    name: 'RichToolbar',
    props: {
      showAlwaysActive: {
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
          element: null,
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
        },
        docEl: {
          dimension: {
            w: 0
          }
        },
        breakpoint: {
          w: 815
        }
      }
    },
    computed: {
      groups() {
        return [
          alwaysActiveGroup(this),
          actionsGroup(this),
          textFormatGroup(this),
          boldItalicGroup(this),
          superSubScriptGroup(this),
          linkGroup(this),
          imageGroup(this),
          alignGroup(this),
          listGroup(this),
          removeFormatGroup(this)
        ]
      },
      btns() {
        const btns = {
          removeFormat: [
            {
              title: 'remove format',
              icon: 'format_clear',
              iconLib: 'material-icons',
              cmd: 'removeFormat'
            }
          ]
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
      forceCollapse() {
        return this.docEl.dimension.w <= this.breakpoint.w
      }
    },
    mounted() {
      this.$nextTick(() => {
        window.addEventListener('resize', this.updateDocElDimensions)
        this.updateDocElDimensions()
      })
    },
    beforeDestroy() {
      window.removeEventListener('resize', this.updateDocElDimensions)
    },
    methods: {
      pingRichToolbar(vm = this) {
        vm.key = vm.key === 1 ? 0 : 1
        $perAdminApp.action(vm, 'reWrapEditable')
      },
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
        this.pingRichToolbar()
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

        this.selection.content = range.startContainer.textContent.substr(start, len)

        this.param.cmd = 'insertLink'
        this.browser.header = this.$i18n('Insert Link')
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
        const title = anchor.getAttribute('title')
        const target = anchor.getAttribute('target')
        const href = anchor.getAttribute('href')
        const hrefArr = href.substr(0, href.length - 5).split('/')
        this.param.cmd = 'editLink'
        this.browser.header = this.$i18n('Edit Link')
        this.browser.path.selected = hrefArr.join('/')
        hrefArr.pop()
        this.browser.path.current = hrefArr.join('/')
        this.browser.withLinkTab = true
        this.browser.type = PathBrowser.Type.PAGE
        this.browser.newWindow = target === '_blank'
        this.browser.linkTitle = title
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
      editImage(vm = this, target) {
        const title = target.getAttribute('title')
        const src = target.getAttribute('src')
        const srcArr = src.split('/')
        vm.param.cmd = 'editImage'
        vm.browser.header = vm.$i18n('Edit Image')
        vm.browser.path.selected = srcArr.join('/')
        srcArr.pop()
        vm.browser.path.current = srcArr.join('/')
        vm.browser.withLinkTab = true
        vm.browser.newWindow = undefined
        vm.browser.type = PathBrowser.Type.ASSET
        vm.browser.linkTitle = title
        vm.browser.element = target
        vm.startBrowsing()
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
        if (this.selection.restore) {
          this.restoreSelection()
          this.selection.restore = false
        }
      },
      onBrowserSelect() {
        this.browser.open = false

        if (this.selection.restore) {
          this.restoreSelection()
        }

        this.$nextTick(() => {
          if (['editLink', 'insertLink'].includes(this.param.cmd)) {
            this.onLinkSelect()
            return;
          } else if (['insertImage', 'editImage'].includes(this.param.cmd)) {
            this.onImageSelect()
          }

          this.execCmd(this.param.cmd, this.param.value)
          this.param.cmd = null
          this.param.value = null
          this.browser.path.selected = null
          this.browser.linkTitle = null
          this.pingRichToolbar()

          if (this.selection.restore) {
            this.$nextTick(() => {
              this.restoreSelection()
              this.selection.restore = false
            })
          }
        })
      },
      onLinkSelect() {
        if (this.param.cmd === 'insertLink') {
          if (this.browser.path.selected.startsWith('/')) {
            this.browser.path.selected += '.html'
          }

          const link = this.selection.doc.createElement('a')
          link.setAttribute('href', this.browser.path.selected)
          link.setAttribute('title', this.browser.linkTitle)
          link.setAttribute('target', this.browser.newWindow ? '_blank' : '_self')
          link.textContent = this.selection.content
          this.restoreSelection()
          this.$nextTick(() => {
            const range = this.getSelection(0)
            range.deleteContents()
            range.insertNode(link)
            $perAdminApp.action(this, 'reWrapEditable')
            $perAdminApp.action(this, 'writeInlineToModel')
            this.$nextTick(() => {
              $perAdminApp.action(this, 'textEditorWriteToModel')
            })
          })
        } else {
          this.restoreSelection()
          this.$nextTick(() => {
            const selection = this.getSelection()
            const link = selection.focusNode.parentNode
            link.setAttribute('href', this.browser.path.selected)
            link.setAttribute('title', this.browser.linkTitle)
            link.setAttribute('target', this.browser.newWindow ? '_blank' : '_self')
            link.textContent = this.selection.content
            $perAdminApp.action(this, 'reWrapEditable')
            $perAdminApp.action(this, 'writeInlineToModel')
            this.$nextTick(() => {
              $perAdminApp.action(this, 'textEditorWriteToModel')
            })
          })
        }
      },
      onImageSelect() {
        if (this.param.cmd === 'editImage') {
          const imgEl = this.browser.element
          const linkTitle = this.browser.linkTitle
          imgEl.setAttribute('src', this.browser.path.selected)
          imgEl.setAttribute('alt', linkTitle ? linkTitle : '')
          imgEl.setAttribute('title', linkTitle ? linkTitle : '')
          $perAdminApp.action(this, 'reWrapEditable')
          $perAdminApp.action(this, 'writeInlineToModel')
          this.$nextTick(() => {
            $perAdminApp.action(this, 'textEditorWriteToModel')
          })
          this.browser.element = null
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
      },
      updateDocElDimensions() {
        this.docEl.dimension.w = document.documentElement.clientWidth
      },
      groupAllowed(group) {
        return !group.rules || group.rules(this)
      },
      groupIsActive(group) {
        return group.items.filter((item) => item.isActive && item.isActive()).length > 0
      }
    }
  }
</script>
