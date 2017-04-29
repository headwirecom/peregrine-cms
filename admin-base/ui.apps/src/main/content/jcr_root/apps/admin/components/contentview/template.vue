<template>
    <div class="peregrine-content-view">
        <div 
            id            = "editviewoverlay" 
            v-bind:style  = "`height: ${editViewHeight}`" 
            v-on:click    = "click" 
            v-on:dragover = "dragOver" 
            v-on:drop     = "drop">
            <div id="editable"></div>
        </div>
        <iframe 
            id           = "editview" 
            v-bind:style = "`height: ${editViewHeight}`" 
            v-bind:src   = "pagePath" 
            v-on:load    = "editViewLoaded"
            frameborder  = "0">
        </iframe>
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

            /* 
                this method should be called from the component in the iframe 
                once data has been returned, and we can remove the timeout...
            */
            var self = this
            setTimeout(function(){ 
                self.setEditViewHeight(self.getIframeHeight('editview'))
            }, 1000);
        },

        resizeOverlay: function(event) {
            console.log('===== METHOD: resizeOverlay =====')
            console.log('unsure what is calling this method...')
            // var rect = this.$el.children['editview'].getBoundingClientRect()
            // var overlay = this.$el.children['editviewoverlay']
            // overlay.style.width = ''+(rect.width-20)+'px'
            // overlay.style.height = ''+(rect.height-20)+'px'
        },

        getTargetEl: function(e) {
            var elRect = this.$el.getBoundingClientRect()

            var posX = e.clientX - elRect.left
            var posY = e.clientY - elRect.top

            var editview = this.$el.children['editview']
            var editable = this.$el.children['editviewoverlay'].children['editable']

            var targetEl = editview.contentWindow.document.elementFromPoint(posX, posY)
            console.log('>>> getTargetEl', targetEl)
            if(!targetEl) return

            while(!targetEl.getAttribute('data-per-path')) {
                targetEl = targetEl.parentElement
                if(!targetEl) { break; }
            }
            return targetEl
        },
        click: function(e) {
            console.log('>>> click event',e)
            if(!e) return
            var targetEl = this.getTargetEl(e)
            console.log('>>> target     ',targetEl)
            if(targetEl) {
                perHelperAction(this, 'showComponentEdit', targetEl.getAttribute('data-per-path'))
            }
        },

        leftArea: function(e) {
//            var editable = this.$el.children['editviewoverlay'].children['editable']
//            editable.style.display = 'none'
        },

        dragOver: function(e) {
            e.preventDefault()

            var elRect = this.$el.getBoundingClientRect()

            var posX = e.clientX - elRect.left
            var posY = e.clientY - elRect.top


            var editview = this.$el.children['editview']
            var editable = this.$el.children['editviewoverlay'].children['editable']


           var targetEl = editview.contentWindow.document.elementFromPoint(posX, posY)

            while(!targetEl.getAttribute('data-per-path')) {
                targetEl = targetEl.parent
                if(!targetEl) { break; }
            }

            if(targetEl) {
                if(targetEl.getAttribute('data-per-path') !== this.currentPath) {
                    console.log(targetEl.getAttribute('data-per-path'))
                    var targetBox = targetEl.getBoundingClientRect()

                    editable.style.top = targetBox.top+'px'
                    editable.style.left = targetBox.left+'px'
                    editable.style.width = targetBox.width+'px'
                    editable.style.height = targetBox.height+'px'
                    editable.style.display = 'block'

                    this.currentPath = targetEl.getAttribute('data-per-path')
                }
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