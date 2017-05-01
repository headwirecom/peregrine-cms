<template>
    <div class="peregrine-content-view">
        <div 
            id            = "editviewoverlay" 
            v-on:click    = "click"
            v-on:mousemove= "mouseMove"
            v-on:mouseout = "leftArea"
            v-on:dragover = "dragOver"
            v-on:drop     = "drop">
            <div id="editable"></div>
        </div>
        <iframe 
            id           = "editview" 
            v-bind:src   = "pagePath" 
            v-on:load    = "editViewLoaded"
            frameborder  = "0"></iframe>
    </div>
</template>

<script>
export default {
    props: ['model'],

    mounted: function() {
        console.log('===== mounted: set initial state =====')
        this.$root.$set(perAdminView.state, 'editViewHeight',  'auto') 
    },

    beforeDestroy: function () {
        console.log('===== beforeDestroy: remove state =====')
        this.$root.$delete(perAdminView.state, 'editViewHeight') 
    },

    computed: {
        pagePath: function() {
            return perAdminView.pageView.path + '.html'
        },
        editViewHeight: function() {
            return perAdminView.state.editViewHeight
        }
    },
    methods: {
        setEditViewHeight: function(height){
            console.log('===== METHOD: setEditViewHeight =====')
            perAdminView.state.editViewHeight = height + 'px'
        },

        getIframeHeight: function(id) {
            console.log('===== METHOD: getIframeHeight =====')
            var iframe = this.$el.children[id]
            return iframe.contentDocument.body.clientHeight;
        },

        editViewLoaded: function(ev) {
            console.log('===== METHOD: editViewLoaded =====')
            perHelperModelAction('getConfig', perAdminView.pageView.path)
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
                perHelperAction(this, 'showComponentEdit', targetEl.getAttribute('data-per-path'))
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

                var y = pos.y - targetBox.top
                if(y < targetBox.height/2) {
                    this.setStyle(editable, targetBox, '-top', '1px solid red')
                } else {
                    this.setStyle(editable, targetBox, '-bottom', '1px solid red')
                }
            } else {
                this.leftArea()
            }

        },

        showComponentEdit: function(me, target) {
            perHelperModelAction('editComponent', target)
        },

        drop: function(e) {
            var editable = this.$el.children['editviewoverlay'].children['editable']
            editable.style.display = 'none'

            var targetEl = this.getTargetEl(e)

            var componentPath = e.dataTransfer.getData('component')

            perHelperModelAction('addComponentToPath', { pagePath : perAdminView.pageView.path, path: targetEl.getAttribute('data-per-path'), component: componentPath})

            /* 
                this method should be called from the component in the iframe 
                once the component has been added, and we can remove the timeout...
            */
            var self = this
            setTimeout(function(){ 
                self.setEditViewHeight(self.getIframeHeight('editview'))
            }, 1000);
        }
    }
}
</script>
