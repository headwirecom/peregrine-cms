var cmpPagerenderVueStructureContainer = (function () {
'use strict';

var template = {render: function(){var _vm=this;var _h=_vm.$createElement;var _c=_vm._self._c||_h;return _c('div',{staticClass:"container"},[_vm._l((_vm.model.children),function(child){return _c('div',[_c(child.component,{tag:"component",attrs:{"model":child}})],1)}),_c('pagerender-vue-components-placeholder',{attrs:{"model":{ path: _vm.model.path, component: _vm.model.component }}})],2)},staticRenderFns: [],
    props: [ 'model' ]
};

return template;

}());
