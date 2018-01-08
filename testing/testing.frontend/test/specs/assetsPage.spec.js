let assetsPage = require('../pages/Assets.page')
let {Explorer, SubNav, AssetPreviewPanel, AssetEditorPanel} = assetsPage

describe('Peregrine assets page', function () {
    it('should login', function() {
    	assetsPage.open()
        expect( browser.getUrl() ).to.contain('content/admin/assets.html')
    })

    describe('Assets Explorer', function() {
        let exampleFolder
        
        it('should have a folder titled "example"', function(){
        	Explorer.container.waitForVisible()
            const folders = Explorer.folders
            const i = folders.findIndex( folder => folder.text.indexOf('example') > -1 ) 
            exampleFolder = folders[i]
            expect( exampleFolder.text ).to.contain('example')
        })
        
        let exampleAsset
        
        it('should have an asset titled "peregrine-logo.png"', function(){
        	Explorer.container.waitForVisible()
            const assets = Explorer.assets
            const i = assets.findIndex( asset => asset.text.indexOf('peregrine-logo.png') > -1 ) 
            exampleAsset = assets[i]
            expect( exampleAsset.text ).to.contain('peregrine-logo.png')
        })
        
        it('clicking asset item should open up asset preview panel', function(){
        	exampleAsset.linkButton.click()
        	AssetPreviewPanel.contentContainer.waitForVisible()
            expect( AssetPreviewPanel.contentContainer.isVisible() ).to.equal(true)
        }) 
        
    })
    
    describe('Edit Asset', function() {
    	
    	it('clicking edit button should open up asset editor panel', function(){
    		AssetPreviewPanel.editButton.link.click()
    		AssetEditorPanel.contentContainer.waitForVisible()
            expect( AssetEditorPanel.contentContainer.isVisible() ).to.equal(true)
        })
        
        it('editing title field', function(){
        	AssetEditorPanel.titleField.waitForVisible() 
    		AssetEditorPanel.titleField.setValue('sample-logo.png')
    		expect( AssetEditorPanel.titleField.getValue() ).to.equal('sample-logo.png')
        })
        
        it('editing description field', function(){
        	AssetEditorPanel.descriptionField.waitForVisible() 
    		AssetEditorPanel.descriptionField.setValue('sample description')
    		expect( AssetEditorPanel.descriptionField.getValue() ).to.equal('sample description')
        })
        
        let exampleTagField
        let exampleOption
        it('editing tag field', function(){
        	exampleTagField = AssetEditorPanel.tagField
        	exampleTagField.selectButton.click()
    		exampleTagField.contentWrapper.waitForVisible()
    		expect( exampleTagField.contentWrapper.isVisible() ).to.equal(true)
    		const options = exampleTagField.items
    		const i = options.findIndex( option => option.text.indexOf('bear') > -1 ) 
    		exampleOption = options[i]
    		expect( exampleOption.text ).to.contain('bear')
    	})
    	
    	it('selecting bear from Tag Field', function(){
    		exampleOption.span.click()
    		//exampleTagField.tags.waitForVisible()
    		const options = exampleTagField.tags
    		const i = options.findIndex( option => option.getText().indexOf('bear') > -1 ) 
    		expect( i ).to.equal(0)
    		
    	})
        
        // save        
        it('saving asset', function(){
        	AssetEditorPanel.save.waitForVisible() 
    		AssetEditorPanel.save.click()
    		AssetPreviewPanel.contentContainer.waitForVisible()
            expect( AssetPreviewPanel.contentContainer.isVisible() ).to.equal(true)
            browser.pause(2000)
        })
        
        let previewContainer
        
        // preview
        it('title field saved', function(){
        	previewContainer = AssetPreviewPanel.contentContainer
        	expect( previewContainer.$('.form-group:nth-child(2) > .field-wrap > .wrapper > p').getText() ).to.equal('sample-logo.png')
        })
        
        it('description field saved', function(){
        	expect( previewContainer.$('.form-group:nth-child(8) > .field-wrap > .wrapper > p').getText() ).to.equal('sample description')
        })
    	
    })
    
    describe('Asset References', function() {
    	
    	let referencePreviewContainer
    	
    	it('reference modal open up', function(){
    		AssetPreviewPanel.referenceButton.link.click()
    		referencePreviewContainer = AssetPreviewPanel.referenceContentContainer
    		expect( referencePreviewContainer.$('.collection-header').getText() ).to.contain('referenced in')
    		
        })
    	
    })
    
})

