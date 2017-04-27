var cmpAdminComponentsComponentexplorer = (function () {
'use strict';

var template = {render: function(){var _vm=this;var _h=_vm.$createElement;var _c=_vm._self._c||_h;return _c('div',{staticClass:"component-explorer blue-grey lighten-5"},[_c('div',{staticClass:"toggle-content-explorer blue-grey lighten-5"},[_c('admin-components-action',{attrs:{"model":{ target: 'components', command: 'showHide' }}},[_c('i',{staticClass:"material-icons"},[_vm._v("keyboard_arrow_left")])])],1),(_vm.isVisible)?_c('div',[_c('span',{staticClass:"panel-title"},[_vm._v("Components")]),(this.$root.$data.admin.components)?_c('div',{staticClass:"collection"},_vm._l((_vm.componentList()),function(cmp){return _c('a',{staticClass:"collection-item",attrs:{"draggable":"true"},on:{"dragstart":function($event){_vm.onDragStart(cmp, $event);}}},[_c('i',{staticClass:"material-icons"},[_vm._v("drag_handle")]),_vm._v(" "+_vm._s(cmp.path.split('/')[2])+" "+_vm._s(cmp.name))])})):_vm._e()]):_c('div',[_vm._v("loading")])])},staticRenderFns: [],
    props: ['model'],
    computed: {
        isVisible: function() {
            return this.$root.$data.state.components
        },

        showHideClass: function() {
            return this.$root.$data.state.components ? 'comp-visible' : 'comp-hidden'

        }
    },
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
