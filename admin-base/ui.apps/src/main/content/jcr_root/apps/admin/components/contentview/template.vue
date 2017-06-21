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
            v-on:wheel     = "onScrollOverlay"
            v-on:mousemove = "mouseMove"
            v-on:dragover  = "onDragOver"
            v-on:drop      = "onDrop"
            v-bind:style   = "`right: ${scrollbarWidth}px;`">
            <div 
                v-bind:class   = "editableClass"
                ref            = "editable" 
                id             = "editable"
                draggable      = "true"
                v-on:dragstart = "onDragStart">
                <div class="editable-actions">
                    <ul v-if="enableTools">
                        <li class="waves-effect waves-light">
                            <a href="#" title="copy" v-on:click.stop.prevent="onCopy">
                                <i class="svg-icon svg-icon-copy"></i>
                            </a>
                        </li>
                        <li v-if="clipboard" class="waves-effect waves-light">
                            <a title="paste" href="#" v-on:click.stop.prevent="onPaste">
                                <i class="svg-icon svg-icon-paste"></i>
                            </a>
                        </li>
                        <li class="waves-effect waves-light">
                            <a href="#" title="delete" v-on:click.stop.prevent="onDelete">
                                <i class="material-icons">delete</i>
                            </a>
                        </li>
                        <li class="waves-effect waves-light">
                            <a href="#" title="drag" style="pointer-events: none;">
                                <i class="material-icons">drag_handle</i>
                            </a>
                        </li>
                    </ul>
                </div>
            </div>
        </div>
        <iframe
            v-on:load    = "setWheelEventListener"
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
            window.addEventListener('resize', this.onResize)
            document.addEventListener('keydown', this.onKeyDown)
            document.addEventListener('keyup', this.onKeyUp)
            this.setScrollBarWidth(window.navigator.userAgent)
        })
    },

    props: ['model'],

    data(){
        return {
            editableVisible: false,
            editableClass: null,
            selectedComponent: null,
            clipboard: null,
            scrollbarWidth: 0,
            ctrlDown: false
        }
    },

    computed: {
        pagePath: function() {
            return $perAdminApp.getNodeFromView('/pageView/path') + '.html'
        },
        viewMode: function() {
            let viewMode = $perAdminApp.getNodeFromViewOrNull('/state/tools/workspace/view')
            if(viewMode) return viewMode
            return 'desktop'
        },
        viewModeClass: function() {
            return this.viewMode
        },
        enableTools: function() {
            var targetEl = this.selectedComponent
            if(!targetEl) return false
            var node = $perAdminApp.findNodeFromPath($perAdminApp.getView().pageView.page, targetEl.getAttribute('data-per-path'))
            return !node.fromTemplate
        }
    },

    watch: {
        viewModeClass: function(mode) {
            if(this.selectedComponent !== null){
                this.$nextTick(function() {
                    var targetBox = this.getBoundingClientRect(this.selectedComponent)
                    this.setEditableStyle(targetBox, 'selected')
                })
            }
        },

    },

    methods: {
        /* Window/Document methods =================
        ============================================ */
        onResize: function(e){
            if(this.selectedComponent !== null){
                var targetBox = this.getBoundingClientRect(this.selectedComponent)
                this.setEditableStyle(targetBox, 'selected')
            }
        },

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
        setWheelEventListener(ev){
            ev.target.contentWindow.addEventListener('wheel', this.onScrollIframe)
            ev.target.contentWindow.addEventListener('scroll', this.onScrollIframe)
        },

        onScrollIframe(ev){
            if(this.selectedComponent !== null){
                this.$nextTick(function () {
                    var selectedComponentRect = this.getBoundingClientRect(this.selectedComponent)
                    this.updateEditablePos(selectedComponentRect.top)
                })
            }
        },

        /*  Overlay (editviewoverlay) methods ======
        ============================================ */
        setScrollBarWidth(userAgent) {
            let widths = { edge: 12, mac: 15, win: 17, unknown: 17 }
            this.scrollbarWidth = widths[$perAdminApp.getOSBrowser()]
            if(!this.scrollbarWidth){
                this.scrollbarWidth = widths.unknown
            }
        },

        onScrollOverlay(ev){
            ev.preventDefault()
            this.$nextTick(function () {
                var isScrolling = false
                ev.target.style['pointer-events'] = 'none'
                if(isScrolling) {
                    clearTimeout(timer)        
                }
                isScrolling = setTimeout(function() {
                    ev.target.style['pointer-events'] = 'auto'
                }, 66)
            })
        },

        getPosFromMouse: function(e) {
            var elRect = this.getBoundingClientRect(this.$refs.editview)

            var posX = e.clientX - elRect.left
            var posY = e.clientY - elRect.top

            return {x: posX, y: posY }
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
            // console.log(rect)
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
                    $perAdminApp.notifyUser('template component', 'This component is part of the template. Please modify the template in order to change it', () => {})
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
            this.selectedComponent = null
            this.editableClass = null
        },

        mouseMove: function(e) {
            if(!e) return
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
            console.log(this.selectedComponent.getAttribute('data-per-path'))
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
            var targetEl = this.getTargetEl(ev)
            var componentPath = ev.dataTransfer.getData('text')
            if(typeof targetEl === 'undefined' || targetEl === null){
                console.log('no target')
                return false
            }
            if(targetEl.getAttribute('data-per-path') === componentPath) {
                ev.dataTransfer.clearData('text')
                return false 
            }
            console.log('drop componentPath: ', componentPath)
            var view = $perAdminApp.getView()
            var payload = { 
                pagePath : view.pageView.path, 
                path: targetEl.getAttribute('data-per-path'), 
                component: componentPath, 
                drop: this.dropPosition 
            }
            var addOrMove
            componentPath.includes('/components/') ? addOrMove = 'addComponentToPath' : addOrMove = 'moveComponentToPath'
            $perAdminApp.stateAction(addOrMove, payload)
            ev.dataTransfer.clearData('text')
        },

        /* Editable methods ========================
        ============================================ */
        setEditableStyle: function(targetBox, editableClass) {
            var editable = this.$refs.editable
            if(editable) {
                editable.style.top    = targetBox.top + 'px'
                editable.style.left   = targetBox.left + 'px'
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
        }
    }
}
</script>
