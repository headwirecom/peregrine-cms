var dataLoaders = new Array()
var pending = new Array()

function messageIfEmpty() {
    console.log('all pending data loaded')
    initPeregrineApp()
}

dataLoaders['/admin/tools'] = function() {
    console.log('data loader for /admin/tools called')

    axios.get('/content/admin/tools.model.json').then(function (response) {
        if(!perAdminView.admin) perAdminView.admin = {}
        if(peregrineAdminApp) {
            peregrineAdminApp.$set(perAdminView.admin, 'tools', response.data.children)
        } else {
            perAdminView.admin.tools = response.data.children
        }
        delete pending['/admin/tools']
        messageIfEmpty()
    }).catch(function(error) {
              console.error("error getting '/admin/tools'", error);
               delete pending['/admin/tools']
               messageIfEmpty()
          })
}

dataLoaders['/pages'] = function(target) {

    console.log('data loader for /pages with', target)
    var segments = target.split('/')
    var promises = []
    for(var i = 1; i < segments.length; i++) {
        var path = segments.slice(0, i+1).join('/')
        var url = '/bin/cpm/nodes/node.tree.json'+path
        promises.push(axios.get(url))
    }

    axios.all(promises).then(function (results) {
        results.forEach( function(result) {
            var path = (result.request.responseURL.slice(result.request.responseURL.indexOf('.json')+5))
            if(path === '/content') {
                perAdminView.pages.children.push(result.data)
            } else {
                var targetNode = perHelperFindNodeFromPath(perAdminView.pages, path)
                peregrineAdminApp.$set(targetNode, 'children', result.data.children)
            }
        })
        }).catch(function(error) {
              console.error("error getting '/pages'", error);
        })
}

function loadData(path, target) {

    pending['/admin/tools'] = true
    console.log('loading data for:', path)
    var loader = dataLoaders[path]
    if(loader) {
        if(target) {
            loader(target)
        } else {
            loader()
        }
    } else {
        console.error('no data loader for', path)
    }

}