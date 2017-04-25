function perHelperFindNodeFromPath(node, path) {

    if(node.children) {
        for(var i = 0; i < node.children.length; i++) {
            if(node.children[i].path === path) {
                // found match
                return node.children[i]
            } else if(path.indexOf(node.children[i].path) === 0) {
                var res = perHelperFindNodeFromPath(node.children[i], path)
                if(res) return res
            }
        }
    }
}

function perHelperAction(component, command, target) {
    if(component.$options.methods && component.$options.methods[command]) {
        component.$options.methods[command](component, target)
    } else {
        if(component.$parent === component.$root) {
            console.error('action', command, 'not found, ignored', target)
        } else {
            perHelperAction(component.$parent, command, target)
        }
    }
}


var modelActions = {

    selectToolsPagesPath: function(target) {
        loadData('/pages', target)
        perAdminView.state.tools.pages.value = target;
    },

    createPage: function(target) {
        var content = { 'jcr:primaryType': 'per:Page', 'jcr:content': { 'jcr:primaryType': 'per:PageContent', 'jcr:title': target.name, 'sling:resourceType': 'example/components/page'}}

        var data = new FormData()
        data.append(':operation', 'import')
        data.append(':contentType', 'json')
        data.append(':name', target.name)
        data.append(':content', JSON.stringify(content))

        axios.post(target.parent, data).then( function(res) {
            loadData('/pages', target.parent)
        }).catch(function(error) {
            console.error('create page failed with', error)
        })
        console.info('page creation complete for', target.name, 'at', target.parent)
    },
    editPage: function(target) {
        console.log('editPage: loading', target)
        axios.get(target+'.data.json').then(function (response) {
                perAdminView.pageView = response.data
                loadContent('/content/admin/pages/edit.html')
        }).catch(function(ex) {
            console.error('was not able to open edit view for', target)
        })

    },

    editComponent: function(target) {
        peregrineAdminApp.$set(perAdminView.state.editor, 'path', target);
        var content = perHelperFindNodeFromPath(perAdminView.pageView.page, target)
        loadData('/component', content.component)
    },

    saveEdit: function(target) {
        var content = perHelperFindNodeFromPath(perAdminView.pageView.page, target)
        var nodeData = JSON.parse(JSON.stringify(content))
        delete nodeData['children']
        nodeData['sling:resourceType'] = content.component.split('-').join('/')
        var data = new FormData()
        data.append(':operation', 'import')
        data.append(':contentType', 'json')
        data.append(':replaceProperties', 'true')
        data.append(':content', JSON.stringify(nodeData))

        axios.post(target, data).then( function(res) {
            peregrineAdminApp.$set(perAdminView.state, 'editor', {})
        }).catch(function(error) {
            console.error('update component failed', error)
        })
    },

    addComponentToPath: function(target) {

        console.log('>>> target path',target.path)
        console.log('>>> target cmp ', target.component)
        var content = perHelperFindNodeFromPath(perAdminView.pageView.page, target.path)
        var component = target.component.split('/').slice(2).join('-')
        var componentPath = target.component.split('/').slice(2).join('/')
        if(!content.children) {
            peregrineAdminApp.$set(content, 'children', [])
        }

        var data = new FormData()
        data.append(':operation', 'import')
        data.append(':contentType', 'json')
        data.append(':nameHint', component.split('-').pop())
        data.append(':http-equiv-accept', 'application/json')

        data.append(':content', JSON.stringify({ 'jcr:primaryType': 'nt:unstructured', 'sling:resourceType': componentPath }))

        axios.post(target.path, data).then( function(res) {
            console.log(JSON.stringify(res.data, true, 2))
            content.children.push({path: target.path, component: component, text: 'edit me'})
        }).catch(function(error) {
            console.error('insert component failed', error)
        })

    }

}


function perHelperModelAction(command, target) {
    if(modelActions[command]) {
        modelActions[command](target)
    } else {
        console.error('command', command, 'currently not supported')
    }
}