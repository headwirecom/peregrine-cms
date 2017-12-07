let objectsPage = require('../pages/Objects.page')
let {Explorer, SubNav, AddObjectWizard} = objectsPage

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
    
    describe('Add a new object', function() {
    	it('should see the subnav', function() {
        	SubNav.container.waitForVisible()
        	expect( SubNav.container.isVisible() ).to.equal(true)
        })

        it('clicking add object should navigate to add object wizard', function() {
            SubNav.addObjectButton.click()
            AddObjectWizard.container.waitForVisible()
            expect( browser.getUrl() ).to.contain('content/admin/objects/create.html/path:/content/objects')
        })
        
        let exampleTemplate
        
        it('should see allfields in template list', function() {
            AddObjectWizard.container.waitForVisible()
            const templates = AddObjectWizard.templates
            const i = templates.findIndex( template => template.text.indexOf('allfields') > -1 ) 
            exampleTemplate = templates[i]
            expect( exampleTemplate.text ).to.contain('allfields')
        })
        /*
        it('selecting allfields template', function() {
            AddObjectWizard.container.waitForVisible()
            expect( browser.getUrl() ).to.contain('content/admin/objects/create.html/path:/content/objects')
        })
        
        it('clicking next button should take wizard to step 2', function() {
            AddObjectWizard.container.waitForVisible()
            expect( browser.getUrl() ).to.contain('content/admin/objects/create.html/path:/content/objects')
        })*/

    })
    

})