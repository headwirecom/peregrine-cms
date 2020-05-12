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
    <div id="editable"
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
    <div ref="addComponentModal"
         v-show="addComponentModal.visible"
         style="background: silver; position: absolute; top: 10px; bottom: 10px; left: 10px; width: 300px; z-index: 2; overflow-y: scroll;">
      <input ref="addComponentModalFilter" type="text" v-model="addComponentModal.filter">
      <button v-on:click="addComponentFromModal(componentKey(component))"
              style="width: 100%;"
              v-for="component in allowedComponents"
              v-bind:key="component.path + '|' + component.variation">
        {{componentDisplayName(component)}}
      </button>
    </div>
  </div>
</template>

<script>
  import {Attribute, Key} from '../../../../../../js/constants'
  import {get, getCaretCharacterOffsetWithin, set} from '../../../../../../js/utils'

  export default {
    props: ['model'],
    data() {
      return {
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
          scrollTop: 0
        },
        clipboard: null,
        ctrlDown: false,
        isTouch: false,
        isIOS: false,
        addComponentModal: {
          visible: false,
          filter: ''
        },
        caretPos: -1,
        holdingDown: false
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
        return this.target.getAttribute(Attribute.DROPTARGET)
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
        return get(this.view, '/state/editor/inline/rich', false)
      },
      allowedComponents() {
        return get(this.view, '/admin/components/data', [])
            .filter(el => {
              if (!this.view.state.tenant || !this.view.state.tenant.name) return false
              if (el.group === '.hidden') return false
              if (!this.componentDisplayName(el).toLowerCase().startsWith(
                  this.addComponentModal.filter.toLowerCase())) {
                return false
              }
              return el.path.startsWith('/apps/' + this.view.state.tenant.name + '/')
            })
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
        this.wrapEditableAroundSelected()
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
            this.refreshInlineEditClones()
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
      }
    },
    mounted() {
      this.$nextTick(() => {
        /* is this a touch device */
        this.isTouch = 'ontouchstart' in window || navigator.maxTouchPoints
        this.isIOS = /iPad|iPhone|iPod/.test(navigator.userAgent) && !window.MSStream
        if (this.isTouch) {
          /* selected components are not immediatly draggable on touch devices */
          this.selected.draggable = false
        }
        set(this.view, '/state/editorVisible', false)
        set(this.view, '/state/editor/path', null)
      })
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
          $perAdminApp.toast(vm.$i18n('fromTemplateNotifyMsg'), 'warn')
        } else {
          if (vm.path !== '/jcr:content') {
            vm.wrapEditableAroundSelected()
            vm.editable.class = 'selected'
          }
          if (!vm.dragging) {
            if (vm.component !== vm.previousComponent) {
              if (vm.autoSave) {
                vm.autoSave = false
                $perAdminApp.stateAction('savePageEdit', {
                  data: vm.node,
                  path: vm.view.state.editor.path
                }).then(() => {
                  $perAdminApp.action(vm, 'showComponentEdit', vm.path).then(() => {
                    vm.flushInlineState()
                  })
                })
              } else {
                $perAdminApp.action(vm, 'showComponentEdit', vm.path).then(() => {
                  vm.flushInlineState()
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
      },

      flushInlineState() {
        if (this.inline) {
          set(this.view, '/state/editor/inline/model', this.inline)
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

      writeInlineToModel() {
        const dataInline = this.targetInline.split('.').slice(1)
        dataInline.reverse()
        let parentProp = this.node
        while (dataInline.length > 1) {
          parentProp = parentProp[dataInline.pop()]
        }
        parentProp[dataInline.pop()] = this.isRich ? this.target.innerHTML : this.target.innerText
      },

      onInlineEdit(event) {
        this.target = event.target
        this.writeInlineToModel()
        this.autoSave = true
        this.reWrapEditable()
      },

      onInlineFocus(event) {
        event.target.classList.add('inline-editing')
        this.editing = true
        this.caretPos = -1
        this.target = event.target
        const dataInline = this.targetInline.split('.').slice(1)
        this.inline = dataInline.join('.')
      },

      onInlineFocusOut(event) {
        event.target.classList.remove('inline-editing')
        this.editing = false
      },

      onInlineKeyDown(event) {
        const key = event.which
        const ctrlOrCmd = event.ctrlKey || event.metaKey
        const backspaceOrDelete = key === Key.BACKSPACE || key === Key.DELETE
        const arrowKey = key >= Key.ARROW_LEFT && key <= Key.ARROW_DOWN

        if (key === Key.A && ctrlOrCmd) {
          this.onInlineSelectAll(event)
        } else if (backspaceOrDelete) {
          this.onInlineDelete(event)
        } else if (key === Key.DOT && ctrlOrCmd) {
          this.addComponent(true)
        } else if (key === Key.COMMA && ctrlOrCmd) {
          this.addComponent(false)
        } else if (arrowKey) {
          this.onInlineArrowKey(event)
        }
        this.holdingDown = true
      },

      onInlineKeyUp(event) {
        const key = event.which
        const arrowKey = key >= Key.ARROW_LEFT && key <= Key.ARROW_DOWN

        if (arrowKey) {
          this.onInlineArrowKey(event, true)
        }
        this.holdingDown = false
      },

      addComponent(below = true) {
        this.addComponentModal.visible = true
        this.$nextTick(() => {
          this.$refs.addComponentModalFilter.focus()
        })
      },

      addComponentFromModal(component) {
        this.addComponentModal.visible = true
        const view = this.view
        const payload = {
          pagePath: view.pageView.path,
          path: this.path,
          component: component,
          drop: 'after'
        }
        $perAdminApp.stateAction('addComponentToPath', payload).then((data) => {
          this.refreshInlineEditClones()
          this.iframeEditMode()
          this.addComponentModal.visible = false
          // TODO: would be nice to select the newly inserted component and focus
          //       into the first contenteditable if there is indeed a contenteditable
        })
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
        if (selection.anchorNode === this.target) {
          event.preventDefault();
          this.iframe.doc.execCommand('delete')
          this.target.innerHTML = ''
          this.writeInlineToModel()
        }
      },

      onInlineArrowKey(event, isKeyUp = false) {
        const key = event.which
        const newCaretPos = getCaretCharacterOffsetWithin(event.target)
        if (this.caretPos === newCaretPos && (isKeyUp || this.holdingDown)) {
          const inlineEditNodes = this.iframe.app.querySelectorAll(`[${Attribute.INLINE}]`)
          if (inlineEditNodes.length <= 1) return

          for (let i = 1; i < inlineEditNodes.length - 1; i++) {
            if (inlineEditNodes[i] === this.target) {
              if (key === Key.ARROW_LEFT || key === Key.ARROW_UP) {
                inlineEditNodes[i - 1].focus()
              } else {
                inlineEditNodes[i + 1].focus()
              }
              break;
            }
          }
        }
        this.caretPos = newCaretPos
      },

      onIframeLoaded(ev) {
        this.iframe.loaded = true
        this.iframe.win = this.$refs.editview.contentWindow
        this.iframe.doc = this.iframe.win.document
        this.iframe.html = this.iframe.doc.querySelector('html')
        this.iframe.body = this.iframe.doc.querySelector('body')
        this.iframe.head = this.iframe.doc.querySelector('head')
        this.iframe.app = this.iframe.doc.querySelector('#peregrine-app')
        this.iframe.doc.querySelector('#peregrine-app').setAttribute('contenteditable', 'false')
        this.addIframeExtraStyles()
        this.removeLinkTargets()
        this.refreshInlineEditClones()
        this.iframeEditMode()
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
          const isDropTarget = this.dropTarget === 'true'
          const isRoot = this.isTemplateNode
          const relMousePos = this.getRelativeMousePosition(event)

          if (isDropTarget) {
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
            this.dropPosition = 'none'
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
        }
        $perAdminApp.stateAction(addOrMove, payload).then((data) => {
          this.refreshInlineEditClones()
        })
        this.unselect(this)
        event.dataTransfer.clearData('text')
      },

      removeLinkTargets() {
        const anchors = this.iframe.app.querySelectorAll('a')
        anchors.forEach((a) => {
          a.setAttribute('href', 'javascript:void(0);')
        })
      },

      refreshInlineEditClones() {
        const selector = `[${Attribute.INLINE}]:not(.inline-edit-clone)`
        const elements = this.iframe.app.querySelectorAll(selector)
        if (!elements || elements.length <= 0) return

        elements.forEach((el) => {
          if (this.isFromTemplate(el)) return

          const cmpPath = this.getPath(el)
          const clsList = el.classList
          const clone = el.cloneNode(true)
          const dataInline = el.getAttribute(Attribute.INLINE).split('.').slice(1)
          clone.classList.add('inline-edit-clone')
          clone.addEventListener('input', this.onInlineEdit)
          clone.addEventListener('focus', this.onInlineFocus)
          clone.addEventListener('focusout', this.onInlineFocusOut)
          clone.addEventListener('keydown', this.onInlineKeyDown)
          clone.addEventListener('keyup', this.onInlineKeyUp)
          clone.setAttribute('contenteditable', this.previewMode !== 'preview' + '')
          el.parentNode.insertBefore(clone, el)
          el.remove()
          this.$watch(`node.${dataInline.join('.')}`, (val, oldVal) => {
            if (cmpPath === this.path && clone && !clone.classList.contains('inline-editing')) {
              clone.innerHTML = val
            }
          })
        })
      },

      iframeEditMode() {
        this.iframe.doc.addEventListener('click', this.onIframeClick)
        this.iframe.doc.addEventListener('scroll', this.onIframeScroll)
        this.iframe.doc.addEventListener('dragover', this.onIframeDragOver)
        this.iframe.doc.addEventListener('drop', this.onIframeDrop)
        this.iframe.body.setAttribute('contenteditable', 'true')
        this.iframe.html.classList.add('edit-mode')
        const elements = this.iframe.app.querySelectorAll(`[${Attribute.INLINE}]`)
        elements.forEach((el, index) => {
          if (this.isFromTemplate(el)) return
          el.setAttribute('contenteditable', 'true')
        })
      },

      iframePreviewMode() {
        this.iframe.doc.removeEventListener('click', this.onIframeClick)
        this.iframe.doc.removeEventListener('scroll', this.onIframeScroll)
        this.iframe.body.setAttribute('contenteditable', 'false')
        this.iframe.html.classList.remove('edit-mode')
        const elements = this.iframe.app.querySelectorAll(`[${Attribute.INLINE}]`)
        elements.forEach((el, index) => {
          if (this.isFromTemplate(el)) return
          el.setAttribute('contenteditable', false)
        })
      },

      addIframeExtraStyles() {
        if (this.iframe.head.querySelector('#editing-extra-styles')) return
        const css = `
          html.edit-mode body {
            cursor: default !important
          }
          html.edit-mode #peregrine-app [contenteditable="true"]:focus {
            outline: 1px solid #fe9701 !important;
          }

          html.edit-mode #peregrine-app [contenteditable="true"]:hover:not(:focus) {
            outline: 1px solid #ffc171 !important;
          }

          html.edit-mode #peregrine-app .from-template {
            cursor: not-allowed !important;
          }

          html.edit-mode #peregrine-app .from-template * {
            cursor: not-allowed !important;
          }

          html.edit-mode #peregrine-app .inline-edit-clone {
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

        while (el.getAttribute('contenteditable') !== 'true') {
          el = el.parentElement
          if (!el || el === component) {
            return false
          }
        }
        return el.getAttribute('contenteditable') === 'true'
      },

      wrapEditableAroundSelected() {
        if (!this.component) return

        const {top, left, width, height} = this.getBoundingClientRect(this.component)
        const offset = this.getBoundingClientRect(this.$refs.editview)

        this.editable.styles.top = `${top}px`
        this.editable.styles.left = `${left + offset.left}px`
        this.editable.styles.width = `${width}px`
        this.editable.styles.height = `${height}px`
      },

      reWrapEditable() {
        this.editable.timer = setTimeout(() => {
          this.editable.class = 'selected'
          this.wrapEditableAroundSelected()
        }, this.editable.delay)
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
            this.refreshInlineEditClones()
          })
        }
        this.unselect(this)
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
          this.refreshInlineEditClones()
        })
      }
    }
  }
</script>