var cmpAdminComponentsIconaction = (function () {
'use strict';

var template = {render: function(){var _vm=this;var _h=_vm.$createElement;var _c=_vm._self._c||_h;return _c('div',{staticClass:"col m4"},[_c('div',{staticClass:"card indigo darken-1"},[_c('div',{staticClass:"card-content white-text"},[_c('span',{staticClass:"card-title"},[_vm._v(_vm._s(_vm.model.title))]),_c('p',[_vm._v(_vm._s(_vm.model.description))]),_c('div',{staticClass:"card-action"},[_c('admin-components-action',{attrs:{"model":{ target: _vm.model.action, command: 'selectPath', title: 'explore' }}})],1)])])])},staticRenderFns: [],
    props: ['model']
};

return template;

}());
