var cmpAdminComponentsExplorer = (function () {
'use strict';

var template = {render: function(){var _vm=this;var _h=_vm.$createElement;var _c=_vm._self._c||_h;return _c('div',{staticClass:"explorer container"},[_vm._l((_vm.pathSegments),function(segment){return [_c('admin-components-action',{attrs:{"model":{ 
                target: segment.path, 
                title: segment.name, 
                command: 'selectPath', 
                classes: 'btn waves-effect waves-light blue-grey darken-3'
            }}})]}),(_vm.pt)?_c('div',[(_vm.pt)?_c('ul',{staticClass:"collection"},_vm._l((_vm.pt.children),function(child){return (_vm.checkIfAllowed(child.resourceType))?_c('a',{staticClass:"collection-item"},[_c('admin-components-action',{attrs:{"model":{ 
                        target: child.path, 
                        title: child.name, 
                        command: 'selectPath' 
                    }}}),_c('div',{staticClass:"secondary-content"},[_c('admin-components-action',{attrs:{"model":{ 
                            target: child.path, 
                            command: 'editPage'
                        }}},[_c('i',{staticClass:"material-icons"},[_vm._v("edit")])]),_c('span',[_c('a',{attrs:{"traget":"viewer","href":_vm.viewUrl(child.path)}},[_c('i',{staticClass:"material-icons"},[_vm._v("visibility")])])])],1)],1):_vm._e()})):_vm._e(),_vm._l((_vm.model.children),function(child){return [_c(child.component,{tag:"component",attrs:{"model":child}})]})],2):_vm._e()],2)},staticRenderFns: [],
    props: ['model'],
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
        viewUrl: function(path) {
            var segments = path.split('/');
            var last = segments.pop();
            if(last.indexOf('.') >= 0) {
                return path
            }
            return path + '.html'
        },
        checkIfAllowed: function(resourceType) {
            return ['nt:file', 'sling:Folder', 'sling:OrderedFolder', 'per:Page'].indexOf(resourceType) >= 0
        },
        selectPath: function(me, target) {
            perHelperModelAction('selectToolsPagesPath', { selected: target, path: me.model.dataFrom });
        },
        viewPage: function(me, target) {
            alert(target);
        },
        addPage: function(me, target) {
            var pageName = prompt('add page at '+me.pt.path);
            perHelperModelAction('createPage', { parent: me.pt.path, name: pageName });
        },
        editPage: function(me, target) {
            perHelperModelAction('editPage', target );
        }
    }

};

return template;

}());
