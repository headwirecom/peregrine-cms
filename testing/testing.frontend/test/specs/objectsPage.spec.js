let objectsPage = require('../pages/Objects.page')
let {Explorer, Workspace, EditorPanel} = objectsPage
let {ComponentExplorer, ContentView} = Workspace

describe('Peregrine objects page', function () {
    it('should login', function() {
    	objectsPage.open()
        expect( browser.getUrl() ).to.contain('content/admin/objects.html')
    })

    

})