var cmpAdminComponentsWorkspace = (function () {
'use strict';

var template = {render: function(){var _vm=this;var _h=_vm.$createElement;var _c=_vm._self._c||_h;return _c('div',{staticStyle:{"width":"100%","height":"90%","display":"flex","margin-bottom":"0px"}},[_c(_vm.getChildByPath('contentview').component,{tag:"component",staticStyle:{"flex":"4","height":"100%"},attrs:{"model":_vm.getChildByPath('contentview')}}),_c(_vm.getChildByPath('editor').component,{tag:"component",staticStyle:{"flex":"1"},attrs:{"model":_vm.getChildByPath('editor')}}),_c(_vm.getChildByPath('components').component,{tag:"component",staticStyle:{"flex":"1"},attrs:{"model":_vm.getChildByPath('components')}})],1)},staticRenderFns: [],
    props: ['model'],
    methods: {

        getChildByPath: function(childName) {
            var this$1 = this;

            var path = this.model.path+'/'+childName;
            for(var i = 0; i < this.model.children.length; i++) {
                if(this$1.model.children[i].path === path) {
                    var ret = this$1.model.children[i];
                    ret.classes = 'col fullheight s4';
                    return ret
                }
            }
            return null
        }

    }
};

return template;

}());
