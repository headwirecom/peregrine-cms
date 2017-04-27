var cmpAdminComponentsFileupload = (function () {
'use strict';

var template = {render: function(){var _vm=this;var _h=_vm.$createElement;var _c=_vm._self._c||_h;return _c('span',[_c('input',{staticClass:"form-control",attrs:{"id":"fileupload","type":"file","placeholder":"upload file"}}),_vm._v(" "),_c('button',{staticClass:"btn",on:{"click":_vm.uploadFile}},[_vm._v("upload")])])},staticRenderFns: [],
    props: ['model'],
    data: function() {

            return {
            formModel: { file: '' },
            schema: { fields: [{
                       type: "input",
                       inputType: "file",
                       label: "File",
                       model: 'file',
                       required: false,
                       placeholder: "upload file"
                       },{
                        type: "submit",
                        buttonText: "upload",
                        onSubmit: this.uploadFile,
                        model: "upload"
                        }]},
            formOptions: {
                          validateAfterLoad: true,
                          validateAfterChanged: true
                        }
                   }

    }, methods: {
        uploadFile: function() {
            perHelperModelAction('uploadFiles', { path: perAdminView.state.tools.assets.value, files: this.$el.children[0].files });
        }
    }
};

return template;

}());
