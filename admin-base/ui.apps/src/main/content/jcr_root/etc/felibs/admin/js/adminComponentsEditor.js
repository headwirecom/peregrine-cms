var cmpAdminComponentsEditor = (function () {
'use strict';

var template = {render: function(){var _vm=this;var _h=_vm.$createElement;var _c=_vm._self._c||_h;return _c('div',[_c('p',[_vm._v("editor")]),(this.$root.$data.state.editor)?_c('vue-form-generator',{attrs:{"schema":this.$root.$data.state.editor.dialog,"model":_vm.formdata,"options":_vm.formOptions}}):_vm._e()],1)},staticRenderFns: [],
    props: ['model'],
    data: function() { return {

        formdata:{
          id: 1,
          name: "John Doe",
          password: "J0hnD03!x4",
          skills: ["Javascript", "VueJS"],
          email: "john.doe@gmail.com",
          status: true
        },

        schema: {},

        formOptions: {
          validateAfterLoad: true,
          validateAfterChanged: true
        }
      }
  },
  mounted: function() {
    if(!perAdminView.state.editor) { this.$set(perAdminView.state, 'editor', { }); }
  }
};

return template;

}());
