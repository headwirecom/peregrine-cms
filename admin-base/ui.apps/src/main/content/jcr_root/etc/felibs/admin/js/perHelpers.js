function perHelperFindNodeFromPath(node, path) {

    if(node.path === path) return node
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

function perHelperSet(node, path, value) {

    path = path.slice(1).split('/').reverse()
    while(path.length > 1) {
        var segment = path.pop()
        if(!node[segment]) {
            node[segment] = {}
        }
        node = node[segment]
    }
    node[path[0]] = value
}


var modelActions = {

    selectToolsPagesPath: function(target) {
        loadData('/pages', target.selected)
        console.log(target)
        perHelperSet(perAdminView, target.path+'/value', target.selected)
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
                perAdminView.pageView.page = response.data
                perAdminView.pageView.path = target
                loadContent('/content/admin/pages/edit.html/path//'+target)
        }).catch(function(ex) {
            console.error('was not able to open edit view for', target)
        })

    },

    editComponent: function(target) {
        var content = perHelperFindNodeFromPath(perAdminView.pageView.page, target)
        loadData('/component', content.component)
        peregrineAdminApp.$set(perAdminView.state.editor, 'path', target)
    },

    saveEdit: function(target) {
        var content = perHelperFindNodeFromPath(perAdminView.pageView.page, target.path)
        var nodeData = JSON.parse(JSON.stringify(content))
        delete nodeData['children']
        nodeData['sling:resourceType'] = content.component.split('-').join('/')
        var data = new FormData()
        data.append(':operation', 'import')
        data.append(':contentType', 'json')
        data.append(':replaceProperties', 'true')
        data.append(':content', JSON.stringify(nodeData))

        axios.post(target.pagePath + target.path, data).then( function(res) {
            peregrineAdminApp.$set(perAdminView.state, 'editor', {})
            peregrineAdminApp.$set(perAdminView.state.editor, 'path', undefined)
        }).catch(function(error) {
            console.error('update component failed', error)
            peregrineAdminApp.$set(perAdminView.state.editor, 'path', undefined)
        })
    },

    addComponentToPath: function(target) {

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

        axios.post(target.pagePath + target.path, data).then( function(res) {
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