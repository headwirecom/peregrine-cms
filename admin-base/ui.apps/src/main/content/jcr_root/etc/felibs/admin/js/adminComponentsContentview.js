var cmpAdminComponentsContentview = (function () {
'use strict';

var template = {render: function(){var _vm=this;var _h=_vm.$createElement;var _c=_vm._self._c||_h;return _c('div',{staticClass:"fullheight"},[_c('div',{staticStyle:{"position":"absolute"},attrs:{"id":"editviewoverlay"},on:{"click":_vm.click,"dragover":_vm.dragOver,"dragleave":_vm.leftArea}},[_c('div',{staticStyle:{"position":"absolute","border":"solid 1px blue","width":"10px","height":"10px"},attrs:{"id":"editable"}})]),_c('iframe',{staticStyle:{"padding-top":"2px"},attrs:{"id":"editview","src":"/content/sites/example.html","width":"100%","height":"100%","frameborder":"0"}})],1)},staticRenderFns: [],
    props: ['model'],
    mounted: function() {
        var rect = this.$el.children['editview'].getBoundingClientRect();
        var overlay = this.$el.children['editviewoverlay'];
        overlay.style.width = ''+rect.width+'px';
        overlay.style.height = ''+rect.height+'px';
    },
    methods: {
        click: function(e) {

            var elRect = this.$el.getBoundingClientRect();

            var posX = e.clientX - elRect.left;
            var posY = e.clientY - elRect.top;

            var editview = this.$el.children['editview'];
            var editable = this.$el.children['editviewoverlay'].children['editable'];

            var targetEl = editview.contentWindow.document.elementFromPoint(posX, posY);

            while(!targetEl.getAttribute('data-per-path')) {
                targetEl = targetEl.parent;
                if(!targetEl) { break; }
            }

            if(targetEl) {
                perHelperAction(this, 'showComponentEdit', targetEl.getAttribute('data-per-path'));
            }
        },

        leftArea: function(e) {
//            var editable = this.$el.children['editviewoverlay'].children['editable']
//            editable.style.display = 'none'
        },

        dragOver: function(e) {
            e.preventDefault();

            var elRect = this.$el.getBoundingClientRect();

            var posX = e.clientX - elRect.left;
            var posY = e.clientY - elRect.top;


            var editview = this.$el.children['editview'];
            var editable = this.$el.children['editviewoverlay'].children['editable'];


           var targetEl = editview.contentWindow.document.elementFromPoint(posX, posY);

            while(!targetEl.getAttribute('data-per-path')) {
                targetEl = targetEl.parent;
                if(!targetEl) { break; }
            }

            if(targetEl) {
                if(targetEl.getAttribute('data-per-path') !== this.currentPath) {
                    console.log(targetEl.getAttribute('data-per-path'));
                    var targetBox = targetEl.getBoundingClientRect();

                    editable.style.top = targetBox.top+'px';
                    editable.style.left = targetBox.left+'px';
                    editable.style.width = targetBox.width+'px';
                    editable.style.height = targetBox.height+'px';
                    editable.style.display = 'block';

                    this.currentPath = targetEl.getAttribute('data-per-path');
                }
            }

        },

        showComponentEdit: function(me, target) {
            perHelperModelAction('editComponent', target);
        }
    }
};

return template;

}());
