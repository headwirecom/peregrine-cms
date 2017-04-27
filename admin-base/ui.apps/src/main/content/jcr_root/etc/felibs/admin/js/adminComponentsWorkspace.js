var cmpAdminComponentsWorkspace = (function () {
'use strict';

var template = {render: function(){var _vm=this;var _h=_vm.$createElement;var _c=_vm._self._c||_h;return _c('div',{staticStyle:{"width":"100%","height":"90%","display":"flex","margin-bottom":"0px"}},[_c(_vm.getChildByPath('contentview').component,{tag:"component",staticStyle:{"flex":"4","height":"100%"},attrs:{"model":_vm.getChildByPath('contentview')}}),_c(_vm.getChildByPath('editor').component,{tag:"component",staticClass:"z-depth-2",staticStyle:{"padding":"0 0.75rem"},style:(_vm.getEditorStyle()),attrs:{"model":_vm.getChildByPath('editor')}}),_c(_vm.getChildByPath('components').component,{tag:"component",staticClass:"z-depth-2 fullheight",style:(_vm.getStyleForComponent('components')),attrs:{"model":_vm.getChildByPath('components')}})],1)},staticRenderFns: [],
    props: ['model'],
    updated: function() {
        this.$children[0].resizeOverlay();
    },
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
        },

        getEditorStyle: function() {
            if(perAdminView.state.editor && perAdminView.state.editor.dialog) {
                return 'flex: 1;'
            } else {
                return 'width: 0px; display: none'
            }
        },

        getStyleForComponent: function(name) {
            if(perAdminView.state[name] === undefined) { this.$root.$set(perAdminView.state, name, true); }
            return perAdminView.state[name] ? 'flex: 1; height: 100%; padding: 0 0.75rem;' : 'width: 0px;'
        },

        showHide: function(me, name) {
            console.log('showHide of', name, 'called');
            perAdminView.state[name] = !perAdminView.state[name];
        }

    }
};

return template;

}());
