var cmpAdminComponentsContentview = (function () {
'use strict';

var template = {render: function(){var _vm=this;var _h=_vm.$createElement;var _c=_vm._self._c||_h;return _c('div',{staticClass:"peregrine-content-view"},[_c('div',{style:(("height: " + (_vm.editViewHeight))),attrs:{"id":"editviewoverlay"},on:{"click":_vm.click,"mousemove":_vm.mouseMove,"mouseout":_vm.leftArea,"dragover":_vm.dragOver,"drop":_vm.drop}},[_c('div',{attrs:{"id":"editable"}})]),_c('iframe',{style:(("height: " + (_vm.editViewHeight))),attrs:{"id":"editview","src":_vm.pagePath,"frameborder":"0"},on:{"load":_vm.editViewLoaded}})],1)},staticRenderFns: [],
    props: ['model'],

    mounted: function() {
        console.log('===== mounted: set initial state =====');
        this.$root.$set(perAdminView.state, 'editViewHeight',  'auto'); 
    },

    beforeDestroy: function () {
        console.log('===== beforeDestroy: remove state =====');
        this.$root.$delete(perAdminView.state, 'editViewHeight'); 
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
            console.log('===== METHOD: setEditViewHeight =====');
            perAdminView.state.editViewHeight = height + 'px';
        },

        getIframeHeight: function(id) {
            console.log('===== METHOD: getIframeHeight =====');
            var iframe = this.$el.children[id];
            return iframe.contentDocument.body.clientHeight;
        },

        editViewLoaded: function(ev) {
            console.log('===== METHOD: editViewLoaded =====');
            perHelperModelAction('getConfig', perAdminView.pageView.path);

            /* 
                this method should be called from the component in the iframe 
                once data has been returned, and we can remove the timeout...
            */
            var self = this;
            setTimeout(function(){ 
                self.setEditViewHeight(self.getIframeHeight('editview'));
            }, 1000);
        },

        resizeOverlay: function(event) {
            console.log('===== METHOD: resizeOverlay =====');
            console.log('unsure what is calling this method...');
            // var rect = this.$el.children['editview'].getBoundingClientRect()
            // var overlay = this.$el.children['editviewoverlay']
            // overlay.style.width = ''+(rect.width-20)+'px'
            // overlay.style.height = ''+(rect.height-20)+'px'
        },

        getPosFromMouse: function(e) {
            var elRect = this.$el.getBoundingClientRect();

            var posX = e.clientX - elRect.left;
            var posY = e.clientY - elRect.top;

            return {x: posX, y: posY }
        },

        getTargetEl: function(e) {

            var pos = this.getPosFromMouse(e);

            var editview = this.$el.children['editview'];
            var editable = this.$el.children['editviewoverlay'].children['editable'];

            var targetEl = editview.contentWindow.document.elementFromPoint(pos.x, pos.y);
            if(!targetEl) { return }

            while(!targetEl.getAttribute('data-per-path')) {
                targetEl = targetEl.parentElement;
                if(!targetEl) { break; }
            }
            return targetEl
        },

        click: function(e) {
            if(!e) { return }
            var targetEl = this.getTargetEl(e);
            if(targetEl) {
                perHelperAction(this, 'showComponentEdit', targetEl.getAttribute('data-per-path'));
            }
        },

        leftArea: function(e) {
            var editable = this.$el.children['editviewoverlay'].children['editable'];
            editable.style.display = 'none';
        },

        setStyle: function(editable, targetBox, name, border) {
            editable.style.top = targetBox.top+'px';
            editable.style.left = targetBox.left+'px';
            editable.style.width = targetBox.width+'px';
            editable.style.height = targetBox.height+'px';
            editable.style.display = 'block';
            editable.style['border'] = 'none';
            editable.style['border-top'] = 'none';
            editable.style['border-bottom'] = 'none';
            editable.style['border'+name] = border;
        },

        mouseMove: function(e) {
            if(!e) { return }
            var targetEl = this.getTargetEl(e);
            if(targetEl) {
                var targetBox = targetEl.getBoundingClientRect();
                this.setStyle(editable, targetBox, '', '1px solid red');
            }
        },

        dragOver: function(e) {
            e.preventDefault();
            var targetEl = this.getTargetEl(e);

            if(targetEl) {
                var pos = this.getPosFromMouse(e);
                var targetBox = targetEl.getBoundingClientRect();

                var y = pos.y - targetBox.top;
                if(y < targetBox.height/2) {
                    this.setStyle(editable, targetBox, '-top', '1px solid red');
                } else {
                    this.setStyle(editable, targetBox, '-bottom', '1px solid red');
                }
            } else {
                this.leftArea();
            }

        },

        showComponentEdit: function(me, target) {
            perHelperModelAction('editComponent', target);
        },

        drop: function(e) {
            var editable = this.$el.children['editviewoverlay'].children['editable'];
            editable.style.display = 'none';

            var targetEl = this.getTargetEl(e);

            var componentPath = e.dataTransfer.getData('component');

            perHelperModelAction('addComponentToPath', { pagePath : perAdminView.pageView.path, path: targetEl.getAttribute('data-per-path'), component: componentPath});

            /* 
                this method should be called from the component in the iframe 
                once the component has been added, and we can remove the timeout...
            */
            var self = this;
            setTimeout(function(){ 
                self.setEditViewHeight(self.getIframeHeight('editview'));
            }, 1000);
        }
    }
};

return template;

}());
