var cmpExampleComponentsNav = (function () {
'use strict';

var template = {render: function(){var _vm=this;var _h=_vm.$createElement;var _c=_vm._self._c||_h;return _c('nav',{staticClass:"navbar navbar-toggleable-md navbar-light bg-faded",attrs:{"data-per-path":_vm.model.path}},[_vm._m(0),_vm._v(" "),_c('a',{staticClass:"navbar-brand",attrs:{"href":"#"}},[_vm._v(_vm._s(_vm.model.brand))]),_vm._m(1)])},staticRenderFns: [function(){var _vm=this;var _h=_vm.$createElement;var _c=_vm._self._c||_h;return _c('button',{staticClass:"navbar-toggler navbar-toggler-right",attrs:{"type":"button","data-toggle":"collapse","data-target":"#navbarSupportedContent","aria-controls":"navbarSupportedContent","aria-expanded":"false","aria-label":"Toggle navigation"}},[_c('span',{staticClass:"navbar-toggler-icon"})])},function(){var _vm=this;var _h=_vm.$createElement;var _c=_vm._self._c||_h;return _c('div',{staticClass:"collapse navbar-collapse",attrs:{"id":"navbarSupportedContent"}},[_c('ul',{staticClass:"navbar-nav mr-auto"},[_c('li',{staticClass:"nav-item active"},[_c('a',{staticClass:"nav-link",attrs:{"href":"#"}},[_vm._v("intro")])])])])}],
    props: ['model']
};

return template;

}());
