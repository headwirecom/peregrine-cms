let objectsPage = require('../pages/Objects.page')
let {Explorer, Workspace, EditorPanel} = objectsPage
let {ComponentExplorer, ContentView} = Workspace

describe('Peregrine objects page', function () {
    it('should login', function() {
    	objectsPage.open()
        expect( browser.getUrl() ).to.contain('content/admin/objects.html')
    })

    describe('Objects Explorer', function() {
        let exampleFolder
        
        it('should have a folder titled "example"', function(){
        	Explorer.container.waitForVisible()
            const folders = Explorer.folders
            const i = folders.findIndex( folder => folder.text.indexOf('example') > -1 ) 
            exampleFolder = folders[i]
            expect( exampleFolder.text ).to.contain('example')
        })
        /*
        it('clicking edit item should load the page editor workspace', function(){
            exampleSite.editButton.click()
            Workspace.container.waitForVisible()
            expect( browser.getUrl() ).to.contain('content/sites/example' )
            expect( Workspace.container.isVisible() ).to.equal(true)
        }) */

    })
    

})