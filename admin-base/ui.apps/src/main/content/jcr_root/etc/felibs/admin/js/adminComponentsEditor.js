var cmpAdminComponentsEditor = (function () {
'use strict';

var template = {render: function(){var _vm=this;var _h=_vm.$createElement;var _c=_vm._self._c||_h;return _c('div',[_c('p',[_vm._v("editor")]),_c('form',[(this.$root.$data.state.editor)?_c('vue-form-generator',{attrs:{"schema":this.$root.$data.state.editor.dialog,"model":_vm.getModel(this.$root.$data.state.editor.path),"options":_vm.formOptions}}):_vm._e(),(this.$root.$data.state.editor.path)?_c('button',{staticClass:"btn",on:{"click":function($event){$event.stopPropagation();$event.preventDefault();_vm.onOk($event);}}},[_vm._v("ok")]):_vm._e()],1)])},staticRenderFns: [],
    props: ['model'],
    methods: {
        getModel: function(path) {
            if(path) {
                if(perAdminView.pageView.page) {
                    return perHelperFindNodeFromPath(perAdminView.pageView.page, path)
                }
            }
            return {}
        },
        onOk: function(e) {
            perHelperModelAction('saveEdit', perAdminView.state.editor.path);
        }
    },
    data: function() {

        var test = 'hello';
        return {

        formOptions: {
          validateAfterLoad: true,
          validateAfterChanged: true
        }

      }
  },
  beforeMount: function() {
    if(!perAdminView.state.editor) { this.$set(perAdminView.state, 'editor', { }); }
  }
};

return template;

}());
