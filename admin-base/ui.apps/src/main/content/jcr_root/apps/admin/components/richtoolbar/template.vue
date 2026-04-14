<!--
  #%L
  admin base - UI Apps
  %%
  Copyright (C) 2017 headwire inc.
  %%
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
  #L%
  -->
<script>
import {
  actionsGroup,
  alignGroup,
  alwaysActiveGroup,
  boldItalicGroup,
  iconsGroup,
  imageGroup,
  linkGroup,
  listGroup,
  removeFormatGroup,
  responsiveMenuGroup,
  specialCharactersGroup,
  superSubScriptGroup,
  textFormatGroup,
} from './groups'
import {get, restoreSelection, saveSelection, set} from '../../../../../../js/utils'
import {IconLib, PathBrowser} from '../../../../../../js/constants'
import RichtoolbarFontSize from '../richtoolbarfontsize/template.vue'
import RichtoolbarGroup from '../richtoolbargroup/template.vue'
import Pathbrowser from '../pathbrowser/template.vue'

function renderIcon(h, icon, lib) {
  if (!icon) return null
  if (lib === IconLib.FONT_AWESOME)
    return h('i', { class: `fa fa-${icon}` })
  if (lib === IconLib.PLAIN_TEXT)
    return h('span', { domProps: { innerHTML: icon } })
  return h('i', { class: 'material-icons', domProps: { textContent: icon } })
}

function resolveClass(cls) {
  if (typeof cls === 'function') return cls()
  return cls || ''
}

function renderBtn(h, btn, vm, keyPrefix, index, inDropdown = false, closeDropdown = null) {
  const isActive = btn.isActive ? btn.isActive() : false
  const isDisabled = btn.isDisabled ? btn.isDisabled() : false
  const extraClass = resolveClass(btn.class)
  const uniqueKey = keyPrefix != null
    ? (index != null ? `${keyPrefix}-${index}` : `${keyPrefix}-${btn.label}`)
    : btn.label
  const classes = inDropdown
    ? ['rtb-menu-item', { active: isActive }, extraClass]
    : ['rtb-btn', 'btn', { active: isActive }, extraClass]
  const label = btn.title ? vm.$i18n(btn.title) : vm.$i18n(btn.label)
  const children = [renderIcon(h, btn.icon, btn.iconLib || IconLib.FONT_AWESOME)]
  if (inDropdown) {
    children.push(h('span', { class: 'rtb-menu-item-label' }, [label]))
  }
  return h('button', {
    key: uniqueKey,
    class: classes,
    attrs: { title: label, type: 'button' },
    domProps: { disabled: isDisabled },
    on: {
      mousedown: (e) => {
        e.preventDefault()
        try {
          const isEditor = document.querySelector('.text-editor:not(.inline-edit)') !== null
          if (isEditor && (btn.cmd === 'insertOrderedList' || btn.cmd === 'insertUnorderedList')) {
            const doc = vm.getInlineDoc() || vm.getLastDoc()
            const container = vm.getInlineContainer() || vm.getLastContainer()
            if (doc && container) {
              let savedSel = saveSelection(container, doc)
              if (!savedSel) {
                savedSel = $perAdminApp.getNodeFromViewOrNull('/state/inline/lastSelectionBuffer')
              }
              if (savedSel) {
                restoreSelection(container, savedSel, doc)
                window.__savedSel = savedSel
                window.__savedDoc = doc
                doc.execCommand(btn.cmd, false, null)

                let comp = vm.$parent
                while (comp) {
                  if (comp.textEditorWriteToModel) {
                    comp.textEditorWriteToModel(comp)
                    break
                  }
                  if (comp.$parent) comp = comp.$parent
                  else break
                }
              }
            }
          }
        } catch(err) {}
      },
      click: () => {
        if (isDisabled) return
        if (closeDropdown) closeDropdown()
        if (btn.click) btn.click(); else vm.exec(btn.cmd)
      },
    },
  }, children)
}

export default {
  name: 'RichToolbar',
  components: { RichtoolbarFontSize, RichtoolbarGroup, Pathbrowser },
  props: {
    showAlwaysActive: {
      type: Boolean,
      default: true,
    },
    responsive: {
      type: Boolean,
      default: true,
    },
    editorContent: {
      type: String,
      default: '',
    },
    onSubNav: {
      type: Boolean,
      default: false,
    },
  },

  data() {
    return {
      openGroups: {},
      selection: {
        restore: false,
        buffer: null,
        doc: null,
        container: null,
        content: null,
      },
      param: {
        cmd: null,
        value: null,
      },
      browser: {
        element: null,
        open: false,
        header: '',
        root: '',
        type: 'image',
        withLinkTab: false,
        withImageTab: false,
        newWindow: false,
        linkTitle: '',
        altText: undefined,
        path: {
          current: '',
          selected: null,
        },
        rel: true,
        img: {
          width: null,
          height: null,
          objectFit: null,
        },
      },
      docEl: {
        dimension: {
          w: 0,
        },
      },
      size: {
        button: 34,
        group: 4,
      },
      hiddenGroups: {},

      historyStack: [],
      historyIndex: -1,
      isUndoing: false,

      sharedHistoryIndex: -1,
      sharedHistoryLength: 0,

      hasEditorSelection: false,
    }
  },

  computed: {
    alwaysActiveGroup() {
      return alwaysActiveGroup(this)
    },
    groups() {
      this.inlinePing
      return [
        actionsGroup(this),
        textFormatGroup(this),
        boldItalicGroup(this),
        superSubScriptGroup(this),
        linkGroup(this),
        imageGroup(this),
        alignGroup(this),
        listGroup(this),
        iconsGroup(this),
        specialCharactersGroup(this),
        removeFormatGroup(this),
      ]
    },
    filteredGroups() {
      return this.groups.filter((group) => this.groupAllowed(group))
    },
    responsiveMenuGroup() {
      return responsiveMenuGroup(this)
    },
    inline() {
      if (!$perAdminApp.getView() || !$perAdminApp.getView().state) return null
      return $perAdminApp.getView().state.inline
    },
    inlineRich() {
      if (!this.inline) return null
      return this.inline.rich
    },
    inlinePing() {
      if (!this.inline) return 30
      return this.inline.ping || 20
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
        link: this.insertLink,
        insertImage: this.insertImage,
        editImage: this.editImage,
        editIcon: this.editIcon,
        preview: this.togglePreview,
        previewInNewTab: this.previewInNewTab,
        updateFontSize: this.updateFontSize,
        undo: this.undo,
        redo: this.redo,
        superscript: this.toggleSuperscript,
        subscript: this.toggleSubscript,
        insertOrderedList: () => this.toggleList('insertOrderedList'),
        insertUnorderedList: () => this.toggleList('insertUnorderedList'),
      }
    },
  },

  mounted() {
    this.$nextTick(() => {
      window.addEventListener('resize', this.updateDocElDimensions)
      this.updateDocElDimensions()
    })
    this.saveSnapshot()
    this.$watch('editorContent', () => {
      if (this.isUndoing) {
        this.isUndoing = false
      }
      this.saveSnapshot()
    })
    if (!this.onSubNav) {
      window.addEventListener('inline-richtoolbar:cmd', this.inlineCmdHandler)

      this._updateEditorSelection = () => {
        const hasSelection = !!this.getEditorSelection()
        this.hasEditorSelection = hasSelection
        window.dispatchEvent(new CustomEvent('richtoolbar:selection', {
          detail: { hasEditorSelection: hasSelection },
        }))
      }
      document.addEventListener('selectionchange', this._updateEditorSelection)

      this.$watch('inline', (val) => {
        if (!val) {
          this.hasEditorSelection = false
          window.dispatchEvent(new CustomEvent('richtoolbar:selection', {
            detail: { hasEditorSelection: false },
          }))
        } else {
          const iframe = document.querySelector('iframe#editview')
          if (iframe?.contentDocument && !this._iframeSelectionListenerRegistered) {
            iframe.contentDocument.addEventListener('selectionchange', this._updateEditorSelection)
            this._iframeSelectionListenerRegistered = true
          }
        }
      })
    } else {
      this._historyHandler = (e) => {
        this.sharedHistoryIndex = e.detail.historyIndex
        this.sharedHistoryLength = e.detail.historyLength
      }
      window.addEventListener('richtoolbar:history', this._historyHandler)

      this._selectionHandler = (e) => {
        this.hasEditorSelection = e.detail.hasEditorSelection
      }
      window.addEventListener('richtoolbar:selection', this._selectionHandler)
    }
    document.addEventListener('mousedown', this._closeDropdowns)
  },

  beforeDestroy() {
    window.removeEventListener('resize', this.updateDocElDimensions)
    window.removeEventListener('inline-richtoolbar:cmd', this.inlineCmdHandler)
    if (this._historyHandler) {
      window.removeEventListener('richtoolbar:history', this._historyHandler)
    }
    if (this._selectionHandler) {
      window.removeEventListener('richtoolbar:selection', this._selectionHandler)
    }
    if (this._updateEditorSelection) {
      document.removeEventListener('selectionchange', this._updateEditorSelection)
      const iframe = document.querySelector('iframe#editview')
      if (iframe?.contentDocument && this._iframeSelectionListenerRegistered) {
        iframe.contentDocument.removeEventListener('selectionchange', this._updateEditorSelection)
      }
    }
    document.removeEventListener('mousedown', this._closeDropdowns)
  },

  render(h) {
    const disabled = !this.inlineRich || this.preview === 'preview'
    const children = []

    if (this.groupAllowed(this.alwaysActiveGroup)) {
      children.push(this._renderGroup(h, this.alwaysActiveGroup, true))
    }

    for (const group of this.filteredGroups) {
      children.push(this._renderGroup(h, group, false))
    }

    if (this.groupAllowed(this.responsiveMenuGroup)) {
      children.push(this._renderGroup(h, this.responsiveMenuGroup, true))
    }

    children.push(h(RichtoolbarFontSize, {
      key: 'font-size',
      props: {
        exec: this.exec,
        isRangeInEditor: this.isRangeInEditor,
        isNodeInEditor: this.isNodeInEditor,
        getDefaultFontSize: this.getDefaultFontSize,
        getSelection: this.getSelection,
        getEditorSelection: this.getEditorSelection,
        disabled: !this.hasEditorSelection,
      },
    }))

    if (this.browser.open) {
      children.push(h(Pathbrowser, {
        key: 'pathbrowser',
        props: {
          isOpen: this.browser.open,
          header: this.browser.header,
          browserRoot: this.browser.root,
          browserType: this.browser.type,
          withLinkTab: this.browser.withLinkTab,
          withImageTab: this.browser.withImageTab,
          newWindow: this.browser.newWindow,
          linkTitle: this.browser.linkTitle,
          setLinkTitle: this.setBrowserLinkTitle,
          altText: this.browser.altText,
          setAltText: this.setBrowserAltText,
          currentPath: this.browser.path.current,
          setCurrentPath: this.setBrowserPathCurrent,
          selectedPath: this.browser.path.selected,
          setSelectedPath: this.setBrowserPathSelected,
          setResourceType: this.setBrowserResourceType,
          rel: this.browser.rel,
          imgWidth: this.browser.img.width,
          imgHeight: this.browser.img.height,
          onCancel: this.onBrowserCancel,
        },
        on: {
          'toggle-newWindow': this.toggleBrowserNewWindow,
          'toggle-rel': () => { this.browser.rel = !this.browser.rel },
          'update-img-width': v => { this.browser.img.width = v },
          'update-img-height': v => { this.browser.img.height = v },
          'update-img-objectFit': v => { this.browser.img.objectFit = v },
          select: this.onBrowserSelect,
        },
      }))
    }

    return h('div', {
      class: ['richtoolbar', { disabled }],
      ref: 'richToolbar',
    }, children)
  },

  methods: {
    _renderGroup(h, group, alwaysActive) {
      const items = typeof group.items === 'function' ? group.items() : group.items
      if (!items || items.length === 0) return null

      const label = typeof group.label === 'function' ? group.label() : group.label
      const icon = typeof group.icon === 'function' ? group.icon() : group.icon
      const iconLib = group.iconLib || IconLib.FONT_AWESOME
      const groupClass = resolveClass(group.class)
      const groupIsDisabled = group.isDisabled ? group.isDisabled() : false

      if (group.searchable) {
        return h(RichtoolbarGroup, {
          key: `group-${label}`,
          props: {
            icon,
            iconLib,
            collapse: !!group.collapse,
            label,
            active: this.groupIsActive(group),
            items,
            searchable: true,
            disabled: groupIsDisabled,
          },
          on: {
            'toggle-click': () => { this.openGroups = {}; if (group.toggleClick) group.toggleClick() },
            click: ({ btn }) => { if (btn.click) btn.click(); else this.exec(btn.cmd) },
          },
        })
      }

      const itemRules = group.itemRules || null
      const realItems = items.filter((it, i) => {
        if (!it || typeof it !== 'object') return false
        if (itemRules && itemRules[i] && !itemRules[i]()) return false
        return true
      })

      if (group.collapse) {
        const groupIsActive = this.groupIsActive(group)

        if (realItems.length <= 1) {
          const singleItem = realItems[0]
          const clickFn = group.toggleClick
            ? () => group.toggleClick()
            : singleItem
              ? () => singleItem.click ? singleItem.click() : this.exec(singleItem.cmd)
              : () => {}
          return h('div', {
            key: `group-${label}`,
            class: ['btn-group', 'rtb-group', `group-${label}`, { 'group-always-active': alwaysActive }, groupClass],
          }, [h('button', {
            class: ['rtb-btn', 'btn', { active: groupIsActive }, groupClass],
            attrs: { title: this.$i18n(label), type: 'button' },
            domProps: { disabled: groupIsDisabled },
            on: { mousedown: (e) => {
              e.preventDefault()
              try {
                const isEditor = document.querySelector('.text-editor:not(.inline-edit)') !== null
                if (isEditor && (btn.cmd === 'insertOrderedList' || btn.cmd === 'insertUnorderedList')) {
                  const doc = this.getInlineDoc() || this.getLastDoc()
                  const container = this.getInlineContainer() || this.getLastContainer()
                  if (doc && container) {
                    let savedSel = saveSelection(container, doc)
                    if (!savedSel) {
                      savedSel = $perAdminApp.getNodeFromViewOrNull('/state/inline/lastSelectionBuffer')
                    }
                    if (savedSel) {
                      restoreSelection(container, savedSel, doc)
                      window.__savedSel = savedSel
                      window.__savedDoc = doc
                      doc.execCommand(btn.cmd, false, null)

                      let comp = this.$parent
                      while (comp) {
                        if (comp.textEditorWriteToModel) {
                          comp.textEditorWriteToModel(comp)
                          break
                        }
                        if (comp.$parent) comp = comp.$parent
                        else break
                      }
                    }
                  }
                }
              } catch(err) {}
            }, click: () => { if (!groupIsDisabled) clickFn() } },
          }, [renderIcon(h, icon, iconLib)])])
        }

        const isOpen = !!this.openGroups[label]

        const closeThisDropdown = () => this.$set(this.openGroups, label, false)
        const menuItems = realItems.map((btn, i) => {
          if (btn.items && btn.items.length > 0) {
            return this._renderGroup(h, btn, false)
          }
          return renderBtn(h, btn, this, label, i, true, closeThisDropdown)
        })

        const menu = h('div', {
          class: ['rtb-dropdown-menu', { 'rtb-dropdown-menu--open': isOpen }],
          style: { backgroundColor: '#fff' },
        }, [h('div', { class: 'rtb-dropdown-items-list' }, menuItems)])

        const toggleBtnChildren = [renderIcon(h, icon, iconLib)]
        if (group.showLabel) {
          toggleBtnChildren.push(h('span', { class: 'rtb-dropdown-label' }, [label]))
        }
        toggleBtnChildren.push(h('span', { class: 'caret-down' }))

        const toggleBtn = h('button', {
          class: ['rtb-btn', 'btn', 'rtb-dropdown-toggle', { active: groupIsActive && !isOpen }, groupClass],
          attrs: { title: this.$i18n(label), type: 'button' },
          domProps: { disabled: groupIsDisabled },
          on: {
            mousedown: e => e.preventDefault(),
            click: () => { if (!groupIsDisabled) this._toggleGroup(label) },
          },
        }, toggleBtnChildren)

        return h('div', {
          key: `group-${label}`,
          class: ['btn-group', 'rtb-group', 'rtb-group--dropdown', `group-${label}`, { 'rtb-group--open': isOpen }],
        }, [toggleBtn, menu])
      }

      const btnNodes = realItems.map((btn, i) => {
        if (btn.items && btn.items.length > 0) {
          return this._renderGroup(h, btn, false)
        }
        return renderBtn(h, btn, this, label, i)
      })

      return h('div', {
        key: `group-${label}`,
        class: ['btn-group', 'rtb-group', `group-${label}`, { 'group-always-active': alwaysActive }, groupClass],
      }, btnNodes)
    },

    _closeDropdowns(e) {
      if (!this.$el || !this.$el.contains(e.target)) {
        this.openGroups = {}
      }
    },

    _toggleGroup(label) {
      const opening = !this.openGroups[label]
      this.openGroups = {}
      if (opening) {
        this.$set(this.openGroups, label, true)
        this.$nextTick(() => {
          const menu = this.$el.querySelector('.rtb-dropdown-menu--open')
          const dropdown = menu?.querySelector('.rtb-dropdown-items-list')
          if (dropdown) {
            const rect = dropdown.getBoundingClientRect()
            if (rect.right > window.innerWidth) {
              menu.classList.add('rtb-dropdown-menu--align-right')
            } else {
              menu.classList.remove('rtb-dropdown-menu--align-right')
            }
          }
        })
      }
    },

    inlineCmdHandler(event) {
      if (this.onSubNav) return
      if (typeof this[event.detail.cmd] === 'function') {
        this[event.detail.cmd]()
      } else {
        this.exec(event.detail.cmd, event.detail.value || null)
      }
    },

    saveSnapshot() {
      const content = this.editorContent
      if (this.historyIndex > -1 && this.historyStack[this.historyIndex] === content) {
        return
      }
      if (this.historyIndex < this.historyStack.length - 1) {
        this.historyStack = this.historyStack.slice(0, this.historyIndex + 1)
      }
      this.historyStack.push(content)
      this.historyIndex++
      if (this.historyStack.length > 50) {
        this.historyStack.shift()
        this.historyIndex--
      }
      if (!this.onSubNav) {
        window.dispatchEvent(new CustomEvent('richtoolbar:history', {
          detail: { historyIndex: this.historyIndex, historyLength: this.historyStack.length },
        }))
      }
    },

    undo() {
      if (this.onSubNav) {
        window.dispatchEvent(new CustomEvent('inline-richtoolbar:cmd', { detail: { cmd: 'undo' } }))
        return
      }
      if (this.historyIndex > 0) {
        this.isUndoing = true
        this.historyIndex--
        window.dispatchEvent(new CustomEvent('richtoolbar:history', {
          detail: { historyIndex: this.historyIndex, historyLength: this.historyStack.length },
        }))
        const content = this.historyStack[this.historyIndex]
        const container = this.getInlineContainer()
        const doc = this.getInlineDoc()
        const savedSel = container && doc ? saveSelection(container, doc) : null
        const textEditor = this.$el.closest('.text-editor-wrapper')
          ? this.$el.closest('.text-editor-wrapper').querySelector('.text-editor')
          : this.$el.nextElementSibling
        textEditor.innerHTML = content
        $perAdminApp.action(this, 'textEditorWriteToModel')
        this.$nextTick(() => {
          this.isUndoing = false
          if (savedSel && container && doc) {
            restoreSelection(container, savedSel, doc)
          }
          this.pingRichToolbar()
        })
      }
    },

    redo() {
      if (this.onSubNav) {
        window.dispatchEvent(new CustomEvent('inline-richtoolbar:cmd', { detail: { cmd: 'redo' } }))
        return
      }
      if (this.historyIndex < this.historyStack.length - 1) {
        this.isUndoing = true
        this.historyIndex++
        window.dispatchEvent(new CustomEvent('richtoolbar:history', {
          detail: { historyIndex: this.historyIndex, historyLength: this.historyStack.length },
        }))
        const content = this.historyStack[this.historyIndex]
        const container = this.getInlineContainer()
        const doc = this.getInlineDoc()
        const savedSel = container && doc ? saveSelection(container, doc) : null
        const textEditor = this.$el.closest('.text-editor-wrapper')
          ? this.$el.closest('.text-editor-wrapper').querySelector('.text-editor')
          : this.$el.nextElementSibling
        textEditor.innerHTML = content
        $perAdminApp.action(this, 'textEditorWriteToModel')
        this.$nextTick(() => {
          this.isUndoing = false
          if (savedSel && container && doc) {
            restoreSelection(container, savedSel, doc)
          }
          this.pingRichToolbar()
        })
      }
    },

    saveToModel(container) {
      const content = container.innerHTML

      const dataInline = container.getAttribute('data-per-inline')

      if (dataInline) {
        const view = $perAdminApp.getView()
        if (!view || !view.pageView || !view.pageView.page) return false

        const editorPath = $perAdminApp.getNodeFromView('/state/editor/path')
        const pageNode = view.pageView.page

        let parentProp
        if (editorPath) {
          parentProp = $perAdminApp.findNodeFromPath(pageNode, editorPath)
        }

        if (!parentProp) {
          const pathParts = dataInline.split('.').slice(1)
          pathParts.reverse()
          parentProp = pageNode
          while (pathParts.length > 1) {
            parentProp = parentProp[pathParts.pop()]
          }
        }

        const keyStr = dataInline.split('.').pop()
        if (parentProp && keyStr) {
          parentProp[keyStr] = content
          return true
        }
      }

      const editorModel = $perAdminApp.getNodeFromViewOrNull('/state/inline/editorModel')
      if (editorModel) {
        editorModel.text = content
        return true
      }

      return false
    },

    toggleScriptTag(tagName, cmd) {
      if (window.__savedDoc) {
        window.__savedSel = null
        window.__savedDoc = null
        return
      }

      let doc = this.getInlineDoc()
      let container = this.getInlineContainer()

      if (!doc || !container) {
        doc = this.getLastDoc()
        container = this.getLastContainer()
      }

      if (!doc || !container) return

      const isEditor = document.querySelector('.text-editor-wrapper') !== null

      const savedSel = saveSelection(container, doc)

      if (!isEditor && doc.execCommand) {
        doc.execCommand(cmd, false, null)
        if (savedSel) {
          restoreSelection(container, savedSel, doc)
        }
        this.saveToModel(container)
        return
      }

      if (!savedSel) return

      const win = doc.defaultView || window
      const sel = win.getSelection()
      if (!sel || sel.rangeCount === 0) return

      const range = sel.getRangeAt(0)
      const toEl = node => node && (node.nodeType === Node.TEXT_NODE ? node.parentElement : node)
      const startEl = toEl(range.startContainer)
      const endEl = toEl(range.endContainer)
      const existingTag = (startEl && startEl.closest(tagName)) || (endEl && endEl.closest(tagName))

      if (existingTag) {
        const parent = existingTag.parentNode
        while (existingTag.firstChild) {
          parent.insertBefore(existingTag.firstChild, existingTag)
        }
        parent.removeChild(existingTag)
      } else {
        try {
          const wrapper = doc.createElement(tagName)
          range.surroundContents(wrapper)
        } catch (e) {
          const fragment = range.extractContents()
          const wrapper = doc.createElement(tagName)
          wrapper.appendChild(fragment)
          range.insertNode(wrapper)
        }
      }

      const freshContainer = this.getInlineContainer() || this.getLastContainer() || container
      if (freshContainer) {
        let comp = this.$parent
        let foundEditor = false
        while (comp) {
          if (comp.textEditorWriteToModel) {
            comp.textEditorWriteToModel(comp)
            foundEditor = true
            break
          }
          if (comp.$parent) {
            comp = comp.$parent
          } else {
            break
          }
        }

        if (!foundEditor) {
          this.saveToModel(freshContainer)
        }

        if (savedSel) {
          restoreSelection(freshContainer, savedSel, doc)
        }
      }
    },

    toggleSuperscript() {
      this.toggleScriptTag('SUP', 'superscript')
    },

    toggleSubscript() {
      this.toggleScriptTag('SUB', 'subscript')
    },

    toggleList(cmd) {
      if (window.__savedDoc) {
        window.__savedSel = null
        window.__savedDoc = null
        return
      }

      let doc = this.getInlineDoc()
      let container = this.getInlineContainer()

      if (!doc || !container) {
        doc = this.getLastDoc()
        container = this.getLastContainer()
      }

      if (!doc || !container) return

      const isEditor = document.querySelector('.text-editor:not(.inline-edit)') !== null
      const savedSel = saveSelection(container, doc)

      if (!isEditor && doc.execCommand) {
        doc.execCommand(cmd, false, null)
        if (savedSel) {
          restoreSelection(container, savedSel, doc)
        }
        this.saveToModel(container)
        return
      }

      doc.execCommand(cmd, false, null)

      const freshContainer = this.getInlineContainer() || this.getLastContainer() || container
      if (freshContainer) {
        let comp = this.$parent
        let foundEditor = false
        while (comp) {
          if (comp.textEditorWriteToModel) {
            comp.textEditorWriteToModel(comp)
            foundEditor = true
            break
          }
          if (comp.$parent) {
            comp = comp.$parent
          } else {
            break
          }
        }

        if (!foundEditor) {
          this.saveToModel(freshContainer)
        }

        if (savedSel) {
          restoreSelection(freshContainer, savedSel, doc)
        }
      }
    },

    getDefaultFontSize() {
      const iframeWindow = document.querySelector('iframe#editview')?.contentWindow
      if (!iframeWindow) return 16
      const currentInlineEditor = iframeWindow.document.querySelector(
        '.inline-edit[contenteditable="true"] > *'
      )
      if (!currentInlineEditor) return null
      return iframeWindow.getComputedStyle(currentInlineEditor).fontSize
    },

    wrapTextNodesInRange(range, fontSize) {
      const textNodes = []
      const walker = document.createTreeWalker(
        range.commonAncestorContainer,
        NodeFilter.SHOW_TEXT,
        {
          acceptNode: node => {
            if (!node.nodeValue.trim()) return NodeFilter.FILTER_REJECT
            const nodeRange = document.createRange()
            nodeRange.selectNodeContents(node)
            return range.intersectsNode(node) ? NodeFilter.FILTER_ACCEPT : NodeFilter.FILTER_REJECT
          },
        }
      )
      let node
      while (node = walker.nextNode()) {
        textNodes.push(node)
      }
      const fontSizeNodes = []
      for (let i = 0; i < textNodes.length; i++) {
        const textNode = textNodes[i]
        const nodeRange = document.createRange()
        nodeRange.selectNodeContents(textNode)
        if (textNode === range.startContainer || textNode.contains(range.startContainer)) {
          nodeRange.setStart(range.startContainer, range.startOffset)
        }
        if (textNode === range.endContainer || textNode.contains(range.endContainer)) {
          nodeRange.setEnd(range.endContainer, range.endOffset)
        }
        const existingSpan = textNode.parentElement.tagName === 'SPAN' && textNode.parentElement.childNodes.length === 1
          ? textNode.parentElement : null
        if (existingSpan) {
          existingSpan.style.fontSize = fontSize
          fontSizeNodes.push(existingSpan)
        } else {
          const span = document.createElement('span')
          span.style.fontSize = fontSize
          nodeRange.surroundContents(span)
          fontSizeNodes.push(span)
        }
      }
      return fontSizeNodes
    },

    getEditorSelection(returnRange = true) {
      const selection = window.getSelection()
      const iframeSelection = document.querySelector('iframe#editview')?.contentDocument.getSelection()
      if (selection?.rangeCount > 0) {
        const range = selection.getRangeAt(0)
        if (this.isRangeInEditor(range)) return returnRange ? range : selection
      }
      if (iframeSelection?.rangeCount > 0) {
        const iframeRange = iframeSelection.getRangeAt(0)
        if (this.isRangeInEditor(iframeRange)) return returnRange ? iframeRange : iframeSelection
      }
    },

    getEditorFrom(range) {
      const getEditorFromEl = typeof range.startContainer.closest === 'function'
        ? range.startContainer
        : range.startContainer.parentElement
      return getEditorFromEl.closest('.inline-edit[contenteditable="true"]')
    },

    isRangeInEditor(range) {
      if (!range) return false
      const textEditor = this.getEditorFrom(range)
      if (!textEditor) return false
      const elementRange = textEditor.ownerDocument.createRange()
      elementRange.selectNodeContents(textEditor)
      return (
        range.compareBoundaryPoints(Range.START_TO_START, elementRange) >= 0 &&
        range.compareBoundaryPoints(Range.END_TO_END, elementRange) <= 0
      )
    },

    isNodeInEditor(node) {
      if (!node) return false
      const textEditor = this.getEditorFrom({ startContainer: node })
      return textEditor.contains(node)
    },

    selectNodes(nodeArray) {
      const selection = this.getEditorSelection(false)
      selection.removeAllRanges()
      const reselectRange = nodeArray[0].ownerDocument.createRange()
      reselectRange.setStart(nodeArray[0], 0)
      reselectRange.setEnd(nodeArray[nodeArray.length - 1], nodeArray[nodeArray.length - 1].childNodes.length)
      selection.addRange(reselectRange)
    },

    updateFontSize(newSize) {
      const fontSize = `${newSize}px`
      const range = this.getEditorSelection()
      const textEditor = this.getEditorFrom(range)
      if (!this.isRangeInEditor(range, textEditor)) {
        console.warn('Selection range outside of Richtext Editor')
        return
      }

      function setFontSizeOfEl(element, fontSizeStr) {
        element.querySelectorAll('*[style*="font-size"]').forEach(element => {
          if (element.tagName === 'SPAN') {
            element.replaceWith(...element.childNodes)
          } else {
            element.style.removeProperty('font-size')
          }
        })
        element.style.fontSize = fontSizeStr
      }

      const noHighlight = range.endContainer.isEqualNode(range.startContainer) && range.endOffset === range.startOffset
      if (noHighlight) {
        const parentQuery = 'p, ul, ol, h1, h2, h3, h4, h5, h6'
        const fontSizeParent = (range.startContainer.contentEditable === 'true' && range.startContainer?.children?.length > 0)
          ? range.startContainer.children[0]
          : typeof range.startContainer.closest === 'function'
            ? range.startContainer.closest(parentQuery)
            : range.startContainer.parentElement.closest(parentQuery)
        if (!textEditor.contains(fontSizeParent)) {
          console.warn('Attempting to change fontsize of paragraph outside of richtext editor', fontSizeParent, range.startContainer)
          return
        }
        setFontSizeOfEl(fontSizeParent, fontSize)
        fontSizeParent.style.fontSize = fontSize
        textEditor.dispatchEvent(new Event('input'))
        return
      }

      if (range.startContainer.nodeType === Node.TEXT_NODE) {
        const newText = range.startContainer.splitText(range.startOffset)
        range.setStart(newText, 0)
      }
      if (range.endContainer.nodeType === Node.TEXT_NODE) {
        range.endContainer.splitText(range.endOffset)
        range.setEnd(range.endContainer, range.endContainer.length)
      }

      const onlySingleNode = range.startContainer.isEqualNode(range.endContainer)
      if (onlySingleNode) {
        if (range.startContainer.nodeType === Node.TEXT_NODE) {
          const span = document.createElement('span')
          span.style.fontSize = fontSize
          range.surroundContents(span)
          this.selectNodes([span])
          return
        } else {
          setFontSizeOfEl(range.startContainer, fontSize)
        }
      }

      const nodeRanges = this.wrapTextNodesInRange(range, fontSize)

      textEditor.querySelectorAll('span[style*="font-size"]:has(> span[style*="font-size"])').forEach(span => {
        if (span.childNodes.length === 1) {
          span.replaceWith(span.childNodes[0])
        }
      })

      this.selectNodes(nodeRanges)
      const ownerDoc = nodeRanges[0].ownerDocument
      const selectionOfNodes = ownerDoc.getSelection().getRangeAt(0)

      const startSelectionMarkId = crypto.randomUUID()
      const startOffset = selectionOfNodes.startOffset
      const endSelectionMarkId = crypto.randomUUID()
      const endOffset = selectionOfNodes.endOffset
      nodeRanges[0].dataset.startSelectionMarkId = startSelectionMarkId
      nodeRanges[nodeRanges.length - 1].dataset.endSelectionMarkId = endSelectionMarkId

      if (ownerDoc.querySelector('iframe#editview')) {
        $perAdminApp.action(this, 'textEditorWriteToModel')
      } else {
        $perAdminApp.action(this, 'writeInlineToModel')
      }

      this.$nextTick(() => {
        this.$nextTick(() => {
          const selectionAfter = ownerDoc.getSelection()
          selectionAfter.removeAllRanges()
          const newRange = ownerDoc.createRange()
          const startEl = ownerDoc.querySelector(`[data-start-selection-mark-id="${startSelectionMarkId}"]`)
          newRange.setStart(startEl, startOffset)
          delete startEl.dataset.startSelectionMarkId
          const endEl = ownerDoc.querySelector(`[data-end-selection-mark-id="${endSelectionMarkId}"]`)
          newRange.setEnd(endEl, endOffset)
          delete endEl.dataset.endSelectionMarkId
          selectionAfter.addRange(newRange)
        })
      })
    },

    pingRichToolbar(vm = this) {
      vm.$emit('ping')
      const view = $perAdminApp.getView()
      if (view) {
        const state = view.state || (view.state = {})
        const inline = state.inline || (state.inline = {})
        inline.ping = (inline.ping || 0) + 1

        const anchor = vm.getAnchorAtSelection ? vm.getAnchorAtSelection() : null
        if (anchor) {
          inline.lastAnchor = anchor
        }
      }
      if ($perAdminApp.getNodeFromViewOrNull('/state/contentview/editor/active')) {
        $perAdminApp.action(vm, 'reWrapEditable')
      }
    },

    getInlineDoc() {
      if (!this.inline) return null
      return this.inline.doc
    },

    getInlineContainer() {
      const doc = this.getInlineDoc()
      if (doc) {
        const container = doc.querySelector('.inline-edit.inline-editing')
        if (container) return container
      }
      return this.getLastContainer()
    },

    execCmd(cmd, value = null, showUi = true, doc = null) {
      const targetDoc = doc || this.getInlineDoc()
      if (!targetDoc || !targetDoc.execCommand) return
      targetDoc.execCommand(cmd, showUi, value)
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
      this.insertLink()
    },

    getLastAnchor() {
      return $perAdminApp.getNodeFromViewOrNull('/state/inline/lastAnchor')
    },

    getLastContainer() {
      return $perAdminApp.getNodeFromViewOrNull('/state/inline/lastContainer')
    },

    getLastDoc() {
      return $perAdminApp.getNodeFromViewOrNull('/state/inline/lastDoc')
    },

    getAnchorAtSelection() {
      const doc = this.getInlineDoc() || this.getLastDoc()
      if (!doc) {
        const lastDoc = this.getLastDoc()
        if (lastDoc && lastDoc.defaultView) {
          return this._findAnchorInDoc(lastDoc)
        }
        return this._findAnchorInDoc(document)
      }
      if (!doc.defaultView) return null
      return this._findAnchorInDoc(doc)
    },

    _findAnchorInDoc(doc) {
      if (!doc || !doc.defaultView) return null
      const selection = doc.defaultView.getSelection()
      if (!selection || selection.rangeCount <= 0) return null
      const node = selection.anchorNode
      if (!node) return null
      const el = node.nodeType === Node.TEXT_NODE ? node.parentElement : node
      return el ? el.closest('a') : null
    },

    insertLink() {
      const doc = this.getInlineDoc() || this.getLastDoc()
      const container = this.getInlineContainer() || this.getLastContainer()
      if (!doc || !container) {
        return
      }

      const startPath = this.roots.pages
      this.param.cmd = 'insertLink'
      this.browser.header = this.$i18n('Insert Link')
      this.browser.path.current = startPath
      this.browser.path.selected = null
      this.browser.withLinkTab = true
      this.browser.newWindow = false
      this.browser.rel = true
      this.browser.linkTitle = ''
      this.browser.type = PathBrowser.Type.PAGE
      this.selection.doc = doc
      this.selection.container = container

      const win = doc.defaultView || window
      const liveSel = win.getSelection ? win.getSelection() : document.getSelection()
      const liveRange = liveSel && liveSel.rangeCount > 0 ? liveSel.getRangeAt(0).cloneRange() : null
      if (!liveRange) {
        return
      }
      this._savedRange = liveRange

      const savedSel = saveSelection(container, doc)
        || $perAdminApp.getNodeFromViewOrNull('/state/inline/lastSelectionBuffer')
      this.selection.buffer = savedSel
      this.selection.restore = false
      this.startBrowsing(startPath)
    },

    editLink() {
      let anchor = this.getAnchorAtSelection() || this.getLastAnchor()
      if (anchor && (!this.getInlineDoc() || !this.getLastDoc())) {
        const anchorDoc = anchor.ownerDocument
        if (anchorDoc) {
          set($perAdminApp.getView(), '/state/inline/lastDoc', anchorDoc)
          set($perAdminApp.getView(), '/state/inline/lastContainer', anchor.closest('.inline-edit'))
        }
      }
      if (!anchor) {
        return
      }

      let doc = this.getInlineDoc() || this.getLastDoc()
      let container = this.getInlineContainer() || this.getLastContainer()

      if (!doc || !container) {
        doc = anchor.ownerDocument
        container = anchor.closest('.inline-edit')
      }

      if (!doc || !container) return

      const href = anchor.getAttribute('href') || ''
      const title = anchor.getAttribute('title') || ''
      const target = anchor.getAttribute('target') || ''
      const rel = anchor.getAttribute('rel') || ''

      this.selection.content = anchor.innerHTML
      this.selection.doc = doc
      this.selection.container = container
      this.selection.buffer = saveSelection(container, doc)
        || $perAdminApp.getNodeFromViewOrNull('/state/inline/lastSelectionBuffer')
      this.selection.restore = true

      const view = $perAdminApp.getView()
      if (view) {
        const state = view.state || (view.state = {})
        const inline = state.inline || (state.inline = {})
        inline.lastAnchor = anchor
        inline.lastContainer = container
        inline.lastDoc = doc
      }

      this.param.cmd = 'editLink'
      this.browser.header = this.$i18n('Edit Link')
      this.browser.withLinkTab = true
      this.browser.newWindow = target === '_blank'
      this.browser.rel = rel.includes('noopener')
      this.browser.linkTitle = title
      this.browser.type = PathBrowser.Type.PAGE

      let startPath
      let resolvedHref = href

      if (/^https?:\/\//.test(href)) {
        try {
          const url = new URL(href)
          if (url.hostname === window.location.hostname) {
            resolvedHref = url.pathname
          }
        } catch (e) { /* Do nothing */ }
      }

      const isExternal = /^https?:\/\//.test(resolvedHref)
      if (isExternal) {
        this.browser.path.selected = resolvedHref
        startPath = this.roots.pages || '/'
      } else {
        let hrefPath = resolvedHref.replace(/\.html$/, '')
        if (!hrefPath.startsWith('/content/')) {
          hrefPath = (this.roots.pages || '') + hrefPath
        }
        const lastSlash = hrefPath.lastIndexOf('/')
        startPath = lastSlash > 0 ? hrefPath.substring(0, lastSlash) : (this.roots.pages || '/')
        this.browser.path.selected = hrefPath
      }
      this.browser.path.current = startPath
      this.startBrowsing(startPath)
    },

    removeLink() {
      let anchor = this.getAnchorAtSelection() || this.getLastAnchor()

      if (!anchor) {
        const inlineDoc = this.getInlineDoc()
        if (inlineDoc && inlineDoc.defaultView) {
          const sel = inlineDoc.defaultView.getSelection()
          if (sel && sel.rangeCount > 0) {
            const node = sel.anchorNode
            if (node) {
              const el = node.nodeType === Node.TEXT_NODE ? node.parentElement : node
              anchor = el ? el.closest('a') : null
            }
          }
        }
      }

      if (!anchor || !anchor.parentNode) {
        const container = this.getLastContainer()
        if (container) {
          const allAnchors = container.getElementsByTagName('a')
          if (allAnchors.length > 0) {
            anchor = allAnchors[0]
          }
        }
      }

      if (!anchor || !anchor.parentNode) {
        return
      }

      let doc = this.getInlineDoc() || this.getLastDoc()
      let container = this.getInlineContainer() || this.getLastContainer()

      if (!doc || !container) {
        doc = anchor.ownerDocument
        container = anchor.closest('.inline-edit')
      }

      if (!doc || !container) {
        return
      }

      container.focus()
      this.$nextTick(() => {
        const win = doc.defaultView || window
        const sel = win.getSelection ? win.getSelection() : null
        if (!sel) return

        const range = doc.createRange()
        range.selectNodeContents(anchor)
        sel.removeAllRanges()
        sel.addRange(range)
        doc.execCommand('unlink', false, null)

        const content = container.innerHTML
        const dataInline = container.getAttribute('data-per-inline')

        if (dataInline) {
          const view = $perAdminApp.getView()
          if (view && view.pageView && view.pageView.page) {
            const pathParts = dataInline.split('.').slice(1)
            pathParts.reverse()
            let parentProp = view.pageView.page
            while (pathParts.length > 1) {
              parentProp = parentProp[pathParts.pop()]
            }
            const keyStr = pathParts.pop()
            parentProp[keyStr] = content
          }
        }
      })
    },

    insertImage() {
      let container = null
      let doc = null

      const inlineContainer = this.getInlineContainer()
      const inlineDoc = this.getInlineDoc()
      if (inlineContainer && inlineDoc) {
        container = inlineContainer
        doc = inlineDoc
      }

      if (!container) {
        const lastContainer = this.getLastContainer()
        const lastDoc = this.getLastDoc()
        if (lastContainer && lastDoc) {
          container = lastContainer
          doc = lastDoc
        }
      }

      if (!container) {
        const wrapper = this.$el?.closest('.text-editor-wrapper')
        if (wrapper) {
          container = wrapper.querySelector('.text-editor')
          if (!doc) doc = document
        }
      }

      if (!container) {
        container = document.querySelector('.text-editor')
        if (!doc) doc = document
      }

      if (!container) {
        return
      }

      if (!doc) doc = container.ownerDocument

      this.param.cmd = 'insertImage'
      this.browser.header = this.$i18n('Insert Image')
      this.browser.path.current = this.roots.assets
      this.browser.withLinkTab = true
      this.browser.newWindow = undefined
      this.browser.type = PathBrowser.Type.ASSET
      this.browser.path.suffix = ''
      this.browser.img.width = null
      this.browser.img.height = null
      this.browser.img.objectFit = null
      this.selection.doc = doc
      this.selection.container = container

      const win = doc.defaultView || window
      const liveSel = win.getSelection ? win.getSelection() : document.getSelection()
      const liveRange = liveSel?.rangeCount > 0 ? liveSel.getRangeAt(0).cloneRange() : null
      this._savedRange = liveRange

      const savedSel = saveSelection(container, doc) || $perAdminApp.getNodeFromViewOrNull('/state/inline/lastSelectionBuffer')
      this.selection.buffer = savedSel
      this.selection.restore = !!savedSel
      this.startBrowsing()
    },

    editImage(vm = this, target) {
      const title = target.getAttribute('title')
      const src = target.getAttribute('src')
      const srcArr = src.split('/')
      const img = {
        width: target.style.width ? parseInt(target.style.width) : null,
        height: target.style.height ? parseInt(target.style.height) : null,
        objectFit: target.style.objectFit || null,
      }
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
      vm.browser.img.width = img.width
      vm.browser.img.height = img.height
      vm.browser.img.objectFit = img.objectFit
      vm.startBrowsing()
    },

    insertIcon(imgPath) {
      const doc = this.selection.doc || this.getInlineDoc() || this.getLastDoc() || document
      const win = doc.defaultView || window
      const sel = win.getSelection ? win.getSelection() : window.getSelection()
      if (!sel || sel.rangeCount === 0) return

      const range = sel.getRangeAt(0)
      range.deleteContents()
      const fragment = range.createContextualFragment(`<img class="peregrine-icon" style="font-size: inherit; display: inline; width: auto; height: 1em; vertical-align: -0.125em;" src="${imgPath}"></img>`)
      const lastChild = fragment.lastChild;
      range.insertNode(fragment)
      range.setStartAfter(lastChild)
      range.collapse(true)
      this.getEditorFrom(range).dispatchEvent(new Event('input'))
    },

    editIcon(vm = this, target) {
      const alt = target.getAttribute('alt') || ''
      const src = target.getAttribute('src') || ''
      const img = {
        width: target.style.width ? parseInt(target.style.width) : 20,
        height: target.style.height ? parseInt(target.style.height) : 20,
        objectFit: target.style.objectFit || null,
      }
      vm.param.cmd = 'editIcon'
      vm.browser.header = vm.$i18n('Edit Icon')
      vm.browser.withLinkTab = false
      vm.browser.withImageTab = true
      vm.browser.path.selected = src
      vm.browser.path.current = src
      vm.browser.type = PathBrowser.Type.ASSET
      vm.browser.altText = alt
      vm.browser.linkTitle = undefined
      vm.browser.newWindow = undefined
      vm.browser.element = target
      vm.browser.img.width = img.width
      vm.browser.img.height = img.height
      vm.browser.img.objectFit = img.objectFit
      vm.saveSelection()
      vm.selection.restore = true
      vm.startBrowsing(src.substring(0, src.lastIndexOf('/')))
    },

    setViewport(viewport) {
      set($perAdminApp.getView(), '/state/tools/workspace/view', viewport)
    },

    togglePreview() {
      const view = $perAdminApp.getView()
      const current = get(view, '/state/tools/workspace/preview', null)
      $perAdminApp.stateAction('editPreview', current ? null : 'preview')
    },

    previewInNewTab() {
      const view = $perAdminApp.getView()
      const page = get(view, '/pageView/path', null)
      window.open(page + '.html', 'viewer')
    },

    itemIsTag(tagName) {
      const selection = this.getSelection(0)
      if (selection) {
        const toEl = node => node && (node.nodeType === Node.TEXT_NODE ? node.parentElement : node)
        const start = toEl(selection.startContainer)
        const end = toEl(selection.endContainer)
        return !!(
          (start && start.closest(tagName)) ||
          (end && end.closest(tagName))
        )
      }
      return false
    },

    startBrowsing(path) {
      const browsePath = path || this.browser.path.current
      $perAdminApp.getApi()
        .populateNodesForBrowser(browsePath, 'pathBrowser')
        .then(() => this.browser.open = true)
        .catch(() => {
          $perAdminApp.getApi().populateNodesForBrowser('/content', 'pathBrowser')
        })
    },

    saveSelection() {
      this.selection.buffer = saveSelection(this.getInlineContainer(), this.getInlineDoc())
      this.selection.doc = this.getInlineDoc()
      this.selection.container = this.getInlineContainer()
    },

    restoreSelection() {
      if (!this.selection.container || !this.selection.buffer || !this.selection.doc) return
      this.selection.container.focus()
      restoreSelection(this.selection.container, this.selection.buffer, this.selection.doc)
    },

    onBrowserCancel() {
      this.browser.open = false
      this.browser.withImageTab = false
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
          return
        } else if (['insertImage', 'editImage', 'editIcon'].includes(this.param.cmd)) {
          this.onImageSelect()
          return
        }

        this.execCmd(this.param.cmd, this.param.value)
        this.param.cmd = null
        this.param.value = null
        this.browser.path.selected = null
        this.browser.linkTitle = null
        this.browser.img.width = null
        this.browser.img.height = null
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
      const typeLC = this.browser.type?.toLowerCase()
      const isAssetOrFile = typeLC === PathBrowser.Type.ASSET || typeLC === 'file'
      let href = this.browser.path.selected || ''

      const isPageType = this.browser.type?.toLowerCase() === PathBrowser.Type.PAGE
      if (href.startsWith('/') && isPageType && !href.includes('.html')) {
        href += '.html'
      }

      const applyLinkAttributes = (link) => {
        link.setAttribute('href', href)
        if (this.browser.linkTitle) {
          link.setAttribute('title', this.browser.linkTitle)
        } else {
          link.removeAttribute('title')
        }
        if (isAssetOrFile) {
          link.setAttribute('download', '')
          link.removeAttribute('target')
          link.removeAttribute('rel')
        } else {
          link.removeAttribute('download')
          link.setAttribute('target', this.browser.newWindow ? '_blank' : '_self')
          link.setAttribute('rel', this.browser.rel ? 'noopener noreferrer' : '')
        }
      }

      if (this.param.cmd === 'insertLink') {
        const link = this.selection.doc.createElement('a')
        applyLinkAttributes(link)

        const range = this._savedRange
        if (!range) return

        const editorEl = this.getEditorFrom(range)
        if (!editorEl) return
        const textEditor = editorEl.closest('.inline-edit[contenteditable="true"]')
        if (!textEditor) return

        let rangeIsInListItem = false
        if (!range.startContainer.isEqualNode(range.endContainer)) {
          const listItems = Array.from(textEditor.querySelectorAll('li')).reverse()
          for (const li of listItems) {
            if (range.intersectsNode(li)) {
              rangeIsInListItem = li
              break
            }
          }
        }

        if (rangeIsInListItem) {
          range.setStart(rangeIsInListItem, 0)
          if (rangeIsInListItem.isEqualNode(range.endContainer)) {
            range.setEnd(range.endContainer, range.endOffset)
          } else {
            range.setEnd(rangeIsInListItem, rangeIsInListItem.childNodes.length)
          }
        }

        link.appendChild(range.extractContents())
        if (link.textContent.trim().length < 1) {
          link.textContent = href
        }
        range.insertNode(link)
        this._savedRange = null
        if ($perAdminApp.getNodeFromViewOrNull('/state/contentview/editor/active')) {
          $perAdminApp.action(this, 'reWrapEditable')
        }
        $perAdminApp.action(this, 'writeInlineToModel')
        this.$nextTick(() => {
          $perAdminApp.action(this, 'textEditorWriteToModel')
        })
      } else {
        let anchor = this.getLastAnchor()
        if (!anchor || !anchor.parentNode) {
          anchor = this.getAnchorAtSelection()
        }
        if (!anchor || !anchor.parentNode) {
          const container = this.getLastContainer()
          if (container) {
            anchor = container.querySelector('a')
          }
        }
        if (!anchor || !anchor.parentNode) {
          const container = this.getLastContainer()
          if (container) {
            const allAnchors = container.querySelectorAll('a')
            if (allAnchors.length > 0) {
              anchor = allAnchors[0]
            }
          }
        }
        if (!anchor || !anchor.parentNode) return
        applyLinkAttributes(anchor)
        $perAdminApp.action(this, 'textEditorWriteToModel')
        const view = $perAdminApp.getView()
        if (view) {
          const state = view.state || (view.state = {})
          const inline = state.inline || (state.inline = {})
          inline.lastAnchor = anchor
        }
      }
    },

    onImageSelect() {
      if (this.param.cmd === 'editIcon') {
        const imgEl = this.browser.element
        const styles = []
        if (this.browser.img.width) styles.push(`width: ${this.browser.img.width}px`)
        if (this.browser.img.height) styles.push(`height: ${this.browser.img.height}px`)
        if (this.browser.img.objectFit) styles.push(`object-fit: ${this.browser.img.objectFit}`)
        imgEl.setAttribute('alt', this.browser.altText || '')
        imgEl.setAttribute('style', styles.join(';'))
        const container = imgEl.closest('[data-per-inline]')
        if (container) $perAdminApp.action(this, 'writeElementToModel', container)
        this.browser.element = null
        this.browser.altText = undefined
        this.browser.withImageTab = false
        return
      }
      if (this.param.cmd === 'editImage') {
        const imgEl = this.browser.element
        const linkTitle = this.browser.linkTitle
        const styles = []
        if (this.browser.img.width) styles.push(`width: ${this.browser.img.width}px`)
        if (this.browser.img.height) styles.push(`height: ${this.browser.img.height}px`)
        if (this.browser.img.objectFit) styles.push(`object-fit: ${this.browser.img.objectFit}`)
        imgEl.setAttribute('src', this.browser.path.selected)
        imgEl.setAttribute('alt', linkTitle ? linkTitle : '')
        imgEl.setAttribute('title', linkTitle ? linkTitle : '')
        imgEl.setAttribute('style', styles.join(';'))
        if ($perAdminApp.getNodeFromViewOrNull('/state/contentview/editor/active')) {
          $perAdminApp.action(this, 'reWrapEditable')
        }
        $perAdminApp.action(this, 'writeInlineToModel')
        this.$nextTick(() => {
          $perAdminApp.action(this, 'textEditorWriteToModel')
        })
        this.browser.element = null
      } else {
        const doc = this.selection.doc || this.getInlineDoc() || document
        const win = doc.defaultView || window
        const sel = win.getSelection ? win.getSelection() : window.getSelection()

        if (sel && sel.rangeCount > 0) {
          const range = sel.getRangeAt(0)
          const styles = []
          if (this.browser.img.width) styles.push(`width: ${this.browser.img.width}px`)
          if (this.browser.img.height) styles.push(`height: ${this.browser.img.height}px`)
          if (this.browser.img.objectFit) styles.push(`object-fit: ${this.browser.img.objectFit}`)

          const img = doc.createElement('img')
          img.setAttribute('src', this.browser.path.selected)
          img.setAttribute('alt', this.browser.linkTitle || '')
          img.setAttribute('title', this.browser.linkTitle || '')
          if (styles.length) {
            img.setAttribute('style', styles.join(';'))
          }

          range.deleteContents()
          range.insertNode(img)
          range.setStartAfter(img)
          range.collapse(true)
          sel.removeAllRanges()
          sel.addRange(range)

          const editor = this.getEditorFrom(range)
          if (editor) editor.dispatchEvent(new Event('input'))
        }

        $perAdminApp.action(this, 'writeInlineToModel')
        this.$nextTick(() => {
          $perAdminApp.action(this, 'textEditorWriteToModel')
        })
      }
    },

    setBrowserPathCurrent(path) {
      this.browser.path.current = path
    },

    setBrowserPathSelected(path) {
      this.browser.path.selected = path
    },

    setBrowserResourceType(type) {
      if (!type) {
        this.browser.type = PathBrowser.Type.FILE
      } else {
        this.browser.type = type.split(':')[1]
      }
    },

    toggleBrowserNewWindow() {
      this.browser.newWindow = !this.browser.newWindow
    },

    setBrowserLinkTitle(event) {
      this.browser.linkTitle = event.target.value
    },

    setBrowserAltText(event) {
      this.browser.altText = event.target.value
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
      return !group.rules || group.rules()
    },

    groupIsActive(group) {
      if (group.isActive) {
        return group.isActive()
      }
      return group.items.filter((item) => item.isActive && item.isActive()).length > 0
    },
  },
}
</script>
