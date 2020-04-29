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
  <div :class="`peregrine-content-view ${viewModeClass}`" @mouseout="leftOverlayArea">
    <div id="editable"
         ref="editable"
         :class="editable.class"
         :style="editable.styles"
         :draggable="enableEditableFeatures"
         @dragstart="onDragStart"
         @touchstart="onEditableTouchStart"
         @touchend="onEditableTouchEnd">
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
          @load="onIframeLoaded"
          @dragover="onDragOver"
          @drop.prevent="onDrop"/>
    </template>
  </div>
</template>

<script>
  export default {
    props: ['model'],
    data() {
      return {
        editable: {
          visible: false,
          class: null,
          timer: null,
          styles: {
            top: 0,
            left: 0,
            width: 0,
            height: 0
          }
        },
        selected: {
          el: null,
          path: null,
          draggable: true
        },
        clipboard: null,
        ctrlDown: false,
        scrollTop: 0,
        isTouch: false,
        isIOS: false,
        iframe: {
          loaded: false,
          doc: null,
          html: null,
          body: null,
          scrollTop: 0,
          clicked: {
            targetEl: null,
            el: null,
            path: null
          }
        }
      }
    },
    computed: {
      isSelected() {
        return this.selected.el && this.selected.path && this.selected.path !== '/jcr:content'
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
      pageView() {
        return $perAdminApp.getView().pageView
      },
      enableEditableFeatures() {
        const path = this.selected.path
        if (path === undefined || path === null) {
          return false
        }
        const node = $perAdminApp.findNodeFromPath(this.pageView.page, path)
        if (!node) {
          return false
        }
        return !node.fromTemplate
      },
      selectedModel() {
        const view = $perAdminApp.getView()
        const path = view.state.editor.path
        return $perAdminApp.findNodeFromPath(view.pageView.page, path)
      }
    },
    watch: {
      viewMode(newViewMode) {
        this.setIframeScrollState(newViewMode)
      },
      previewMode(val) {
        if (val === 'preview') {
          this.iframe.doc.removeEventListener('click', this.onIframeClick)
        } else {
          this.iframe.doc.addEventListener('click', this.onIframeClick)
        }
      },
      'iframe.clicked.el'(val) {
        if (!val) return
        this.updateSelectedComponent()
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
        document.addEventListener('keydown', this.onKeyDown)
        document.addEventListener('keyup', this.onKeyUp)
      })
    },
    methods: {
      setSelectedEl(el) {
        if (el) {
          this.selected.el = el
          this.selected.path = el.getAttribute('data-per-path') || null
          this.editable.class = 'selected'
          this.wrapEditableAround(el)
          $perAdminApp.action(this, 'showComponentEdit', this.selected.path).then(() => {
            const view = $perAdminApp.getView()
            const path = view.state.editor.path
            const model = $perAdminApp.findNodeFromPath(view.pageView.page, path)
            const elements = []
            const queried = this.selected.el.querySelectorAll('[data-per-model-key]')
            elements.push.apply(elements, queried)
            elements.push.apply(elements, [this.selected.el])
            elements.forEach((element) => {
              const modelKey = element.getAttribute('data-per-model-key')
              const style = element.getAttribute('style')
              element.setAttribute('contenteditable', true)
              element.setAttribute('style', `cursor: text !important;${style}`)
              element.addEventListener('input', (event) => {
                model[modelKey] = event.target.innerHTML
              })
            })
          })
        } else {
          this.selected.el = null
          this.selected.path = null
        }
      },

      setIframeClicked(targetEl) {
        if (targetEl) {
          const componentEl = this.findComponentEl(targetEl)
          this.iframe.clicked.targetEl = targetEl
          this.iframe.clicked.el = componentEl
          this.iframe.clicked.path = componentEl ? componentEl.getAttribute('data-per-path') : null
        } else {
          this.iframe.clicked.targetEl = null
          this.iframe.clicked.el = null
          this.iframe.clicked.path = null
        }
      },

      findComponentEl(targetEl) {
        let componentEl = targetEl
        while (!componentEl.getAttribute('data-per-path')) {
          componentEl = componentEl.parentElement
          if (!componentEl) {
            break
          }
        }
        return componentEl
      },

      onIframeLoaded(ev) {
        this.iframe.loaded = true
        this.iframe.doc = this.$refs.editview.contentWindow.document
        this.iframe.html = this.iframe.doc.querySelector('html')
        this.iframe.body = this.iframe.doc.querySelector('body')
        this.iframe.doc.addEventListener('click', this.onIframeClick)
        this.iframe.doc.addEventListener('scroll', this.onIframeScroll)
        this.iframe.doc.addEventListener('dragover', this.onDragOver)
        this.iframe.doc.addEventListener('drop', this.onDrop)
        this.iframe.body.setAttribute('style', 'cursor: default !important')
      },

      onIframeClick(ev) {
        this.setIframeClicked(ev.target)
      },

      onIframeScroll() {
        this.iframe.scrollTop = this.iframe.html.scrollTop
      },

      updateSelectedComponent() {
        const node = $perAdminApp.findNodeFromPath(this.pageView.page, this.iframe.clicked.path)
        if (node.fromTemplate) {
          $perAdminApp.notifyUser(this.$i18n('templateComponent'),
              this.$i18n('fromTemplateNotifyMsg'), {
                complete: this.removeEditOverlay
              })
        } else {
          this.setSelectedEl(this.iframe.clicked.el)
        }
      },

      wrapEditableAround(el) {
        const {top, left, width, height} = this.getBoundingClientRect(el)
        this.editable.styles.top = `${top}px`
        this.editable.styles.left = `${left}px`
        this.editable.styles.width = `${width}px`
        this.editable.styles.height = `${height}px`
      },

      /* Window/Document methods =================
      ============================================ */
      onKeyDown(ev) {
        const nodeName = document.activeElement.nodeName
        const className = '' + document.activeElement.className
        /* check no field is currently in focus */
        if (nodeName === 'INPUT' || nodeName === 'TEXTAREA' || className.startsWith('trumbowyg')) {
          return false
        } else {
          const ctrlKey = 17
          const cmdKey = 91
          if (ev.keyCode === ctrlKey || ev.keyCode === cmdKey) {
            this.ctrlDown = true
          }
          if (this.selected.el !== null) {
            const cKey = 67
            const vKey = 86
            if (this.ctrlDown && (ev.keyCode === cKey)) {
              this.onCopy()
            }
            if (this.ctrlDown && (ev.keyCode === vKey)) {
              this.onPaste()
            }
          }
        }
      },

      onKeyUp(ev) {
        const nodeName = document.activeElement.nodeName
        const className = document.activeElement.className
        /* check no field is currently in focus */
        if (nodeName === 'INPUT' || nodeName === 'TEXTAREA' || className === 'ql-editor') {
          return false
        } else {
          const ctrlKey = 17
          const cmdKey = 91
          if (ev.keyCode === ctrlKey || ev.keyCode === cmdKey) {
            this.ctrlDown = false
          }
        }
      },

      setIframeScrollState(viewMode) {
        const iframeDoc = this.$refs.editview.contentWindow.document
        if (viewMode.endsWith('preview')) {
          iframeDoc.body.style.overflowX = 'hidden'
          iframeDoc.body.style.overflowY = 'auto'
        } else {
          iframeDoc.body.style.overflowX = 'hidden'
          iframeDoc.body.style.overflowY = 'auto'
        }
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
          left: rect.left - marginLeft,
          right: rect.right + marginRight,
          top: rect.top - marginTop,
          bottom: rect.bottom + marginBottom,
        }
        newRect.width = newRect.right - newRect.left
        newRect.height = newRect.bottom - newRect.top
        return newRect;
      },

      findIn(el, pos) {
        if (!el) return

        const rect = this.getBoundingClientRect(el)
        let ret = null
        if (pos.x > rect.left && pos.x < rect.right && pos.y > rect.top && pos.y < rect.bottom) {
          ret = el
          for (let i = 0; i < el.children.length; i++) {
            const child = this.findIn(el.children[i], pos)
            if (child != null) {
              ret = child
              break
            }
          }
        }
        return ret
      },

      leftOverlayArea(e) {
        if ($perAdminApp.getNodeFromViewOrNull('/state/editorVisible')) return

        // check if we only left the area into the overlay for the actions
        this.removeEditOverlay()
      },

      removeEditOverlay() {
        this.setSelectedEl(null)
        this.editable.class = null
        if (this.isTouch) {
          this.selected.draggable = false
        }
      },

      getRelativeMousePosition(event) {
        if (!event) return {x: -1, y: -1}
        const componentEl = this.findComponentEl(event.target)
        const offset = this.getBoundingClientRect(componentEl)
        return {
          width: offset.width,
          x: event.pageX - offset.left,
          xPercentage: (event.pageX - offset.left) / offset.width * 100,
          height: offset.height,
          y: event.pageY - offset.top - this.iframe.scrollTop,
          yPercentage: (event.pageY - offset.top - this.iframe.scrollTop) / offset.height * 100
        }
      },

      /* Drag and Drop ===========================
      ============================================ */
      onDragStart(ev) {
        if (this.selected.el === null) return

        this.editable.class = 'dragging'
        ev.dataTransfer.setData('text', this.selected.path)
      },

      onDragOver(event) {
        event.preventDefault()
        const targetEl = this.findComponentEl(event.target)
        if (targetEl) {
          this.wrapEditableAround(targetEl)
          const isDropTarget = targetEl.getAttribute('data-per-droptarget') === 'true'
          const isRoot = $perAdminApp.findNodeFromPath($perAdminApp.getView().pageView.page,
              targetEl.getAttribute('data-per-path')).fromTemplate === true
          if (isDropTarget) {
            const dropLocation = targetEl.getAttribute('data-per-location')
            if (dropLocation === 'after' && !isRoot) {
              this.dropPosition = 'after'
              this.editable.class = 'drop-bottom'
            } else if (dropLocation === 'before' && !isRoot) {
              this.dropPosition = 'before'
              this.editable.class = 'drop-top'
            } else if (dropLocation) {
              this.dropPosition = 'into-' + dropLocation
              this.editable.class = 'selected'
            } else {
              this.dropPosition = 'none'
              this.leftOverlayArea()
            }
          } else if (!isRoot) {
            const relativeMousePosition = this.getRelativeMousePosition(event)
            if (relativeMousePosition.yPercentage <= 43.5) {
              this.dropPosition = 'before'
              this.editable.class = 'drop-top'
            } else {
              this.dropPosition = 'after'
              this.editable.class = 'drop-bottom'
            }
          } else {
            this.dropPosition = 'none'
            this.leftOverlayArea()
          }
        } else {
          this.dropPosition = 'none'
          this.leftOverlayArea()
        }
      },

      onDrop(ev) {
        console.log('onDrop')
        this.editable.class = null
        if (this.isTouch) {
          this.selected.draggable = false
        }
        const targetEl = this.getTargetEl(ev)
        if (typeof targetEl === 'undefined' || targetEl === null) {
          return false
        }
        const targetPath = targetEl.getAttribute('data-per-path');
        const componentPath = ev.dataTransfer.getData('text')
        if (targetPath === componentPath) {
          ev.dataTransfer.clearData('text')
          return false
        }
        const view = $perAdminApp.getView()
        const payload = {
          pagePath: view.pageView.path,
          path: targetEl.getAttribute('data-per-path'),
          component: componentPath,
          drop: this.dropPosition
        }
        let addOrMove
        if (componentPath.includes('/components/')) {
          addOrMove = 'addComponentToPath';
        } else {
          addOrMove = 'moveComponentToPath';
          const targetNode = $perAdminApp.findNodeFromPath($perAdminApp.getView().pageView.page,
              targetPath)
          if ((!targetNode) || (targetNode.fromTemplate)) {
            $perAdminApp.notifyUser('template component',
                'You cannot drag a component into a template section', {
                  complete: this.removeEditOverlay
                })
            return false;
          }
        }
        $perAdminApp.stateAction(addOrMove, payload)
        ev.dataTransfer.clearData('text')
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
        if (this.selected.el === null) return

        this.selected.draggable = true
        this.editable.class = 'draggable'
      },

      updateEditablePos(top) {
        this.$refs.editable.style.top = top + 'px'
      },

      onDelete(e) {
        const targetEl = this.selected.el
        const view = $perAdminApp.getView()
        const pagePath = view.pageView.path
        const payload = {
          pagePath: view.pageView.path,
          path: targetEl.getAttribute('data-per-path')
        }
        if (payload.path !== '/jcr:content') {
          $perAdminApp.stateAction('deletePageNode', payload)
        }
        this.editable.class = null
        this.setSelectedEl(null)
      },

      onCopy(e) {
        const targetEl = this.selected.el
        this.clipboard = $perAdminApp.findNodeFromPath(
            $perAdminApp.getView().pageView.page,
            targetEl.getAttribute('data-per-path')
        )
      },

      onPaste(e) {
        const targetEl = this.selected.el
        const nodeFromClipboard = this.clipboard
        const view = $perAdminApp.getView()
        const isDropTarget = targetEl.getAttribute('data-per-droptarget') === 'true'
        let dropPosition
        isDropTarget ? dropPosition = 'into' : dropPosition = 'after'
        const payload = {
          pagePath: view.pageView.path,
          data: nodeFromClipboard,
          path: targetEl.getAttribute('data-per-path'),
          drop: dropPosition
        }
        $perAdminApp.stateAction('addComponentToPath', payload)
      },

      refreshEditor(me, target) {
        me.$refs['editview'].contentWindow.location.reload();
      }
    }
  }
</script>