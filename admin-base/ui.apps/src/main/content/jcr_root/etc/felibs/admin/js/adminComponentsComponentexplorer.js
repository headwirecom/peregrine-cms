var cmpAdminComponentsComponentexplorer = (function () {
'use strict';

var template = {render: function(){var _vm=this;var _h=_vm.$createElement;var _c=_vm._self._c||_h;return _c('div',{staticStyle:{"height":"90%"}},[_c('p',[_vm._v("components")]),(this.$root.$data.admin.components)?_c('div',{staticClass:"collection",staticStyle:{"height":"100%","overflow":"auto"}},_vm._l((_vm.componentList()),function(cmp){return _c('a',{staticClass:"collection-item",attrs:{"draggable":"true"},on:{"dragstart":function($event){_vm.onDragStart(cmp, $event);}}},[_vm._v(_vm._s(cmp.path.split('/')[2])+" "+_vm._s(cmp.name))])})):_vm._e()])},staticRenderFns: [],
    props: ['model'],
    methods: {
        onDragStart: function(cmp, ev) {
            if(ev) {
                ev.dataTransfer.setData('component', cmp.path);
            }
        },
        componentList: function() {
            if(!this.$root.$data.admin.components) { return {} }
            if(!this.$root.$data.admin.currentPageConfig) { return {} }
            var allowedComponents = this.$root.$data.admin.currentPageConfig.allowedComponents;
            var list = this.$root.$data.admin.components.data;
            if(!list || !allowedComponents) { return {} }

            var ret = [];
            for(var i = 0; i < list.length; i++) {
                var path = list[i].path;
                for(var j = 0; j < allowedComponents.length; j++) {
                    if(path.startsWith(allowedComponents[j])) {
                        ret.push(list[i]);
                        break;
                    }
                }
            }
            return ret
        }

    }
};

return template;

}());
