var cmpAdminComponentsEditor = (function () {
'use strict';

var template = {render: function(){var _vm=this;var _h=_vm.$createElement;var _c=_vm._self._c||_h;return _c('div',[_c('p',[_vm._v("editor")]),_vm._v(_vm._s(this.$root.$data.state.editor)),_c('vue-form-generator',{attrs:{"schema":_vm.schema,"model":_vm.formdata,"options":_vm.formOptions}})],1)},staticRenderFns: [],
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

        schema:{ fields:[{
           type: "input",
           inputType: "text",
           label: "ID (disabled text field)",
           model: "id",
           readonly: true,
           disabled: true
        },{
           type: "input",
           inputType: "text",
           label: "Name",
           model: "name",
           placeholder: "Your name",
           featured: true,
           required: true
        }]},

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
