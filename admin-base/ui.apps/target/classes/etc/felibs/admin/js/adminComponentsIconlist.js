var cmpAdminComponentsIconlist = (function () {
'use strict';

var template = {render: function(){var _vm=this;var _h=_vm.$createElement;var _c=_vm._self._c||_h;return _c('div',{staticClass:"row"},_vm._l((_vm.fromSource),function(action){return _c('admin-components-iconaction',{attrs:{"model":action}})}))},staticRenderFns: [],
    props: ['model']
    ,
    computed: {
        fromSource: function() {
            var segments = this.model.source.split('/').slice(1);
            var ret = this.$root.$data;
            for(var i = 0; i < segments.length; i++) {
                ret = ret[segments[i]];
            }
            return ret
        }
    }
};

return template;

}());
