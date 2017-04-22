var cmpAdminComponentsDebugger = (function () {
'use strict';

var template = {render: function(){var _vm=this;var _h=_vm.$createElement;var _c=_vm._self._c||_h;return _c('div',{style:(_vm.elementStyle)},[(!_vm.visible)?_c('a',{attrs:{"href":"#"},on:{"click":function($event){$event.stopPropagation();$event.preventDefault();_vm.showDebugger(true);}}},[_vm._v("show data")]):_vm._e(),_vm._v(" "),(_vm.visible)?_c('a',{staticStyle:{"position":"fixed","bottom":"20px","left":"0","background-color":"black","color":"white"},attrs:{"href":"#"},on:{"click":function($event){$event.stopPropagation();$event.preventDefault();_vm.showDebugger(false);}}},[_vm._v("hide data")]):_vm._e(),(_vm.visible)?_c('pre',[_vm._v("\r\n"+_vm._s(_vm.jsonview)+"\r\n")]):_vm._e()])},staticRenderFns: [],
    props: ['model']
    ,
    computed: {
        jsonview: function() {
            return JSON.stringify(this.$root.$data, true, 2)
        },
        elementStyle: function() {
            if(this.visible) {
                return "position: fixed; bottom: 0; left: 0; top: 0; right: 0; height: 100%; width: 100%; overflow: scroll; background-color: black; color: white;"
            } else {
                return "position: fixed; bottom: 20px; left: 0;"
            }
        }

    },
    data: function() {
        if(!this.visible) {
            this.visible = false;
        }
        return { visible: this.visible }
    },
    methods: {
        showDebugger: function(show) {
            this.visible = show;
        }
    }
};

return template;

}());
