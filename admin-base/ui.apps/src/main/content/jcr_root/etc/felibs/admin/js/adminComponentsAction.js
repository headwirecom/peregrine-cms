var cmpAdminComponentsAction = (function () {
'use strict';

var template = {render: function(){var _vm=this;var _h=_vm.$createElement;var _c=_vm._self._c||_h;return _c('span',[(!_vm.model.type)?_c('a',{class:_vm.model.classes,attrs:{"href":_vm.model.target},on:{"click":function($event){$event.stopPropagation();$event.preventDefault();_vm.action($event);}}},[_vm._v(_vm._s(_vm.model.title)),_vm._t("default")],2):_vm._e(),(_vm.model.type === 'icon')?_c('a',{staticClass:"btn-floating waves-effect waves-light",class:_vm.model.classes,attrs:{"href":_vm.model.target},on:{"click":function($event){$event.stopPropagation();$event.preventDefault();_vm.action($event);}}},[_c('i',{staticClass:"material-icons"},[_vm._v(_vm._s(_vm.model.title)),_vm._t("default")],2)]):_vm._e()])},staticRenderFns: [],
    props: ['model' ],
    methods: {
        action: function(e) {
            perHelperAction(this, this.model.command, this.model.target);
        }
    }
};

return template;

}());
