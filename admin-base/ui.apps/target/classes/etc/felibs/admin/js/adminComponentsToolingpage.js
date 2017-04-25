var cmpAdminComponentsToolingpage = (function () {
'use strict';

var template = {render: function(){var _vm=this;var _h=_vm.$createElement;var _c=_vm._self._c||_h;return _c('div',{staticStyle:{"position":"fixed","width":"100%","height":"100%","top":"0","bottom":"0","left":"0","right":"0","overflow":"scroll"}},[_vm._l((_vm.model.children),function(child){return [_c(child.component,{tag:"component",attrs:{"model":child}})]})],2)},staticRenderFns: [],
    props: [ 'model' ],
    methods: {
        selectPath: function(me, target) {
            loadContent(target+'.html');
        },
        editPage: function(me, target) {
            console.log('edit page');
        }
    }
};

return template;

}());
