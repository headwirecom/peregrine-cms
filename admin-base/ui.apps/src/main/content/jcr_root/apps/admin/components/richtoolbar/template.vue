<template>
  <div class="richtoolbar" ref="richToolbar" :class="{disabled: !inlineRich || preview === 'preview'}">
    <richtoolbar-group
        v-if="groupAllowed(alwaysActiveGroup)"
        :icon="alwaysActiveGroup.icon"
        :iconLib="alwaysActiveGroup.iconLib"
        :collapse="!alwaysActiveGroup.noCollapse && (alwaysActiveGroup.collapse)"
        :label="alwaysActiveGroup.label"
        :title="alwaysActiveGroup.title"
        :active="false"
        :items="alwaysActiveGroup.items"
        :class="alwaysActiveGroup.class"
        @click="exec($event.btn.cmd)"/>
    <template v-for="(group, groupIndex) in filteredGroups">
      <richtoolbar-group
          :key="getKey(group, groupIndex)"
          v-if="group.items.length > 0"
          :icon="group.icon"
          :iconLib="group.iconLib"
          :collapse="!group.noCollapse && (group.collapse)"
          :label="group.label"
          :title="group.title"
          :active="groupIsActive(group)"
          :items="group.items"
          :searchable="group.searchable"
          :class="group.class"
          @toggle-click="group.toggleClick ? group.toggleClick() : () => {}"
          @click="exec($event.btn.cmd)"/>
    </template>

    <richtoolbar-group
      v-if="groupAllowed(responsiveMenuGroup)"
      :icon="responsiveMenuGroup.icon"
      :iconLib="responsiveMenuGroup.iconLib"
      :collapse="!responsiveMenuGroup.noCollapse && (responsiveMenuGroup.collapse)"
      :label="responsiveMenuGroup.label"
      :title="responsiveMenuGroup.title"
      :active="false"
      :items="responsiveMenuGroup.items"
      :class="responsiveMenuGroup.class"
      @click="exec($event.btn.cmd)"
    />

    <richtoolbar-font-size
      :exec="exec"
      :isRangeInEditor="isRangeInEditor"
      :isNodeInEditor="isNodeInEditor"
      :getDefaultFontSize="getDefaultFontSize"
      :getSelection="getSelection"
      :getEditorSelection="getEditorSelection"
    />

    <pathbrowser
        v-if="browser.open"
        :isOpen="browser.open"
        :header="browser.header"
        :browserRoot="browser.root"
        :browserType="browser.type"
        :withLinkTab="browser.withLinkTab"
        :newWindow="browser.newWindow"
        @toggle-newWindow="toggleBrowserNewWindow"
        :linkTitle="browser.linkTitle"
        :setLinkTitle="setBrowserLinkTitle"
        :currentPath="browser.path.current"
        :setCurrentPath="setBrowserPathCurrent"
        :selectedPath="browser.path.selected"
        :setSelectedPath="setBrowserPathSelected"
        :setResourceType="setBrowserResourceType"
        :rel="browser.rel"
        @toggle-rel="browser.rel = !browser.rel"
        :img-width="browser.img.width"
        @update-img-width="browser.img.width = $event"
        :img-height="browser.img.height"
        @update-img-height="browser.img.height = $event"
        :onCancel="onBrowserCancel"
        @select="onBrowserSelect"/>
  </div>
</template>

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
import {PathBrowser} from '../../../../../../js/constants'
import RichtoolbarGroup from '../richtoolbargroup/template.vue'
import RichtoolbarFontSize from '../richtoolbarfontsize/template.vue'
import Pathbrowser from '../pathbrowser/template.vue'

export default {
  name: 'RichToolbar',
  components: {RichtoolbarGroup, Pathbrowser, RichtoolbarFontSize},
  props: {
    showAlwaysActive: {
      type: Boolean,
      default: true
    },
    responsive: {
      type: Boolean,
      default: true
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
        },
        rel: true,
        img: {
          width: null,
          height: null
        }
      },
      docEl: {
        dimension: {
          w: 0
        }
      },
      size: {
        button: 34,
        group: 4
      },
			hiddenGroups: {},

			historyStack: [],
			historyIndex: -1,
			isUndoing: false,
    }
  },

  computed: {
    alwaysActiveGroup() {
      return alwaysActiveGroup(this)
    },
    groups() {
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
        link: this.link,
        insertImage: this.insertImage,
        editImage: this.editImage,
        preview: this.togglePreview,
        previewInNewTab: this.previewInNewTab,
        updateFontSize: this.updateFontSize,
        undo: this.undo,
        redo: this.redo,
      }
    },
    formattingItems() {
      const headlines = []
      for (let i = 1; i <= 6; i++) {
        headlines.push({
          label: `${this.$i18n('headline')} ${i}`,
          icon: 'title',
          class: () => this.itemIsTag(`H${i}`) ? 'active' : null,
          click: () => this.exec('formatBlock', `h${i}`)
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
    }
  },

  mounted() {
    this.$nextTick(() => {
      window.addEventListener('resize', this.updateDocElDimensions)
      this.updateDocElDimensions()
    })
		this.saveSnapshot();
		this.$watch('editorContent', (newValue) => {
			if (this.isUndoing) {
				this.isUndoing = false;
			}
			this.saveSnapshot();
		});
		if (!this.onSubNav) {
			window.addEventListener('inline-richtoolbar:cmd', this.inlineCmdHandler)
		}
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.updateDocElDimensions)
    window.removeEventListener('inline-richtoolbar:cmd', this.inlineCmdHandler)
  },

  methods: {
	 	inlineCmdHandler(event) {
			if (this.onSubNav) return;
		  this[event.detail.cmd]()
	  },
		saveSnapshot() {
			const content = this.editorContent;
			if (this.historyIndex > -1 && this.historyStack[this.historyIndex] === content) {
				return;
			}
			// If we are in the middle of stack and change something, remove future states
			if (this.historyIndex < this.historyStack.length - 1) {
				this.historyStack = this.historyStack.slice(0, this.historyIndex + 1);
			}
			this.historyStack.push(content);
			this.historyIndex++;
			// limit stack size
			if (this.historyStack.length > 50) {
				this.historyStack.shift();
				this.historyIndex--;
			}
	  },
			
		undo() {
			if (this.onSubNav) {
				window.dispatchEvent(new CustomEvent('inline-richtoolbar:cmd', { detail: { cmd: 'undo' } }))
				return;
			}
	    if (this.historyIndex > 0) {
	      this.isUndoing = true;
	      this.historyIndex--;
	      const content = this.historyStack[this.historyIndex];
				this.$el.nextElementSibling.innerHTML = content;
				$perAdminApp.action(this, 'textEditorWriteToModel')
	      // Restore focus/model
	      this.$nextTick(() => this.isUndoing = false);
	    }
	  },
	  redo() {
			if (this.onSubNav) {
				window.dispatchEvent(new CustomEvent('inline-richtoolbar:cmd', { detail: { cmd: 'redo' } }))
				return;
			}
      if (this.historyIndex < this.historyStack.length - 1) {
        this.isUndoing = true;
        this.historyIndex++;
        const content = this.historyStack[this.historyIndex];
        this.$el.nextElementSibling.innerHTML = content;
        $perAdminApp.action(this, 'textEditorWriteToModel')
        this.$nextTick(() => this.isUndoing = false);
      }
    },

    getDefaultFontSize() {
      const iframeWindow = document.querySelector("iframe#editview")?.contentWindow;
      if (!iframeWindow) return 16; // RTE not in page editor
      const currentInlineEditor = iframeWindow.document.querySelector(
        '.inline-edit[contenteditable="true"] > *'
      );
      if (!currentInlineEditor) return null;
      const defaultFontSize = iframeWindow.getComputedStyle(currentInlineEditor)
        .fontSize;
      return defaultFontSize;
    },

    // creates span for every text node, returns said nodes so they can be re-selected.
    wrapTextNodesInRange(range, fontSize) {
      const textNodes = [];

      const walker = document.createTreeWalker(
        range.commonAncestorContainer,
        NodeFilter.SHOW_TEXT,
        {
          acceptNode: node => {
            if (!node.nodeValue.trim()) return NodeFilter.FILTER_REJECT;
            const nodeRange = document.createRange();
            nodeRange.selectNodeContents(node);
            return range.intersectsNode(node) ? NodeFilter.FILTER_ACCEPT : NodeFilter.FILTER_REJECT;
          }
        }
      );

      let node;
      while (node = walker.nextNode()) {
        textNodes.push(node);
      }

      const fontSizeNodes = []
      for (let i = 0; i < textNodes.length; i++) {
        const textNode = textNodes[i];
        const nodeRange = document.createRange();
        nodeRange.selectNodeContents(textNode);

        // Adjust start if first node
        if (textNode === range.startContainer || textNode.contains(range.startContainer)) {
          nodeRange.setStart(range.startContainer, range.startOffset);
        }

        // Adjust end if last node
        if (textNode === range.endContainer || textNode.contains(range.endContainer)) {
          nodeRange.setEnd(range.endContainer, range.endOffset);
        }

        const existingSpan  = textNode.parentElement.tagName === 'SPAN' && textNode.parentElement.childNodes.length === 1 ? textNode.parentElement : null
        if (existingSpan) {
          existingSpan.style.fontSize = fontSize;
          fontSizeNodes.push(existingSpan)
        } else {
          const span = document.createElement('span');
          span.style.fontSize = fontSize;
          nodeRange.surroundContents(span);
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
      const getEditorFromEl = typeof range.startContainer.closest === 'function' ? range.startContainer : range.startContainer.parentElement
      const textEditor = getEditorFromEl.closest('.inline-edit[contenteditable="true"]')
      return textEditor
    },

    
    isRangeInEditor(range) {
      if (!range) return false
      const textEditor = this.getEditorFrom(range)
      if (!textEditor) return false
      const elementRange = textEditor.ownerDocument.createRange();
      elementRange.selectNodeContents(textEditor);

      return (
        range.compareBoundaryPoints(Range.START_TO_START, elementRange) >= 0 &&
        range.compareBoundaryPoints(Range.END_TO_END, elementRange) <= 0
      );
    },

    isNodeInEditor(node) {
      if (!node) return false
      const textEditor = this.getEditorFrom({startContainer: node})
      return textEditor.contains(node)
    },

    // for selecting updated nodes, this way range always applies to same text
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

      // not using this.getSelection(), results are inconsistant, but I don't wanna update it since other stuff relies on it.
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
        });
        element.style.fontSize = fontSizeStr
      }

      // set text size on wrapper element if nothing is selected
      const noHighlight = range.endContainer.isEqualNode(range.startContainer) && range.endOffset === range.startOffset
      if (noHighlight) {
        const parentQuery = 'p, ul, ol, h1, h2, h3, h4, h5, h6'
        const fontSizeParent = (range.startContainer.contentEditable === 'true' && range.startContainer?.children?.length > 0) ? range.startContainer.children[0] : // if focusing RTE container, goto child <p>
          typeof range.startContainer.closest === 'function' ? range.startContainer.closest(parentQuery) : range.startContainer.parentElement.closest(parentQuery) // find valid parent
        if (!textEditor.contains(fontSizeParent)) {
          console.warn('Attempting to change fontsize of paragraph outside of richtext editor', fontSizeParent, range.startContainer)
          return
        }
        setFontSizeOfEl(fontSizeParent, fontSize)
        fontSizeParent.style.fontSize = fontSize
        textEditor.dispatchEvent(new Event('input'))
        return
      }

      // split first & last text node if they are not fully selected
      if (range.startContainer.nodeType === Node.TEXT_NODE) {
        const newText = range.startContainer.splitText(range.startOffset)
        range.setStart(newText, 0)
      }
      if (range.endContainer.nodeType === Node.TEXT_NODE) {
        range.endContainer.splitText(range.endOffset)
        range.setEnd(range.endContainer, range.endContainer.length) 
      }

      // logic supporting singe nodes and re-using them instead
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

      // cleanup pointless nested spans if they exist
      textEditor.querySelectorAll('span[style*="font-size"]:has(> span[style*="font-size"])').forEach(span => {
        // :only-child still counts if element has text node siblings, so we need to check ourselves
        if (span.childNodes.length === 1) {
          span.replaceWith(span.childNodes[0]) // can only be one
        }
      })

      // Reapply selection, this encorporates split text nodes potentially created in prev step
      this.selectNodes(nodeRanges)
      const ownerDoc = nodeRanges[0].ownerDocument
      const selectionOfNodes = ownerDoc.getSelection().getRangeAt(0)

      // writing to inline causes parent rerender, which deletes existing nodes & selection. save offsets and re-apply after saving
      // mark selection nodes
      const startSelectionMarkId = crypto.randomUUID()
      const startOffset = selectionOfNodes.startOffset
      const endSelectionMarkId = crypto.randomUUID()
      const endOffset = selectionOfNodes.endOffset
      nodeRanges[0].dataset.startSelectionMarkId = startSelectionMarkId
      nodeRanges[nodeRanges.length - 1].dataset.endSelectionMarkId = endSelectionMarkId

      // save updates
      if (ownerDoc.querySelector('iframe#editview')) {
        // is sidebar edit
        $perAdminApp.action(this, 'textEditorWriteToModel')
      } else {
        // inline edit
        $perAdminApp.action(this, 'writeInlineToModel')
      }

      this.$nextTick(() => {
        this.$nextTick(() => {
          // find marked nodes and set selection again. Requires two nextTicks
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
      vm.key = vm.key === 1 ? 0 : 1
      vm.$emit('ping')
      $perAdminApp.action(vm, 'reWrapEditable')
    },
    getKey(group, index) {
      let key = `rich-toolbar-group-${index}-${group.label}`
      if (this.groupIsActive(group)) {
        key += `-${this.inlinePing}`
      }
      return key
    },
    getInlineDoc() {
      if (!this.inline) return null
      return this.inline.doc
    },
    getInlineContainer() {
      if (!this.getInlineDoc()) return
      return this.getInlineDoc().querySelector('.inline-edit.inline-editing')
    },
    execCmd(cmd, value = null, showUi = true) {
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

      this.param.cmd = 'insertLink'
      this.browser.header = this.$i18n('Insert Link')
      this.browser.path.current = this.roots.pages
      this.browser.withLinkTab = true
      this.browser.newWindow = false
      this.browser.type = PathBrowser.Type.FILE;
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
      const img = {
        width: target.style.width ? parseInt(target.style.width) : null,
        height: target.style.height ? parseInt(target.style.height) : null
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
      vm.startBrowsing()
    },
    insertIcon(imgPath) {
      console.log('imgPath: ', imgPath);
      const range = window.getSelection()?.getRangeAt(0);
      range.deleteContents()
      const fragment = range.createContextualFragment(`<img class="peregrine-icon" style="font-size: inherit; display: inline; width: auto; height: 1em; vertical-align: -0.125em;" src="${imgPath}"></img>`)
      const lastChild = fragment.lastChild;
      range.insertNode(fragment)
      range.setStartAfter(lastChild)
      range.collapse(true)
      this.getEditorFrom(range).dispatchEvent(new Event('input'))
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
          return
        } else if (['insertImage', 'editImage'].includes(this.param.cmd)) {
          this.onImageSelect()
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

    // This runs after link is chosen in modal
    onLinkSelect() {
      if (this.param.cmd === 'insertLink') {
        if (this.browser.path.selected.startsWith('/') && this.browser.type === "Page") {
          this.browser.path.selected += '.html'
        }

        const link = this.selection.doc.createElement('a')
        link.setAttribute('href', this.browser.path.selected)
        if (this.browser.linkTitle) {
          link.setAttribute('title', this.browser.linkTitle)
        }
        if (this.browser.type === "Asset" || this.browser.type === "file") {
          link.download = '';
        } else {
          link.setAttribute('target', this.browser.newWindow ? '_blank' : '_self')
          link.setAttribute('rel', this.browser.rel ? 'noopener noreferrer' : '')
        }
        
        this.restoreSelection()
        this.$nextTick(() => {
          const range = this.getSelection(0)
          const textEditor = this.getEditorFrom(range).closest('.inline-edit[contenteditable="true"]')

          // check for list elements if start & end are not in same node.
          let rangeIsInListItem = false
          if (!range.startContainer.isEqualNode(range.endContainer)){
            const listItems = Array.from(textEditor.querySelectorAll('li'));
            // reversing because we want to prioritize last item, genrally while selecting a list item selection will automatically include previous item
            listItems.reverse();
            for (let i = 0; i < listItems.length; i++) {
              const li = listItems[i];
              if (range.intersectsNode(li)) {
                // found last intersecting li node, using that, will ignore test of range
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
          // check if link text would be empty, in this case insert link href as text
          if (link.textContent.trim().length < 1) {
            link.innerText = link.getAttribute('href')
          }
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
          link.setAttribute('rel', this.browser.rel ? 'noopener noreferrer' : '')
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
        const styles = []
        if (this.browser.img.width) {
          styles.push(`width: ${this.browser.img.width}px`)
        }
        if (this.browser.img.height) {
          styles.push(`height: ${this.browser.img.height}px`)
        }
        imgEl.setAttribute('src', this.browser.path.selected)
        imgEl.setAttribute('alt', linkTitle ? linkTitle : '')
        imgEl.setAttribute('title', linkTitle ? linkTitle : '')
        imgEl.setAttribute('style', styles.join(';'))
        $perAdminApp.action(this, 'reWrapEditable')
        $perAdminApp.action(this, 'writeInlineToModel')
        this.$nextTick(() => {
          $perAdminApp.action(this, 'textEditorWriteToModel')
        })
        this.browser.element = null
      } else {
        const styles = []
        if (this.browser.img.width) {
          styles.push(`width: ${this.browser.img.width}px`)
        }
        if (this.browser.img.height) {
          styles.push(`height: ${this.browser.img.height}px`)
        }
        this.param.cmd = 'insertHTML'
        this.param.value =
            `<img src="${this.browser.path.selected}"
                  alt="${this.browser.linkTitle}"
                  title="${this.browser.linkTitle}"
                  style="${styles.join(';')}"/>`
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
        this.browser.type = PathBrowser.Type.FILE;
      } else {
        this.browser.type = type.split(":")[1];
      }
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
      return !group.rules || group.rules()
    },
    groupIsActive(group) {
      if (group.isActive) {
        return group.isActive()
      } else {
        return group.items.filter((item) => item.isActive && item.isActive()).length > 0
      }
		},
  }
}
</script>
