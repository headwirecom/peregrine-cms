let objectsPage = require('../pages/Objects.page')
let {Explorer, SubNav, AddObjectWizard, ObjectEditorPanel} = objectsPage

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
        
        it('should have an object titled "sample"', function(){
        	Explorer.container.waitForVisible()
            const objects = Explorer.objects
            const i = objects.findIndex( object => object.text.indexOf('sample') > -1 ) 
            exampleObject = objects[i]
            expect( exampleObject.text ).to.contain('sample')
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
        
        it('should see sample in template list', function() {
            AddObjectWizard.container.waitForVisible()
            const templates = AddObjectWizard.templates
            const i = templates.findIndex( template => template.text.indexOf('sample') > -1 ) 
            exampleTemplate = templates[i]
            expect( exampleTemplate.text ).to.contain('sample')
        })
        
        it('selecting sample template', function() {
        	exampleTemplate.linkButton.click()
            expect( exampleTemplate.classAttribute ).to.contain('grey lighten-2')
        })
        
        it('clicking next button should go wizard step 2', function() {
            AddObjectWizard.nextButton.click()
            AddObjectWizard.objectNameField.waitForVisible()
            expect( AddObjectWizard.objectNameField.isVisible() ).to.equal(true)
        })
        
        it('clicking next button should go wizard step 3', function() {
            AddObjectWizard.objectNameField.setValue('myobject1')
            AddObjectWizard.nextButton.click()
            expect( AddObjectWizard.finishButton.isVisible() ).to.equal(true)
        })
        
       let newObject
        
       it('clicking finish button to add this object', function() {
        	AddObjectWizard.finishButton.scroll(0,10000)
            AddObjectWizard.finishButton.click()
            Explorer.container.waitForVisible()
            const objects = Explorer.objects
            const i = objects.findIndex( object => object.text.indexOf('myobject1') > -1 ) 
            newObject = objects[i]
            expect( newObject.text ).to.contain('myobject1')
        })

    })
    
    describe('Edit an object', function() {
    	let exampleObject
    	
    	it('find object to edit', function() {
	    	Explorer.container.waitForVisible()
	        const objects = Explorer.objects
	        const i = objects.findIndex( object => object.text.indexOf('myobject1') > -1 )
	        expect( i ).to.not.equal(-1)
	        exampleObject = objects[i]
	    })
    	
    	it('clicking edit item should load the object editor panel', function(){
    		exampleObject.editButton.click()
    		ObjectEditorPanel.container.waitForVisible()
            expect( ObjectEditorPanel.container.isVisible() ).to.equal(true)
        })
        
        let inputs
        
        it('editing field: text', function(){
    		inputs = ObjectEditorPanel.inputs
    		inputs[0].setValue('first text')
    		expect( inputs[0].getValue() ).to.equal('first text')
    	})
    	
    	it('editing field: textarea', function(){
    		inputs[1].setValue('first textarea')
    		expect( inputs[1].getValue() ).to.equal('first textarea')
    		ObjectEditorPanel.save.click()
        })
    	
    	let previewContainer
    	
    	it('field text gets saved', function(){
    		previewContainer = ObjectEditorPanel.previewContainer
    		expect( previewContainer.$('.form-group:nth-child(1) > .field-wrap > .wrapper > p').getText() ).to.equal('first text')
    	})
    	
    	it('field textarea gets saved', function(){
    		previewContainer = ObjectEditorPanel.previewContainer
    		expect( previewContainer.$('.form-group:nth-child(2) > .field-wrap > .wrapper > p').getText() ).to.equal('first textarea')
    	})
    })
    
    describe('Delete an object', function() {
    	
    	let exampleObject
    	
    	it('find object to be deleted', function() {
	    	Explorer.container.waitForVisible()
	        const objects = Explorer.objects
	        const i = objects.findIndex( object => object.text.indexOf('myobject1') > -1 )
	        expect( i ).to.not.equal(-1)
	        exampleObject = objects[i]
	    })
    	
    	it('delete object', function() {
    		exampleObject.deleteButton.click()
    		browser.alertAccept();
	    	Explorer.container.waitForVisible()
	        const objects = Explorer.objects
	        const i = objects.findIndex( object => object.text.indexOf('myobject1') > -1 )
	        expect( i ).to.equal(-1)
	    })
    })

})