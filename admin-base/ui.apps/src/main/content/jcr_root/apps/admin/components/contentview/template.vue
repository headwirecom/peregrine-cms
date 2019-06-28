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
    <div
        v-bind:class  ="`peregrine-content-view ${viewModeClass}`"
        v-on:mouseout = "leftOverlayArea">
        <div
            id             = "editviewoverlay"
            v-on:click     = "onClickOverlay"
            v-on:scroll    = "onScrollOverlay"
            v-on:mousemove = "mouseMove"
            v-on:dragover  = "onDragOver"
            v-on:drop.prevent = "onDrop">
            <div class="editview-container" ref="editviewContainer">
                <div
                    v-bind:class   = "editableClass"
                    ref            = "editable"
                    id             = "editable"
                    :draggable     = "enableEditableFeatures"
                    v-on:dragstart = "onDragStart"
                    v-on:touchstart = "onEditableTouchStart"
                    v-on:touchend  = "onEditableTouchEnd">
                    <div v-if="enableEditableFeatures" class="editable-actions">
                        <ul>
                            <li class="waves-effect waves-light">
                                <a href="#" title="copy" v-on:click.stop.prevent="onCopy">
                                    <i class="material-icons">content_copy</i>
                                </a>
                            </li>
                            <li v-if="clipboard" class="waves-effect waves-light">
                                <a title="paste" href="#" v-on:click.stop.prevent="onPaste">
                                    <i class="material-icons">content_paste</i>
                                </a>
                            </li>
                            <li class="waves-effect waves-light">
                                <a href="#" title="delete" v-on:click.stop.prevent="onDelete">
                                    <i class="material-icons">delete</i>
                                </a>
                            </li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
        <iframe
            v-on:load    = "onIframeLoaded"
            ref          = "editview"
            id           = "editview"
            v-bind:src   = "pagePath"
            frameborder  = "0"></iframe>
    </div>
</template>

<script>
export default {
    mounted() {
        this.$nextTick(function() {
            /* is this a touch device */
            this.isTouch = 'ontouchstart' in window || navigator.maxTouchPoints
            this.isIOS = /iPad|iPhone|iPod/.test(navigator.userAgent) && !window.MSStream
            if(this.isTouch){
                /* selected components are not immediatly draggable on touch devices */
                this.selectedComponentDragable = false
            }
            document.addEventListener('keydown', this.onKeyDown)
            document.addEventListener('keyup', this.onKeyUp)

            /* check if page has loaded */
            var unwatch = $perAdminApp.getApp().$watch('pageView', pageView => {
                if(pageView.status === 'loaded'){
                    this.updateOverlay()
                    unwatch() // we dont need to watch the pageView prop anymore
                }
            }, {deep: true })
        })
    },

    props: ['model'],

    data(){
        return {
            editableVisible: false,
            editableClass: null,
            selectedComponent: null,
            selectedComponentDragable: true,
            clipboard: null,
            ctrlDown: false,
            scrollTop: 0,
            isTouch: false,
            isIOS: false,
            editableTimer: null
        }
    },
    watch: {
        viewMode: function (newViewMode) {
            this.setIframeScrollState(newViewMode)
        }
    },

    computed: {
        pagePath: function() {
            return $perAdminApp.getNodeFromView('/pageView/path') + '.html'
        },
        viewMode: function() {
            const viewMode = $perAdminApp.getNodeFromViewOrNull('/state/tools/workspace/view')
            const previewMode = $perAdminApp.getNodeFromViewOrNull('/state/tools/workspace/preview')
            let ret = ''
            if(viewMode) {
                ret = viewMode + (previewMode ? ' ' + previewMode : '' )
            } else {
                ret = 'desktop' + (previewMode ? ' ' + previewMode : '' )
            }
            return ret
        },
        viewModeClass: function() {
            return this.viewMode
        },
        enableEditableFeatures: function() {
            var targetEl = this.selectedComponent
            if(targetEl == null || targetEl === undefined) return false
            const path = targetEl.getAttribute('data-per-path')
            if(path === undefined || path === null) return false
            var node = $perAdminApp.findNodeFromPath($perAdminApp.getView().pageView.page, path)
            if(!node) return false
            return !node.fromTemplate
        }
    },

    methods: {
        /* Window/Document methods =================
        ============================================ */
        onKeyDown(ev){
            var nodeName = document.activeElement.nodeName
            var className = document.activeElement.className
            /* check no field is currently in focus */
            if(nodeName === 'INPUT' || nodeName === 'TEXTAREA' || className === 'ql-editor'){
                return false
            } else {
                var ctrlKey = 17
                var cmdKey = 91
                if (ev.keyCode == ctrlKey || ev.keyCode == cmdKey){
                    this.ctrlDown = true
                }
                if(this.selectedComponent !== null){
                    var cKey = 67
                    var vKey = 86
                    if (this.ctrlDown && (ev.keyCode == cKey)){
                        this.onCopy()
                    }
                    if (this.ctrlDown && (ev.keyCode == vKey)){
                        this.onPaste()
                    }
                }
            }
        },

        onKeyUp(ev){
            var nodeName = document.activeElement.nodeName
            var className = document.activeElement.className
            /* check no field is currently in focus */
            if(nodeName === 'INPUT' || nodeName === 'TEXTAREA' || className === 'ql-editor'){
                return false
            } else {
                var ctrlKey = 17
                var cmdKey = 91
                if (ev.keyCode == ctrlKey || ev.keyCode == cmdKey){
                    this.ctrlDown = false
                }
            }
        },


        /* Iframe (editview) methods ===============
        ============================================ */
        onIframeLoaded(ev){
            const iframeDoc = ev.target.contentWindow.document
            this.setIframeScrollState(this.viewMode)
            iframeDoc.body.style.position = 'relative'
            this.createHeightChangeListener(iframeDoc)
        },

        setIframeScrollState(viewMode) {
             var iframeDoc = this.$refs.editview.contentWindow.document
            if(viewMode.endsWith('preview')){
                iframeDoc.body.style.overflowX = 'hidden'
                iframeDoc.body.style.overflowY = 'auto'
            } else {
                iframeDoc.body.style.overflowX = 'hidden'
                iframeDoc.body.style.overflowY = 'auto'
            }
        },

        updateOverlay(){
            this.$nextTick(() => {
                /* ensure edit container height === iframe body height */
                this.setEditContainerHeight()
                /* update editable position if selected */
                if(this.selectedComponent !== null){
                    var targetBox = this.getBoundingClientRect(this.selectedComponent)
                    this.setEditableStyle(targetBox, 'selected')
                }
            })
        },

        createHeightChangeListener(iframeDoc){
            var heightChangeListener = iframeDoc.createElement('iframe')
            heightChangeListener.id = 'height_change_listener'
            heightChangeListener.setAttribute('tabindex', '-1')
            heightChangeListener.style.position = 'absolute'
            heightChangeListener.style.top = '0'
            heightChangeListener.style.bottom = '0'
            heightChangeListener.style.left = '0'
            heightChangeListener.style.height = '100%'
            heightChangeListener.style.width = '100%'
            heightChangeListener.style.border = '0'
            heightChangeListener.style['z-index'] = '-1'
            heightChangeListener.style['background-color'] = 'transparent'
            iframeDoc.body.appendChild(heightChangeListener)
            heightChangeListener.contentWindow.addEventListener("resize", this.updateOverlay)
        },

        /*  Overlay (editviewoverlay) methods ======
        ============================================ */
        onScrollOverlay(ev){
            this.scrollTop = ev.target.scrollTop
            var editview = this.$refs.editview
            if(this.isIOS){
                /* ios device, use scroll alternative */
                this.$nextTick(function() {
                    editview.contentWindow.document.body.style.transform = `translateY(-${this.scrollTop}px)`
                })
            } else {
                /* is not IOS device, scroll normally */
                this.$nextTick(function() {
                    editview.contentWindow.scrollTo(0, this.scrollTop)
                })
            }
        },

        setEditContainerHeight(){
            // make sure the iframe exists before we actually try to read it
            if(this.$refs.editview) {
                var iframeHeight = this.$refs.editview.contentWindow.document.body.offsetHeight
                this.$refs.editviewContainer.style.height = iframeHeight + 'px'
            }
        },

        getPosFromMouse: function(e) {
            var elRect = this.getBoundingClientRect(this.$refs.editview)
            if(e) {
                var posX = e.clientX - elRect.left
                var posY = e.clientY - elRect.top
                return {x: posX, y: posY }
            }
            else {
                // fix for case where mouse event is not defined
                return {x: -elRect.left, y: -elRect.top }
            }
        },

        getElementStyle: function (e, styleName) {
            var styleValue = "";
            if(document.defaultView && document.defaultView.getComputedStyle) {
                styleValue = document.defaultView.getComputedStyle(e, "").getPropertyValue(styleName);
            }
            else if(e.currentStyle) {
                styleName = styleName.replace(/\-(\w)/g, function (strMatch, p1) {
                    return p1.toUpperCase();
                });
                styleValue = e.currentStyle[styleName];
            }
            return styleValue;
        },

        getBoundingClientRect: function(e) {
            let rect = e.getBoundingClientRect()
            let marginTop = parseFloat(this.getElementStyle(e, 'margin-top'))
            let marginLeft = parseFloat(this.getElementStyle(e, 'margin-left'))
            let marginRight = parseFloat(this.getElementStyle(e, 'margin-right'))
            let marginBottom = parseFloat(this.getElementStyle(e, 'margin-bottom'))
            let newRect = {
                left: rect.left - marginLeft,
                right: rect.right + marginRight,
                top: rect.top - marginTop,
                bottom: rect.bottom + marginBottom,
            }
            newRect.width = newRect.right - newRect.left
            newRect.height = newRect.bottom - newRect.top
            return newRect;
        },

        findIn: function(el, pos) {
            if(!el) return null
            var rect = this.getBoundingClientRect(el)
            var ret = null
            if(pos.x > rect.left && pos.x < rect.right && pos.y > rect.top && pos.y < rect.bottom) {
                ret = el
                for(var i = 0; i < el.children.length; i++) {
                    var child = this.findIn(el.children[i], pos)
                    if(child != null) {
                        ret = child
                        break
                    }
                }
            }
            return ret
        },

        getTargetEl: function(e) {
            var pos = this.getPosFromMouse(e)
            var editview = this.$refs.editview
//            if($perAdminApp.getOSBrowser() === 'win'){
//                var targetEl = this.findIn(editview.contentWindow.document.body, pos)
//            } else {
//                var targetEl = editview.contentWindow.document.elementFromPoint(pos.x, pos.y)
//            }
            var targetEl = this.findIn(editview.contentWindow.document.body, pos)
            if(!targetEl) return

            while(!targetEl.getAttribute('data-per-path')) {
                targetEl = targetEl.parentElement
                if(!targetEl) { break }
            }
            return targetEl
        },

        onClickOverlay: function(e) {
            if(!e) return
            var targetEl = this.getTargetEl(e)
            if(targetEl) {
                var path = targetEl.getAttribute('data-per-path')
                var node = $perAdminApp.findNodeFromPath($perAdminApp.getView().pageView.page, path)
                if(node.fromTemplate) {
                    $perAdminApp.notifyUser('template component', 'This component is part of the template. Please modify the template in order to change it', {
                        complete: this.removeEditOverlay
                    })
                } else {
                    this.selectedComponent = targetEl
                    var targetBox = this.getBoundingClientRect(targetEl)
                    this.setEditableStyle(targetBox, 'selected')
                    $perAdminApp.action(this, 'showComponentEdit', path)
                }
            }
        },

        leftOverlayArea: function(e) {
            if($perAdminApp.getNodeFromViewOrNull('/state/editorVisible')) return

            // check if we only left the area into the overlay for the actions
            var targetEl = this.getTargetEl(e)
            if(targetEl) return
            this.removeEditOverlay()
        },

        removeEditOverlay() {
            this.selectedComponent = null
            this.editableClass = null
            if (this.isTouch) this.selectedComponentDragable = false
        },

        mouseMove: function(e) {
            if(!e || this.isTouch) return
            if($perAdminApp.getNodeFromViewOrNull('/state/editorVisible')) return
            var targetEl = this.getTargetEl(e)
            if(targetEl) {
                if(targetEl.getAttribute('data-per-droptarget')) {
                    targetEl = targetEl.parentElement
                }
                this.selectedComponent = targetEl
                var targetBox = this.getBoundingClientRect(targetEl)
                this.setEditableStyle(targetBox, 'selected')
            }
        },

        /* Drag and Drop ===========================
        ============================================ */
        onDragStart(ev) {
            if(this.selectedComponent === null)return
            this.editableClass = 'dragging'
            ev.dataTransfer.setData('text', this.selectedComponent.getAttribute('data-per-path'))
        },

        onDragOver(ev) {
            ev.preventDefault()
            var targetEl = this.getTargetEl(ev)

            if(targetEl) {
                var pos = this.getPosFromMouse(ev)
                var targetBox = this.getBoundingClientRect(targetEl)
                var isDropTarget = targetEl.getAttribute('data-per-droptarget') === 'true'

                if(isDropTarget) {
                    var dropLocation = targetEl.getAttribute('data-per-location')
                    this.dropPosition = 'into'
                    if(dropLocation) {
                        this.dropPosition += '-' + dropLocation
                    }
                    this.setEditableStyle(targetBox, 'selected')
                } else {
                    var y = pos.y - targetBox.top
                    if(y < targetBox.height/2) {
                        this.dropPosition = 'before'
                        this.setEditableStyle(targetBox, 'drop-top')
                    } else {
                        this.dropPosition = 'after'
                        this.setEditableStyle(targetBox, 'drop-bottom')
                    }
                }
            } else {
                this.dropPosition = 'none'
                this.leftOverlayArea()
            }

        },

        onDrop(ev) {
            this.editableClass = null
            if (this.isTouch) this.selectedComponentDragable = false
            var targetEl = this.getTargetEl(ev)
            if(typeof targetEl === 'undefined' || targetEl === null){
                return false
            }
            var targetPath = targetEl.getAttribute('data-per-path');
            var componentPath = ev.dataTransfer.getData('text')

            if(targetPath === componentPath) {
                ev.dataTransfer.clearData('text')
                return false
            }

            var view = $perAdminApp.getView()
            var payload = {
                pagePath : view.pageView.path,
                path: targetEl.getAttribute('data-per-path'),
                component: componentPath,
                drop: this.dropPosition
            }
            var addOrMove
            if(componentPath.includes('/components/')) {
                addOrMove = 'addComponentToPath';
            } else {
                addOrMove = 'moveComponentToPath';
                var targetNode = $perAdminApp.findNodeFromPath($perAdminApp.getView().pageView.page, targetPath)
                if((!targetNode) || (targetNode.fromTemplate)) {
                    $perAdminApp.notifyUser('template component', 'You cannot drag a component into a template section', {
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
        onEditableTouchStart: function(ev){
            this.editableTimer = setTimeout(this.onLongTouchOverlay, 800)
        },
        onEditableTouchEnd: function(ev){
            clearTimeout(this.editableTimer)
        },
        onLongTouchOverlay: function(){
            if(this.selectedComponent === null) return
            this.selectedComponentDragable = true
            this.editableClass = 'draggable'
        },
        setEditableStyle: function(targetBox, editableClass) {
            var editable = this.$refs.editable
            var editview = this.$refs.editview
            var scrollY = editview ? editview.contentWindow.scrollY : 0
            var scrollX = editview ? editview.contentWindow.scrollX : 0
            if(editable) {
                editable.style.top    = (targetBox.top + scrollY + (this.isIOS ? this.scrollTop : 0)) + 'px'
                editable.style.left   = (targetBox.left + scrollX) + 'px'
                editable.style.width  = targetBox.width + 'px'
                editable.style.height = targetBox.height + 'px'
            }
            this.editableClass = editableClass
        },

        updateEditablePos: function(top){
            this.$refs.editable.style.top = top + 'px'
        },

        onDelete: function(e) {
            var targetEl = this.selectedComponent
            var view = $perAdminApp.getView()
            var pagePath = view.pageView.path
            var payload = {
                pagePath: view.pageView.path,
                path: targetEl.getAttribute('data-per-path')
            }
            $perAdminApp.stateAction('deletePageNode',  payload)
            this.editableClass = null
            this.selectedComponent = null
        },

        onCopy: function(e) {
            var targetEl = this.selectedComponent
            var node = $perAdminApp.findNodeFromPath($perAdminApp.getView().pageView.page, targetEl.getAttribute('data-per-path'))
            this.clipboard = node
        },

        onPaste: function(e) {
            var targetEl = this.selectedComponent
            var nodeFromClipboard = this.clipboard
            var view = $perAdminApp.getView()
            var isDropTarget = targetEl.getAttribute('data-per-droptarget') === 'true'
            var dropPosition
            isDropTarget ? dropPosition = 'into' : dropPosition = 'after'
            var payload = {
                pagePath: view.pageView.path,
                data: nodeFromClipboard,
                path: targetEl.getAttribute('data-per-path'),
                drop: dropPosition
            }
            $perAdminApp.stateAction('addComponentToPath', payload)
        },
        refreshEditor(me, target) {
            console.log('refresh editor')
            me.$refs['editview'].contentWindow.location.reload();
        }
    }
}
</script>

