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
        
        it('clicking example folder item should load all objects', function(){
        	exampleFolder.linkButton.click()
        	expect( browser.getUrl() ).to.contain('content/objects/example' )
        }) 
        
        let exampleObject
        
        it('should have an object titled "allfields"', function(){
        	Explorer.container.waitForVisible()
            const objects = Explorer.objects
            const i = objects.findIndex( object => object.text.indexOf('allfields') > -1 ) 
            exampleObject = objects[i]
            expect( exampleObject.text ).to.contain('allfields')
        })

    })
    

})