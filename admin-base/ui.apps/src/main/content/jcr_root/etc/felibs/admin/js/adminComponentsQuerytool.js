var cmpAdminComponentsQuerytool = (function () {
'use strict';

var template = {render: function(){var _vm=this;var _h=_vm.$createElement;var _c=_vm._self._c||_h;return _c('div',[_c('div',{staticClass:"row"},[_c('div',{staticClass:"col s12"},[_c('input',{directives:[{name:"model",rawName:"v-model",value:(_vm.querystring),expression:"querystring"}],domProps:{"value":(_vm.querystring)},on:{"input":function($event){if($event.target.composing){ return; }_vm.querystring=$event.target.value;}}}),_c('div',{staticClass:"right"},[_c('button',{staticClass:"btn",on:{"click":_vm.executeQuery}},[_vm._v("go")])])])]),_c('div',{staticClass:"row"},[_c('div',{staticClass:"col s12"},[_c('table',[_vm._m(0),_c('tbody',_vm._l((_vm.results.data),function(result){return _c('tr',[_c('td',[_vm._v(_vm._s(result.name))]),_c('td',[_vm._v(_vm._s(result.path))]),_c('td',[_c('a',{attrs:{"href":'/bin/browser.html'+result.path,"target":"composum"}},[_vm._v("view")])])])}))]),_c('ul',{staticClass:"pagination"},[_c('li',{staticClass:"waves-effect"},[_c('a',{attrs:{"href":""},on:{"click":function($event){$event.stopPropagation();$event.preventDefault();_vm.loadPage(-1);}}},[_c('i',{staticClass:"material-icons"},[_vm._v("chevron_left")])])]),_c('li',[_vm._v("Page "+_vm._s(_vm.page + 1))]),_c('li',{staticClass:"waves-effect"},[_c('a',{attrs:{"href":""},on:{"click":function($event){$event.stopPropagation();$event.preventDefault();_vm.loadPage(1);}}},[_c('i',{staticClass:"material-icons"},[_vm._v("chevron_right")])])])])])])])},staticRenderFns: [function(){var _vm=this;var _h=_vm.$createElement;var _c=_vm._self._c||_h;return _c('thead',[_c('tr',[_c('th',[_vm._v("Resource")]),_c('th',[_vm._v("Path")]),_c('th',[_vm._v("Actions")])])])}],
    props: ['model'],
    data: function() {

            if(!this.results) { this.results = {
                                                 pages: 0,
                                                 more: false,
                                                 data: []
                                             }; }

            return {
                querystring: 'select * from nt:base',
                page: 0,
                results: this.results

        }
    },
    methods: {
        executeQuery: function() {
            this.page = 0;
            this.more = false;
            this.query();
        },
        query: function() {
            var resObj = this.results;
            axios.get('/bin/search?q='+this.querystring+'&page='+this.page).then(function(result) {
                resObj.data = result.data.data;
                resObj.pages = result.data.pages;
                resObj.more = result.data.more;
            });
        },
        loadPage: function(increment) {
            this.page = this.page + increment;
            this.query();
        }
    }
};

return template;

}());
