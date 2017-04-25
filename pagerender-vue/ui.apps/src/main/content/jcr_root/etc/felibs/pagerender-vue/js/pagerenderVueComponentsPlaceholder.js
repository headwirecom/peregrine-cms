var cmpPagerenderVueComponentsPlaceholder = (function () {
'use strict';

var template = {render: function(){var _vm=this;var _h=_vm.$createElement;var _c=_vm._self._c||_h;return (_vm.isEditMode)?_c('div',{staticStyle:{"border":"1px solid #c0c0c0","width":"95%","padding":"4px","margin":"4px","text-align":"center"},attrs:{"data-per-path":_vm.model.path,"data-per-droptarget":"true"},on:{"allowdrop":_vm.allowDrop,"drop":_vm.drop}},[_vm._v(_vm._s(_vm.componentName))]):_vm._e()},staticRenderFns: [],
    props: ['model'],
    computed: {
        isEditMode: function() {
            if(window.parent) {
                if(window.parent.perAdminView) {
                    return true
                }
            }
            return false
        },
        componentName: function() {
            return this.model.component.split('-').pop()
        }
    },
    methods: {
        allowDrop: function(e) {
            e.preventDefault();
        },
        drop: function(e) {
            alert('component drop');
        },
        edit: function(e) {
            alert('edit');
        }
    }
};

return template;

}());
