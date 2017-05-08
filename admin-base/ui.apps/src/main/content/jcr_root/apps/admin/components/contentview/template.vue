<template>
    <div class="peregrine-content-view">
        <div 
            id            = "editviewoverlay" 
            v-on:click    = "click"
            v-on:mousewheel = "scrollEditView"
            v-on:mousemove= "mouseMove"
            v-on:mouseout = "leftArea"
            v-on:dragover = "dragOver"
            v-on:drop     = "drop">
            <div id="editable"></div>
        </div>
        <iframe 
            id           = "editview" 
            v-bind:src   = "pagePath" 
            frameborder  = "0"></iframe>
    </div>
</template>

<script>
export default {
    props: ['model'],

    computed: {
        pagePath: function() {
            return $perAdminApp.getNodeFromView('/pageView/path') + '.html'
        }
    },
    methods: {
        scrollEditView(ev){
            var timer = null
            var editViewOverlay = ev.target
            editViewOverlay.style['pointer-events'] = 'none'
            if(timer !== null) {
                clearTimeout(timer)        
            }
            timer = setTimeout(function() {
                editViewOverlay.style['pointer-events'] = 'auto'
            }, 150)
        },

        getPosFromMouse: function(e) {
            var elRect = this.$el.getBoundingClientRect()

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
                $perAdminApp.action(this, 'showComponentEdit', targetEl.getAttribute('data-per-path'))
            }
        },

        leftArea: function(e) {
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

        mouseMove: function(e) {
            if(!e) return
            if($perAdminApp.getNodeFromView('/state/editorVisible')) return

            var targetEl = this.getTargetEl(e)
            if(targetEl) {
                var targetBox = targetEl.getBoundingClientRect()
                this.setStyle(editable, targetBox, '', '1px solid red')
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
                    this.setStyle(editable, targetBox, '', '1px solid red')
                } else {
                    var y = pos.y - targetBox.top
                    if(y < targetBox.height/2) {
                        this.dropPosition = 'before'
                        this.setStyle(editable, targetBox, '-top', '1px solid red')
                    } else {
                        this.dropPosition = 'after'
                        this.setStyle(editable, targetBox, '-bottom', '1px solid red')
                    }
                }
            } else {
                this.dropPosition = 'none'
                this.leftArea()
            }

        },

        drop: function(e) {
            var editable = this.$el.children['editviewoverlay'].children['editable']
            editable.style.display = 'none'

            var targetEl = this.getTargetEl(e)
            var componentPath = e.dataTransfer.getData('component')

            var view = $perAdminApp.getView()
            $perAdminApp.stateAction('addComponentToPath', { pagePath : view.pageView.path, path: targetEl.getAttribute('data-per-path'), component: componentPath, drop: this.dropPosition})
        }
    }
}
</script>
