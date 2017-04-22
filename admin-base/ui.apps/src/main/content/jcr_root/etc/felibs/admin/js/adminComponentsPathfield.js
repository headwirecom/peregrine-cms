var cmpAdminComponentsPathfield = (function () {
'use strict';

var template = {render: function(){var _vm=this;var _h=_vm.$createElement;var _c=_vm._self._c||_h;return _c('div',[_c('input',{directives:[{name:"model",rawName:"v-model.lazy",value:(_vm.path.value),expression:"path.value",modifiers:{"lazy":true}}],domProps:{"value":(_vm.path.value)},on:{"change":function($event){_vm.path.value=$event.target.value;}}})])},staticRenderFns: [],
    props: ['model']
    ,
    data: function() {
        var this$1 = this;

        var dataDefault = this.model.dataDefault;
        var dataFrom    = this.model.dataFrom;

        var segments = dataFrom.split('/').slice(1);

        var node = this.$root.$data;
        for(var i = 0; i < segments.length; i++) {
            var next = node[segments[i]];
            if(!next) {
                next = this$1.$root.$set(node, segments[i], {});
            }
            node = next;
        }
        if(!node.value) {
            this.$root.$set(node, 'value', dataDefault.toString());
            loadData('/pages', dataDefault.toString());
        }

        return { path: node }
    }
};

return template;

}());
