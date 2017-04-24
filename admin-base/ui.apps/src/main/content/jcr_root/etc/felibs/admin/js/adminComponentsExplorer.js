var cmpAdminComponentsExplorer = (function () {
'use strict';

var template = {render: function(){var _vm=this;var _h=_vm.$createElement;var _c=_vm._self._c||_h;return _c('div',{staticClass:"container"},[_vm._l((_vm.pathSegments),function(segment){return [_c('admin-components-action',{attrs:{"target":segment.path,"title":segment.name,"command":'selectPath',"classes":"btn waves-effect waves-light"}})]}),(_vm.pt)?_c('div',[(_vm.pt)?_c('ul',{staticClass:"collection"},_vm._l((_vm.pt.children),function(child){return (child.resourceType === 'per:Page')?_c('a',{staticClass:"collection-item"},[_c('admin-components-action',{attrs:{"target":child.path,"title":child.name,"command":'selectPath'}}),_vm._v("  "),_c('a',{staticClass:"secondary-content",attrs:{"traget":"viewer","href":child.path + '.html'}},[_c('i',{staticClass:"material-icons"},[_vm._v("send")])]),_vm._v("  "),_c('admin-components-action',{attrs:{"target":child.path,"command":'editPage',"classes":"secondary-content"}},[_c('i',{staticClass:"material-icons"},[_vm._v("edit")])])],1):_vm._e()})):_vm._e(),_c('admin-components-action',{attrs:{"target":_vm.pt.path,"title":'add page',"command":'addPage'}})],1):_vm._e()],2)},staticRenderFns: [],
    props: ['model']
    ,
    data: function() {
        var this$1 = this;

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

        return { path: node }
    },
    computed: {
        pt: function() {
            console.log(this.$data.path.value);
            return perHelperFindNodeFromPath(this.$root.$data.pages, this.$data.path.value)
        },
        parentPath: function() {
            var segments = this.$data.path.value.toString().split('/');
            var joined = segments.slice(0, segments.length -1).join('/');
            return joined
        },
        pathSegments: function() {
            var segments = this.$data.path.value.toString().split('/');
            var ret = [];
            for(var i = 1; i < segments.length; i++) {
                ret.push( { name: segments[i], path: segments.slice(0, i+1).join('/') } );
            }
            return ret;
        }
    },
    methods: {
        selectPath: function(me, target) {
            perHelperModelAction('selectToolsPagesPath', target);
        },
        viewPage: function(me, target) {
            alert(target);
        },
        addPage: function(me, target) {
            var pageName = prompt('add page');
            perHelperModelAction('createPage', { parent: target, name: pageName });
        },
        editPage: function(me, target) {
            perHelperModelAction('editPage', target );
        }
    }
};

return template;

}());
