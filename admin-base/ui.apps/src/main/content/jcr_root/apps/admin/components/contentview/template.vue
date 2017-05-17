<template>
    <div 
        v-bind:class  ="`peregrine-content-view ${viewModeClass}`" 
        v-on:mouseout = "leftArea">
        <div 
            id             = "editviewoverlay"
            v-on:click     = "click"
            v-on:wheel     = "scrollEditViewOverlay"
            v-on:mousemove = "mouseMove"
            v-on:dragover  = "dragOver"
            v-on:drop      = "drop">
            <div id="editable"
                 draggable     = "true"
                 v-on:dragstart= "dragStart">
                 <div class="editable-actions">
                    <ul>
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
                            <a href="#" title="drag" v-on:click.stop.prevent="allowEditableEvents">
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
        })
    },

    props: ['model'],

    data(){
        return {
            selectedEl: null,
            clipboard: null
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
        }
    },
    methods: {
        dragStart(ev) {
            let element = this.getTargetEl(ev)
            if(element) {
                ev.dataTransfer.setData('componentFrom', element.getAttribute('data-per-path'))
            }
            else {
                ev.preventDefault()
                return false
            }
        },

        setWheelEventListener(ev){
            ev.target.contentWindow.addEventListener('wheel', this.onEditViewScroll)
        },

        onEditViewScroll(ev){
            var scrollTop = ev.target.scrollTop
            console.log('onEditViewScroll: ', scrollTop)
        },

        allowEditableEvents(ev){
            console.log('parent? ', ev.target.parent)
            var editable = this.$el.children['editviewoverlay'].children['editable']
            editable.style['pointer-events'] = 'auto'
        },

        scrollEditViewOverlay(ev){
            console.log('scrollEditViewOverlay')
            var timer = null
            if(this.selectedEl !== null){
                var selectedElRect = this.selectedEl.getBoundingClientRect()
                this.updateEditablePos(selectedElRect.top)
            }
            var editViewOverlay = ev.target
            editViewOverlay.style['pointer-events'] = 'none'
            /* TODO: remove timeout by using custom method to detect when scrolling stops */
            
            if(timer !== null) {
                clearTimeout(timer)        
            }
            timer = setTimeout(function() {
                editViewOverlay.style['pointer-events'] = 'auto'
            }, 10)
        },

        onResize: function(e){
            if(this.selectedEl !== null){
                var targetBox = this.selectedEl.getBoundingClientRect()
                var editable = this.$el.children['editviewoverlay'].children['editable']
                this.setStyle(editable, targetBox, '', '1px solid #ef5350')
            }
        },

        getPosFromMouse: function(e) {
            var elRect = this.$refs.editview.getBoundingClientRect()

            var posX = e.clientX - elRect.left
            var posY = e.clientY - elRect.top

            return {x: posX, y: posY }
        },

        getTargetEl: function(e) {

            var pos = this.getPosFromMouse(e)

            var editview = this.$el.children['editview']
            var editable = this.$el.children['editviewoverlay'].children['editable']

            var targetEl = editview.contentWindow.document.elementFromPoint(pos.x, pos.y)
            if(!targetEl) return

            while(!targetEl.getAttribute('data-per-path')) {
                targetEl = targetEl.parentElement
                if(!targetEl) { break; }
            }
            return targetEl
        },

        click: function(e) {
            if(!e) return
            var targetEl = this.getTargetEl(e)
            if(targetEl) {
                this.selectedEl = targetEl
                var targetBox = targetEl.getBoundingClientRect()
                this.setStyle(editable, targetBox, '', '1px solid #ef5350')
                $perAdminApp.action(this, 'showComponentEdit', targetEl.getAttribute('data-per-path'))
            }
        },

        leftArea: function(e) {
            if($perAdminApp.getNodeFromViewOrNull('/state/editorVisible')) return
            var editable = this.$el.children['editviewoverlay'].children['editable']
            editable.style.display = 'none'
        },

        setStyle: function(editable, targetBox, name, border) {
            editable.style.top = targetBox.top+'px'
            editable.style.left = targetBox.left+'px'
            editable.style.width = targetBox.width+'px'
            editable.style.height = targetBox.height+'px'
            editable.style.display = 'block'
            editable.style['border'] = 'none'
            editable.style['border-top'] = 'none'
            editable.style['border-bottom'] = 'none'
            editable.style['border'+name] = border
        },

        updateEditablePos: function(top, left){
            var editable = this.$el.children['editviewoverlay'].children['editable']
            editable.style.top = top + 'px'
            editable.style.left = left + 'px'
        },

        mouseMove: function(e) {
            if(!e) return
            if($perAdminApp.getNodeFromViewOrNull('/state/editorVisible')) return
            var targetEl = this.getTargetEl(e)
            if(targetEl) {
                this.selectedEl = targetEl
                if(targetEl.getAttribute('data-per-droptarget')) {
                    targetEl = targetEl.parentElement
                }
                var targetBox = targetEl.getBoundingClientRect()
                this.setStyle(editable, targetBox, '', '1px solid #ef5350')
            }
        },

        dragOver: function(e) {
            e.preventDefault()
            var targetEl = this.getTargetEl(e)

            if(targetEl) {
                var pos = this.getPosFromMouse(e)
                var targetBox = targetEl.getBoundingClientRect()
                var isDropTarget = targetEl.getAttribute('data-per-droptarget') === 'true'

                if(isDropTarget) {
                    this.dropPosition = 'into'
                    this.setStyle(editable, targetBox, '', '1px solid #ef5350')
                } else {
                    var y = pos.y - targetBox.top
                    if(y < targetBox.height/2) {
                        this.dropPosition = 'before'
                        this.setStyle(editable, targetBox, '-top', '1px solid #ef5350')
                    } else {
                        this.dropPosition = 'after'
                        this.setStyle(editable, targetBox, '-bottom', '1px solid #ef5350')
                    }
                }
            } else {
                this.dropPosition = 'none'
                this.leftArea()
            }

        },

        drop: function(e) {
            var editable = this.$el.children['editviewoverlay'].children['editable']
            editable.style['pointer-events'] = 'none'
            editable.style.display = 'none'

            var targetEl = this.getTargetEl(e)
            var componentPath = e.dataTransfer.getData('component')
            var componentFrom = e.dataTransfer.getData('componentFrom')
            var view = $perAdminApp.getView()
            if(componentPath) {
                $perAdminApp.stateAction('addComponentToPath', { pagePath : view.pageView.path, path: targetEl.getAttribute('data-per-path'), component: componentPath, drop: this.dropPosition})
            } else if(componentFrom) {
                $perAdminApp.stateAction('moveComponentToPath', { pagePath : view.pageView.path, path: targetEl.getAttribute('data-per-path'), component: componentFrom, drop: this.dropPosition})
            }

        },

        onDelete: function(e) {
            var targetEl = this.getTargetEl(e)
            var view = $perAdminApp.getView()
            var pagePath = view.pageView.path
            console.log('onDelete. pagePath:', pagePath)
            console.log('onDelete. targetEl:', targetEl)
            console.log('onDelete. view:', view)
            $perAdminApp.stateAction('deletePageNode', { pagePath: view.pageView.path, path: targetEl.getAttribute('data-per-path') } )
        },

        onCopy: function(e) {
            var targetEl = this.getTargetEl(e)
            var targetCopy = targetEl.cloneNode(true)
            var path = targetCopy.getAttribute('data-per-path')
            targetCopy.setAttribute('data-per-path', path + '-1')
            this.clipboard = targetCopy
            console.log('onCopy (copied node):', targetCopy)
        },

        onPaste: function(e) {
            var componentFromClipboard = this.clipboard
            console.log('componentFromClipboard:', componentFromClipboard)
            var view = $perAdminApp.getView()
            /* do something with clipboard contents, then clear clipboard */
            var payload = { 
                pagePath: view.pageView.path, 
                path: componentFromClipboard.getAttribute('data-per-path'), 
                component: 'componentPath',
                drop: 'this.dropPosition'
            }
            console.log('payload: ', payload)
            /* uncomment when payload has been built properly */
            // $perAdminApp.stateAction('addComponentToPath', payload)
            this.clipboard = null
        }
    }
}
</script>
