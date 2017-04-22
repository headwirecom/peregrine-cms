function perHelperFindNodeFromPath(node, path) {

    if(node.children) {
        for(var i = 0; i < node.children.length; i++) {
            if(node.children[i].path === path) {
                // found match
                return node.children[i]
            } else if(path.indexOf(node.children[i].path) === 0) {
                return perHelperFindNodeFromPath(node.children[i], path)
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

    }
}


function perHelperModelAction(command, target) {
    if(modelActions[command]) {
        modelActions[command](target)
    } else {
        console.error('command', command, 'currently not supported')
    }
}