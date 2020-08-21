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
<template>
  <div :class="`peregrine-content-view ${viewModeClass}`">
    <div v-show="previewMode !== 'preview'"
         id="editable"
         ref="editable"
         draggable="true"
         :class="editable.class"
         :style="editable.styles"
         @dragstart="onEditableDragStart"
         @touchstart="onEditableTouchStart"
         @touchend="onEditableTouchEnd">
      <a v-if="enableEditableFeatures"
         draggable="false"
         href="#"
         class="drag-handle top-right"
         title="move component">
        <i class="material-icons">drag_handle</i>
      </a>
      <a v-if="enableEditableFeatures"
         draggable="false"
         href="#"
         class="drag-handle bottom-left"
         title="move component">
        <i class="material-icons">drag_handle</i>
      </a>
      <div v-if="enableEditableFeatures" class="editable-actions">
        <ul>
          <li class="waves-effect waves-light">
            <a href="#" :title="$i18n('copy')" @click.stop.prevent="onCopy">
              <i class="material-icons">content_copy</i>
            </a>
          </li>
          <li v-if="clipboard" class="waves-effect waves-light">
            <a :title="$i18n('paste')" href="#" @click.stop.prevent="onPaste">
              <i class="material-icons">content_paste</i>
            </a>
          </li>
          <li v-if="isSelected" class="waves-effect waves-light">
            <a href="#" :title="$i18n('deleteComponent')" @click.stop.prevent="onDelete">
              <i class="material-icons">delete</i>
            </a>
          </li>
        </ul>
      </div>
    </div>
    <template>
      <div v-if="!iframe.loaded" class="spinner-wrapper">
        <admin-components-materializespinner/>
      </div>
      <iframe
          v-show="iframe.loaded"
          id="editview"
          ref="editview"
          :src="pagePath"
          :data-per-mode="previewMode"
          @load="onIframeLoaded"/>
    </template>
    <admin-components-addcomponentmodal
        v-if="iframe.win"
        :selected-component="component"
        :is-drop-target="componentIsDropTarget"
        :windows="[rootWin, iframe.win]"
        @component-added="onAddComponentModalComponentAdded"/>
  </div>
</template>

<script>
import {Attribute, Key, Toast} from '../../../../../../js/constants'
import {Error} from '../../../../../../js/messages'
import {
  get,
  getCaretCharacterOffsetWithin,
  isChromeBrowser,
  restoreSelection,
  saveSelection,
  set
} from '../../../../../../js/utils'

export default {
    props: ['model'],
    data() {
      return {
        rootWin: window,
        target: null,
        previousTarget: null,
        inline: null,
        scrollTop: 0,
        dragging: false,
        autoSave: false,
        editing: false,
        editable: {
          visible: false,
          class: null,
          timer: null,
          delay: 200,
          styles: {
            top: 0,
            left: 0,
            width: 0,
            height: 0
          }
        },
        selected: {
          draggable: true
        },
        iframe: {
          loaded: false,
          win: null, doc: null, html: null, body: null, head: null, app: null,
          scrollTop: 0,
          timeout: null,
          delay: 0,
          mouseOverCmp: null,
          dimension: {w: 0, h: 0, x: 0, y: 0}
        },
        clipboard: null,
        ctrlDown: false,
        isTouch: false,
        isIOS: false,
        caret: {
          pos: -1,
          counter: 0
        },
        holdingDown: false,
        inlineEdit: {
          firstTime: [],
          selection: null
        },
        toast: {
          templateComponent: null,
          missingEventPath: null
        }
      }
    },
    computed: {
      component() {
        if (this.target) {
          return this.findComponentEl(this.target)
        } else {
          return null
        }
      },
      previousComponent() {
        if (this.previousTarget) {
          return this.findComponentEl(this.previousTarget)
        } else {
          return null
        }
      },
      path() {
        if (this.component) {
          return this.getPath(this.component)
        } else {
          return null
        }
      },
      dropTarget() {
        if (!this.target) return
        return this.target.getAttribute(Attribute.DROPTARGET) === 'true'
      },
      dropLocation() {
        return this.target.getAttribute(Attribute.LOCATION)
      },
      targetInline() {
        return this.target.getAttribute(Attribute.INLINE)
      },
      view() {
        return $perAdminApp.getView()
      },
      pageView() {
        return this.view.pageView
      },
      node() {
        const path = get(this.view, '/state/editor/path', null)
        if (path) {
          return $perAdminApp.findNodeFromPath(this.view.pageView.page, path)
        } else {
          return null
        }
      },
      isSelected() {
        return this.component && this.path && this.path !== '/jcr:content'
      },
      pagePath() {
        return $perAdminApp.getNodeFromView('/pageView/path') + '.html'
      },
      previewMode() {
        const ws = $perAdminApp.getNodeFromViewOrNull('/state/tools/workspace')
        return ws ? ws.preview : ''
      },
      viewMode() {
        const viewMode = $perAdminApp.getNodeFromViewOrNull('/state/tools/workspace/view')
        const previewMode = $perAdminApp.getNodeFromViewOrNull('/state/tools/workspace/preview')
        let ret
        if (viewMode) {
          ret = viewMode + (previewMode ? ' ' + previewMode : '')
        } else {
          ret = 'desktop' + (previewMode ? ' ' + previewMode : '')
        }
        return ret
      },
      viewModeClass() {
        return this.viewMode
      },
      enableEditableFeatures() {
        if (this.path === undefined || this.path === null || this.dragging) return false

        const node = $perAdminApp.findNodeFromPath(this.view.pageView.page, this.path)
        if (!node) {
          return false
        }
        return !node.fromTemplate
      },
      isTemplateNode() {
        return $perAdminApp.findNodeFromPath(this.pageView.page, this.path).fromTemplate === true
      },
      isRich() {
        return get(this.view, '/state/inline/rich', false)
      },
      componentTitle() {
        const componentName = this.view.state.editor.component.split('-').join('/')
        const components = this.view.admin.components.data
        for (let i = 0; i < components.length; i++) {
          const component = components[i]
          if (component.path.endsWith(componentName)) {
            return component.title
          }
        }
      },
      componentIsDropTarget() {
        if (!this.component) return false

        const selector = `[${Attribute.PATH}="${this.path}"][${Attribute.DROPTARGET}]`
        const dropTargetElements = this.component.querySelectorAll(selector)

        return dropTargetElements.length > 0;
      }
    },
    watch: {
      target(val, oldVal) {
        this.previousTarget = oldVal
        if (val) {
          this.selectComponent(this)
        } else {
          this.unselect(this)
        }
      },
      scrollTop() {
        if (this.target) {
          this.wrapEditableAroundSelected()
        } else {
          this.wrapEditableAroundElement(this.iframe.mouseOverCmp)
        }
      },
      'view.state.tools.workspace.view'() {
        this.$nextTick(() => {
          this.wrapEditableAroundSelected()
        })
      },
      'pageView.path'() {
        this.unselect(this)
      },
      node: {
        deep: true,
        handler(val) {
          if (!this.component) return
          this.wrapEditableAroundSelected()
          this.$nextTick(() => {
            this.refreshIframeElements()
          })
        }
      },
      previewMode(val) {
        if (val === 'preview') {
          this.iframePreviewMode()
          this.editable.class = null
        } else {
          this.iframeEditMode()
          if (this.component) {
            clearTimeout(this.editable.timer)
            this.editable.timer = setTimeout(() => {
              this.editable.class = 'selected'
              this.wrapEditableAroundSelected()
            }, this.editable.delay)
          }
        }
      },
      'iframe.dimension': {
        deep: true,
        handler() {
          if (this.target) {
            this.wrapEditableAroundSelected()
          } else {
            this.wrapEditableAroundElement(this.iframe.mouseOverCmp)
          }
        }
      },
      'toast.templateComponent'(val, old) {
        if (old) {
          old.remove()
        }
      },
      'toast.missingEventPath'(val, old) {
        if (old) {
          old.remove()
        }
      }
    },
    mounted() {
      const vm = this
      vm.$nextTick(() => {
        /* is this a touch device */
        vm.isTouch = 'ontouchstart' in window || navigator.maxTouchPoints
        vm.isIOS = /iPad|iPhone|iPod/.test(navigator.userAgent) && !window.MSStream
        if (vm.isTouch) {
          /* selected components are not immediatly draggable on touch devices */
          vm.selected.draggable = false
        }
        set(vm.view, '/state/editorVisible', false)
        set(vm.view, '/state/editor/path', null)
        set(vm.view, '/state/inline/rich', null)

        if (this.previewMode !== 'preview') {
          set($perAdminApp.getView(), '/state/contentview/editor/active', true)
        } else {
          set($perAdminApp.getView(), '/state/contentview/editor/active', false)
        }
      })
    },
    beforeDestroy() {
      set($perAdminApp.getView(), '/state/contentview/editor/active', false)
    },
    methods: {
      componentKey(component) {
        if (component.variation) {
          return component.path + ':' + component.variation
        } else {
          return component.path
        }
      },
      componentDisplayName(component) {
        if (component.title) {
          return component.title
        } else {
          return component.path.split('/')[2] + ' ' + component.name
        }
      },

      selectComponent(vm, el = vm.target) {
        vm.target = el
        if (!vm.target || !vm.component || !vm.path) return

        if (!vm.dragging && vm.isTemplateNode) {
          vm.unselect(vm)
          this.toast.templateComponent = $perAdminApp.toast(vm.$i18n('fromTemplateNotifyMsg'), Toast.Level.WARNING)
        } else {
          if (vm.dragging || vm.path !== '/jcr:content') {
            vm.wrapEditableAroundSelected()
            vm.editable.class = 'selected'
          }
          if (!vm.dragging) {
            if (vm.component !== vm.previousComponent) {
              set(this.view, '/state/inline/rich', null)
              set(this.view, '/state/inline/model', null)
              if (vm.autoSave && vm.node && vm.view.state.editor.path) {
                vm.autoSave = false
                $perAdminApp.stateAction('savePageEdit', {
                  data: vm.node,
                  path: vm.view.state.editor.path
                }).then(() => {
                  $perAdminApp.action(vm, 'showComponentEdit', vm.path).then(() => {
                    vm.flushInlineState()
                    vm.$nextTick(vm.pingToolbar)
                  })
                })
              } else {
                $perAdminApp.action(vm, 'showComponentEdit', vm.path).then(() => {
                  vm.flushInlineState()
                  vm.$nextTick(vm.pingToolbar)
                })
              }
            } else {
              vm.flushInlineState()
            }
          }
        }
      },

      unselect(vm) {
        vm.target = null
        vm.editable.class = null
        vm.autoSave = false
        set(vm.view, '/state/inline/rich', false)
      },

      flushInlineState() {
        if (this.inline) {
          set(this.view, '/state/inline/model', this.inline)
          this.inline = null
        }
      },

      findComponentEl(targetEl) {
        let el = targetEl
        while (!el.getAttribute(Attribute.PATH) || el.getAttribute(Attribute.DROPTARGET)) {
          el = el.parentElement
          if (!el) {
            break
          }
        }
        return el
      },

      findVnode(vmCmp, fullPath) {
        fullPath.reverse()
        let vnode = vmCmp._vnode
        let startIndex = 1
        fullPath.some((el) => {
          if (el !== vnode.elm) {
            startIndex++
          } else {
            return true
          }
        })
        const path = fullPath.slice(startIndex)
        path.reverse()
        while (path.length > 0 && vnode.children && vnode.children.length > 0) {
          const wanted = path.pop()
          vnode.children.some((child) => {
            if (child.elm === wanted) {
              vnode = child
              return true
            }
          })
        }
        return vnode
      },

      writeInlineToModel(vm = this) {
        let content = ''
        if (vm.isRich) {
          content = vm.target.innerHTML.replace(/(?:\r\n|\r|\n)/g, '<br>')
        } else {
          content = vm.target.innerText
        }
        const dataInline = vm.targetInline.split('.').slice(1)
        dataInline.reverse()
        let parentProp = vm.node
        while (dataInline.length > 1) {
          parentProp = parentProp[dataInline.pop()]
        }
        parentProp[dataInline.pop()] = content
      },

      onInlineEdit(event) {
        if (!this.inlineEdit.firstTime.includes(event.target)) {
          this.inlineEdit.firstTime.push(event.target)
          this.inlineEdit.selection = saveSelection(event.target, this.iframe.doc)
        }

        this.target = event.target
        const eventPath = event.path || (event.composedPath && event.composedPath())

        if (!eventPath) {
          this.toast.missingEventPath = $perAdminApp.toast(Error.MISSING_EVENT_PATH, Toast.Level.ERROR)
          throw Error.MISSING_EVENT_PATH
        }

        const vnode = this.findVnode(this.component.__vue__, eventPath)
        const attr = this.isRich ? 'innerHTML' : 'innerText'
        if (vnode.data.domProps) {
          if (this.isRich) {
            vnode.data.domProps.innerHTML = this.target.innerHTML.replace(/(?:\r\n|\r|\n)/g, '<br>')
          } else {
            vnode.data.domProps.innerHTML = this.target.innerText
          }
        }
        this.writeInlineToModel()

        if (this.inlineEdit.selection) {
          this.$nextTick(() => {
            this.$nextTick(() => {
              restoreSelection(event.target, this.inlineEdit.selection, this.iframe.doc)
              this.inlineEdit.selection = null
            })
          })
        }

        this.autoSave = true
        this.reWrapEditable()
      },

      onInlineClick(event) {
        this.pingToolbar()
      },

      onInlineFocus(event) {
        event.target.classList.add('inline-editing')
        if (isChromeBrowser() && event.target.innerHTML) {
          event.target.innerHTML = event.target.innerHTML.trim()
        }
        this.editing = true
        this.caret.pos = -1
        this.caret.counter = 0
        this.target = event.target
        const dataInline = this.targetInline.split('.').slice(1)
        this.inline = dataInline.join('.')
        set(this.view, '/state/inline/doc', this.iframe.doc)
      },

      onInlineFocusOut(event) {
        event.target.classList.remove('inline-editing')
        this.editing = false
        set(this.view, '/state/inline/doc', null)
        if (!isChromeBrowser() && event.target.innerHTML) {
          event.target.innerHTML = event.target.innerHTML.trim()
        }
      },

      onInlineKeyDown(event) {
        this.pingToolbar()
        const key = event.which
        const shift = event.shiftKey
        const ctrlOrCmd = event.ctrlKey || event.metaKey
        const backspaceOrDelete = key === Key.BACKSPACE || key === Key.DELETE
        const arrowKey = key >= Key.ARROW_LEFT && key <= Key.ARROW_DOWN

        if (key === Key.A && ctrlOrCmd) {
          this.onInlineSelectAll(event)
        } else if (backspaceOrDelete) {
          this.onInlineDelete(event)
        } else if (arrowKey && !shift) {
          this.onInlineArrowKey(event)
        }
        this.holdingDown = true
      },

      onInlineKeyUp(event) {
        this.pingToolbar()
        const key = event.which
        const shift = event.shiftKey
        const ctrlOrCmd = event.ctrlKey || event.metaKey
        const backspaceOrDelete = key === Key.BACKSPACE || key === Key.DELETE
        const arrowKey = key >= Key.ARROW_LEFT && key <= Key.ARROW_DOWN

        if (arrowKey && !shift) {
          this.onInlineArrowKey(event, true)
        }
        this.holdingDown = false
      },

      onInlineDblClick(event) {
        if (event.target.tagName === 'IMG') {
          $perAdminApp.action(this, 'editImage', event.target)
        }
      },

      onInlineSelectAll(event) {
        event.preventDefault()
        let range, selection
        selection = this.iframe.win.getSelection()
        range = this.iframe.doc.createRange()
        range.selectNodeContents(event.target)
        selection.removeAllRanges()
        selection.addRange(range)
      },

      onInlineDelete(event) {
        const selection = this.iframe.win.getSelection()
        if (selection.rangeCount > 1 && selection.anchorNode === this.target) {
          event.preventDefault();
          this.iframe.doc.execCommand('delete')
          this.target.innerHTML = ''
          this.writeInlineToModel()
        }
      },

      onInlineArrowKey(event, isKeyUp = false) {
        if (isChromeBrowser()) return

        const key = event.which
        const newCaretPos = getCaretCharacterOffsetWithin(event.target)
        if (this.caret.pos === newCaretPos && (isKeyUp || this.holdingDown)) {
          this.caret.counter++
          if (this.caret.counter < 2) return
          const inlineEditNodes = this.iframe.app.querySelectorAll(`[${Attribute.INLINE}]`)
          if (inlineEditNodes.length <= 1) return
          const len = inlineEditNodes.length

          for (let i = 0; i < len; i++) {
            if (inlineEditNodes[i] === this.target) {
              if (i > 0 && (key === Key.ARROW_LEFT || key === Key.ARROW_UP)) {
                this.placeCaretAtEnd(inlineEditNodes[i - 1])
              } else if (i < len - 1 && (key === Key.ARROW_RIGHT || key === Key.ARROW_DOWN)) {
                inlineEditNodes[i + 1].focus()
              }
              break
            }
          }
        }
        this.caret.pos = newCaretPos
      },

      onIframeLoaded(ev) {
        this.iframe.loaded = true
        this.iframe.win = this.$refs.editview.contentWindow
        this.iframe.doc = this.iframe.win.document
        this.iframe.html = this.iframe.doc.querySelector('html')
        this.iframe.body = this.iframe.doc.querySelector('body')
        this.iframe.head = this.iframe.doc.querySelector('head')
        this.iframe.app = this.iframe.doc.querySelector('#peregrine-app')
        this.iframe.win.addEventListener('resize', this.updateIframeDimensions);
        this.updateIframeDimensions()
        this.addIframeExtraStyles()
        this.refreshIframeElements()
        if (this.previewMode !== 'preview') {
          this.iframeEditMode()
        } else {
          this.iframePreviewMode()
        }
      },

      onIframeClick(ev) {
        if (!this.isContentEditableOrNested(ev.target)) {
          this.target = ev.target
        }
      },

      onIframeScroll() {
        this.scrollTop = this.iframe.html.scrollTop
      },

      onIframeDragOver(event) {
        event.preventDefault()
        this.dragging = true
        this.target = event.target

        if (this.component) {
          const isRoot = this.isTemplateNode
          const relMousePos = this.getRelativeMousePosition(event)

          if (this.dropTarget) {
            const dropLocation = this.dropLocation
            if (relMousePos.yPercentage <= 30 && dropLocation === 'before' && !isRoot) {
              this.dropPosition = 'before'
              this.editable.class = 'drop-top'
            } else if (relMousePos.yPercentage >= 70 && dropLocation === 'after' && !isRoot) {
              this.dropPosition = 'after'
              this.editable.class = 'drop-bottom'
            } else if (dropLocation) {
              this.dropPosition = 'into-' + dropLocation
              this.editable.class = 'selected'
            } else {
              this.dropPosition = 'none'
            }
          } else if (!isRoot) {
            if (relMousePos.yPercentage <= 43.5) {
              this.dropPosition = 'before'
              this.editable.class = 'drop-top'
            } else {
              this.dropPosition = 'after'
              this.editable.class = 'drop-bottom'
            }
          } else {
            this.editable.class = 'selected'
            if (relMousePos.yPercentage <= 43.5) {
              this.dropPosition = 'into-before'
            } else {
              this.dropPosition = 'into-after'
            }
          }
        } else {
          this.dropPosition = 'none'
        }
      },

      onIframeDrop(event) {
        event.preventDefault()
        this.dragging = false
        this.target = event.target
        if (this.isTouch) {
          this.selected.draggable = false
        }
        if (typeof this.component === 'undefined' || this.component === null) return false

        const componentPath = event.dataTransfer.getData('text')
        if (this.path === componentPath) {
          event.dataTransfer.clearData('text')
          return false
        }
        const view = this.view
        const payload = {
          pagePath: view.pageView.path,
          path: this.path,
          component: componentPath,
          drop: this.dropPosition
        }
        let addOrMove
        if (componentPath.includes('/components/')) {
          addOrMove = 'addComponentToPath';
        } else {
          addOrMove = 'moveComponentToPath';
          const targetNode = $perAdminApp.findNodeFromPath(this.view.pageView.page, this.path)
          if (!targetNode || targetNode.fromTemplate) {
            $perAdminApp.notifyUser('template component',
                'You cannot drag a component into a template section')
            this.unselect(this)
            return false;
          }
          this.cleanUpAfterDelete(componentPath)
        }
        $perAdminApp.stateAction(addOrMove, payload).then((data) => {
          this.refreshIframeElements()
        })
        this.unselect(this)
        event.dataTransfer.clearData('text')
      },

      onIframeMouseOver(event) {
        if (this.editable.class === 'selected') return

        const cmpEl = this.findComponentEl(event.target)

        if (!cmpEl) {
          this.editable.visible = false
          return
        }

        this.iframe.mouseOverCmp = cmpEl
        this.wrapEditableAroundElement(cmpEl)

        if (this.isFromTemplate(cmpEl)) {
          this.editable.class = 'mouseover-orange'
        } else {
          this.editable.class = 'mouseover-green'
        }

        this.editable.visible = true
      },

      refreshIframeElements() {
        this.refreshInlineEditElements()
      },

      refreshInlineEditElements() {
        const selector = `[${Attribute.INLINE}]:not(.inline-edit)`
        const elements = this.iframe.app.querySelectorAll(selector)
        if (!elements || elements.length <= 0) return

        elements.forEach((el) => {
          if (this.isFromTemplate(el)) return

          el.classList.add('inline-edit')
          if (el.children.length === 0) {
            el.appendChild(document.createTextNode(' '))
          }
          el.addEventListener('input', this.onInlineEdit)
          el.addEventListener('click', this.onInlineClick)
          el.addEventListener('focus', this.onInlineFocus)
          el.addEventListener('focusout', this.onInlineFocusOut)
          el.addEventListener('keydown', this.onInlineKeyDown)
          el.addEventListener('keyup', this.onInlineKeyUp)
          el.addEventListener('dblclick', this.onInlineDblClick)
          el.setAttribute('contenteditable', this.previewMode !== 'preview' + '')
        })
      },

      iframeEditMode() {
        set($perAdminApp.getView(), '/state/contentview/editor/active', true)
        this.iframe.doc.addEventListener('click', this.onIframeClick)
        this.iframe.doc.addEventListener('scroll', this.onIframeScroll)
        this.iframe.doc.addEventListener('dragover', this.onIframeDragOver)
        this.iframe.doc.addEventListener('drop', this.onIframeDrop)
        this.iframe.doc.addEventListener('mouseover', this.onIframeMouseOver)
        this.iframe.html.classList.add('edit-mode')
        const elements = this.iframe.app.querySelectorAll(`[${Attribute.INLINE}]`)
        elements.forEach((el, index) => {
          if (this.isFromTemplate(el)) return
          el.setAttribute('contenteditable', 'true')
        })
        this.iframe.body.setAttribute('contenteditable', 'true')
        this.iframe.doc.getElementById('peregrine-app').setAttribute('contenteditable', 'false')
      },

      iframePreviewMode() {
        set($perAdminApp.getView(), '/state/contentview/editor/active', false)
        try {
          this.iframe.doc.removeEventListener('click', this.onIframeClick)
          this.iframe.doc.removeEventListener('scroll', this.onIframeScroll)
          this.iframe.doc.removeEventListener('mouseover', this.onIframeScroll)
        } catch (err) {
          console.debug('no event listener to be removed from iframe', err)
        }
        if (this.iframe.body) {
          this.iframe.body.setAttribute('contenteditable', 'false')
        }
        if (this.iframe.html) {
          this.iframe.html.classList.remove('edit-mode')
        }
        if (this.iframe.app) {
          const elements = this.iframe.app.querySelectorAll(`[${Attribute.INLINE}]`)
          elements.forEach((el, index) => {
            if (this.isFromTemplate(el)) return
            el.setAttribute('contenteditable', 'false')
          })
        }
      },

      addIframeExtraStyles() {
        if (this.iframe.head.querySelector('#editing-extra-styles')) return
        const css = `
          html.edit-mode body {
            cursor: default !important
          }
          html.edit-mode #peregrine-app [contenteditable="true"]:focus {
            outline: 1px dotted #fe9701 !important;
          }

          html.edit-mode #peregrine-app [contenteditable="true"]:hover:not(:focus) {
            outline: 1px dotted #ffc171 !important;
          }

          html.edit-mode #peregrine-app .from-template {
            cursor: not-allowed !important;
          }

          html.edit-mode #peregrine-app .from-template * {
            cursor: not-allowed !important;
          }

          html.edit-mode #peregrine-app .inline-edit {
            cursor: text !important
          }`
        const style = this.iframe.doc.createElement('style')
        this.iframe.head.appendChild(style)
        style.type = 'text/css'
        style.appendChild(this.iframe.doc.createTextNode(css))
        style.setAttribute('id', 'editing-extra-styles')
      },

      isContentEditableOrNested(el) {
        const component = this.findComponentEl(el)
        let found = el

        if (el === component) return el.getAttribute('contenteditable') === 'true'

        while (el.getAttribute('contenteditable') !== 'true') {
          el = el.parentElement
          if (!el || el === component) {
            return false
          }
        }
        return el.getAttribute('contenteditable') === 'true'
      },

      wrapEditableAroundElement(el) {
        if (!el) return

        this.$nextTick(() => {
          const {top, left, width, height} = this.getBoundingClientRect(el)
          const offset = this.getBoundingClientRect(this.$refs.editview)

          this.editable.styles.top = `${top}px`
          this.editable.styles.left = `${left + offset.left}px`
          this.editable.styles.width = `${width}px`
          this.editable.styles.height = `${height}px`
        })
      },

      wrapEditableAroundSelected() {
        this.wrapEditableAroundElement(this.component)
      },

      reWrapEditable(vm = this) {
        vm.editable.timer = setTimeout(() => {
          vm.editable.class = 'selected'
          vm.wrapEditableAroundSelected()
        }, vm.editable.delay)
      },

      getElementStyle(e, styleName) {
        let styleValue = '';
        if (document.defaultView && document.defaultView.getComputedStyle) {
          styleValue = document.defaultView.getComputedStyle(e, '').getPropertyValue(styleName);
        } else if (e.currentStyle) {
          styleName = styleName.replace(/-(\w)/g, (strMatch, p1) => {
            return p1.toUpperCase();
          });
          styleValue = e.currentStyle[styleName];
        }
        return styleValue;
      },

      getBoundingClientRect(e) {
        const rect = e.getBoundingClientRect()
        const marginTop = parseFloat(this.getElementStyle(e, 'margin-top'))
        const marginLeft = parseFloat(this.getElementStyle(e, 'margin-left'))
        const marginRight = parseFloat(this.getElementStyle(e, 'margin-right'))
        const marginBottom = parseFloat(this.getElementStyle(e, 'margin-bottom'))
        const newRect = {
          left: rect.left - (marginLeft > 0 ? marginLeft : 0),
          right: rect.right + (marginRight > 0 ? marginRight : 0),
          top: rect.top - marginTop,
          bottom: rect.bottom + marginBottom,
        }
        newRect.width = newRect.right - newRect.left
        newRect.height = newRect.bottom - newRect.top
        return newRect;
      },

      removeEditable() {
        this.target = null
        this.editable.class = null
        if (this.isTouch) {
          this.selected.draggable = false
        }
      },

      getRelativeMousePosition(event) {
        const offset = this.getBoundingClientRect(this.component)
        return {
          width: offset.width,
          x: event.pageX - offset.left,
          xPercentage: (event.pageX - offset.left) / offset.width * 100,
          height: offset.height,
          y: event.pageY - offset.top - this.scrollTop,
          yPercentage: (event.pageY - offset.top - this.scrollTop) / offset.height * 100
        }
      },

      getPath(el) {
        const component = this.findComponentEl(el)
        return component.getAttribute(Attribute.PATH)
      },

      isFromTemplate(el) {
        return $perAdminApp.findNodeFromPath(this.pageView.page, this.getPath(el)).fromTemplate
      },

      /* Drag and Drop ===========================
      ============================================ */
      onEditableDragStart(ev) {
        if (this.component === null) return

        this.editable.class = 'dragging'
        ev.dataTransfer.setData('text', this.path)
        ev.dataTransfer.setDragImage(this.component, 400, 0)
      },

      /* Editable methods ========================
      ============================================ */
      onEditableTouchStart(ev) {
        this.editable.timer = setTimeout(this.onLongTouchOverlay, 800)
      },

      onEditableTouchEnd(ev) {
        clearTimeout(this.editable.timer)
      },

      onLongTouchOverlay() {
        if (this.component === null) return

        this.selected.draggable = true
        this.editable.class = 'draggable'
      },

      onDelete(e) {
        const view = this.view
        const pagePath = view.pageView.path
        const payload = {
          pagePath: view.pageView.path,
          path: this.path
        }
        if (payload.path !== '/jcr:content') {
          $perAdminApp.stateAction('deletePageNode', payload).then((data) => {
            this.cleanUpAfterDelete(payload.path)
            this.refreshIframeElements()
          })
        }
        this.unselect(this)
      },

      cleanUpAfterDelete(path) {
        const selector = `[${Attribute.PATH}="${path}"]`
        const remains = this.iframe.app.querySelectorAll(selector)
        if (remains.length <= 0) return
        remains.forEach((remain) => {
          remain.remove()
        })
      },

      onCopy(e) {
        this.clipboard = $perAdminApp.findNodeFromPath(
            this.view.pageView.page,
            this.path
        )
      },

      onPaste(e) {
        const nodeFromClipboard = this.clipboard
        const view = this.view
        const isDropTarget = this.dropTarget === 'true'
        let dropPosition
        isDropTarget ? dropPosition = 'into' : dropPosition = 'after'
        const payload = {
          pagePath: view.pageView.path,
          data: nodeFromClipboard,
          path: this.path,
          drop: dropPosition
        }
        $perAdminApp.stateAction('addComponentToPath', payload).then((data) => {
          this.refreshIframeElements()
        })
      },

      placeCaretAtEnd(el) {
        const doc = el.ownerDocument
        const win = doc.defaultView || doc.parentWindow
        el.focus()
        if (typeof win.getSelection != 'undefined' && typeof doc.createRange != 'undefined') {
          const range = doc.createRange()
          range.selectNodeContents(el)
          range.collapse(false)
          const sel = win.getSelection()
          sel.removeAllRanges()
          sel.addRange(range)
        } else if (typeof doc.body.createTextRange != 'undefined') {
          const textRange = doc.body.createTextRange()
          textRange.moveToElementText(el)
          textRange.collapse(false)
          textRange.select()
        }
      },

      pingToolbar() {
        $perAdminApp.action(this, 'pingRichToolbar')
      },

      onAddComponentModalComponentAdded(newNode) {
        this.refreshIframeElements()
        const selector = `[${Attribute.PATH}="${newNode.path}"]`
        const newNodeEl = this.iframe.app.querySelector(selector)
        const firstInlineEditEl = newNodeEl.querySelector(`[${Attribute.INLINE}]`)

        if (firstInlineEditEl) {
          firstInlineEditEl.focus()
        } else {
          this.$nextTick(() => {
            newNodeEl.click()
            this.scrollIntoViewCenter(newNodeEl, this.iframe.doc, this.iframe.win)
          })
        }
      },

      scrollIntoViewCenter(el, doc, win) {
        el.scrollIntoView(true)
        const viewportH = Math.max(doc.documentElement.clientHeight, win.innerHeight || 0)
        win.scrollBy(0, (el.getBoundingClientRect().height - viewportH) / 2)
      },

      onComponentMouseEnter(event) {
        event.stopPropagation()
        const cls = event.target.getAttribute('class')

        if (this.isFromTemplate(event.target)) {
          event.target.setAttribute('class', 'outline-orange ' + cls)
        } else {
          event.target.setAttribute('class', 'outline-green ' + cls)
        }
      },

      onComponentMouseLeave(event) {
        event.stopPropagation()
        event.target.classList.remove('outline-orange', 'outline-green')
      },

      updateIframeDimensions() {
        clearTimeout(this.iframe.timeout)
        this.iframe.timeout = setTimeout(() => {
          this.iframe.dimension.w = this.iframe.doc.documentElement.clientWidth
          this.iframe.dimension.h = this.iframe.doc.documentElement.clientHeight
        }, this.iframe.delay)
      }
    }
  }
</script>