var cmpPagerenderVueComponentsBase = (function () {
'use strict';

var template = {render: function(){var _vm=this;var _h=_vm.$createElement;var _c=_vm._self._c||_h;return _c('p',{attrs:{"data-per-path":_vm.model.path},domProps:{"innerHTML":_vm._s(_vm.model.text)}})},staticRenderFns: [],
    props: [ 'model'],
    mounted: function() {
        if(window.parent && window.parent.perAdminView) {
            var box = this.$el.getBoundingClientRect();
            if(box.width === 0 || box.height === 0) {
                var div = document.createElement('div');
                div.setAttribute('data-per-path', this.model.path);
                var text = document.createTextNode('empty');
                div.appendChild(text);
                this.$el.appendChild(div);
            }
        }
    }
};

return template;

}());
