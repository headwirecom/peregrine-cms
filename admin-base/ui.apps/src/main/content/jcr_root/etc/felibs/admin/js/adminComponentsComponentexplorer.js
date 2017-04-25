var cmpAdminComponentsComponentexplorer = (function () {
'use strict';

var template = {render: function(){var _vm=this;var _h=_vm.$createElement;var _c=_vm._self._c||_h;return _c('div',{staticStyle:{"height":"90%"}},[_c('p',[_vm._v("components")]),_c('div',{staticClass:"collection",staticStyle:{"height":"100%","overflow":"auto"}},_vm._l((this.$root.$data.admin.components.data),function(cmp){return _c('a',{staticClass:"collection-item",attrs:{"draggable":"true"},on:{"dragstart":function($event){_vm.onDragStart(cmp, $event);}}},[_vm._v(_vm._s(cmp.path.split('/')[2])+" "+_vm._s(cmp.name))])}))])},staticRenderFns: [],
    props: ['model'],
    methods: {
        onDragStart: function(cmp, ev) {
            if(ev) {
                ev.dataTransfer.setData('component', cmp.path);
            }
        }
    }
};

return template;

}());
