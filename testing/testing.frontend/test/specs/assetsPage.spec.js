let assetsPage = require('../pages/Assets.page')
let {Explorer, SubNav, AssetPreviewPanel, AssetEditorPanel} = assetsPage

describe('Peregrine assets page', function () {
    it('should login', function() {
    	assetsPage.open()
        expect( browser.getUrl() ).to.contain('content/admin/pages/assets.html')
    })

    describe('Assets Explorer', function() {
        let exampleFolder
        let folderName = 'example'
        it('should have a folder titled "example"', function(){
        	Explorer.container.waitForVisible()
            const folders = Explorer.folders
            const i = folders.findIndex( folder => folder.text.indexOf(folderName) > -1 ) 
            exampleFolder = folders[i]
            expect( exampleFolder.text ).to.contain(folderName)
        })
        
    })  
    
    describe('Asset Uploading', function() {
    	// we need that file existing
    	let localAssetPath = 'C:\\downloaded\\peregrine-logo.png'
    	it('uploading sample asset to system', function(){
    		browser.waitForExist('.nav-content > span:nth-child(3) > a:nth-child(1) > label:nth-child(1) > input:nth-child(1)');
            browser.chooseFile('.nav-content > span:nth-child(3) > a:nth-child(1) > label:nth-child(1) > input:nth-child(1)', localAssetPath)
        })
    })
    
    describe('Asset Editing', function() {
    	
    	let exampleAsset
    	let assetName = 'peregrine-logo.png'
        
        it('should have an asset titled "peregrine-logo.png"', function(){
        	Explorer.container.waitForVisible()
            const assets = Explorer.assets
            const i = assets.findIndex( asset => asset.text.indexOf(assetName) > -1 ) 
            exampleAsset = assets[i]
            expect( exampleAsset.text ).to.contain(assetName)
        })
        
        it('clicking asset item should open up asset preview panel', function(){
        	exampleAsset.linkButton.click()
        	AssetPreviewPanel.contentContainer.waitForVisible()
            expect( AssetPreviewPanel.contentContainer.isVisible() ).to.equal(true)
        }) 
    	
    	it('clicking edit button should open up asset editor panel', function(){
    		AssetPreviewPanel.editButton.link.click()
    		AssetEditorPanel.contentContainer.waitForVisible()
            expect( AssetEditorPanel.contentContainer.isVisible() ).to.equal(true)
        })
        
        let title = 'sample-logo.png'
        it('editing title field', function(){
        	AssetEditorPanel.titleField.waitForVisible() 
    		AssetEditorPanel.titleField.setValue(title)
    		expect( AssetEditorPanel.titleField.getValue() ).to.equal(title)
        })
        
        let description = 'sample description'
        it('editing description field', function(){
        	AssetEditorPanel.descriptionField.waitForVisible() 
    		AssetEditorPanel.descriptionField.setValue(description)
    		expect( AssetEditorPanel.descriptionField.getValue() ).to.equal(description)
        })
        
        let exampleTagField
        let exampleOption
        let tag = 'bear'
        it('editing tag field', function(){
        	exampleTagField = AssetEditorPanel.tagField
        	exampleTagField.selectButton.click()
    		exampleTagField.contentWrapper.waitForVisible()
    		expect( exampleTagField.contentWrapper.isVisible() ).to.equal(true)
    		const options = exampleTagField.items
    		const i = options.findIndex( option => option.text.indexOf(tag) > -1 ) 
    		exampleOption = options[i]
    		expect( exampleOption.text ).to.contain(tag)
    	})
    	
    	it('selecting bear from Tag Field', function(){
    		exampleOption.span.click()
    		//exampleTagField.tags.waitForVisible()
    		const options = exampleTagField.tags
    		const i = options.findIndex( option => option.getText().indexOf(tag) > -1 ) 
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
        	expect( previewContainer.$('.form-group:nth-child(2) > .field-wrap > .wrapper > p').getText() ).to.equal(title)
        })
        
        it('description field saved', function(){
        	expect( previewContainer.$('.form-group:nth-child(8) > .field-wrap > .wrapper > p').getText() ).to.equal(description)
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
    
    describe('Asset Renaming, Moving and Deleting', function() {
    	
    	let assetNewName = 'logo.png'
    		
    	it('rename dialog open up', function(){
    		AssetPreviewPanel.renameButton.link.click()
    		browser.pause(1000)
    		browser.alertText(assetNewName)
    		browser.alertAccept()
    	})
    	
    	let exampleAsset
    	
    	it('should have an asset titled "logo.png"', function(){
    		Explorer.container.waitForVisible()
            const assets = Explorer.assets
            const i = assets.findIndex( asset => asset.text.indexOf(assetNewName) > -1 ) 
            exampleAsset = assets[i]
            expect( exampleAsset.text ).to.contain(assetNewName)
        })
    	
    	it('clicking asset item should open up asset preview panel', function(){
        	exampleAsset.linkButton.click()
        	browser.pause(1000)
        }) 
        
        let pathBrowserContainer
        it('clicking move button should open up path browser modal', function(){
        	pathBrowserContainer = AssetPreviewPanel.pathBrowserContainer
        	AssetPreviewPanel.moveButton.link.click()
        	pathBrowserContainer.waitForVisible()
    		expect( pathBrowserContainer.isVisible() ).to.equal(true)
        }) 
    	
    	let exampleFolder
    	let folderName = 'example'
    	it('should have an asset folder named example in path browser', function(){
    		const assets = AssetPreviewPanel.assets
            const i = assets.findIndex( asset => asset.text.indexOf(folderName) > -1 ) 
            exampleFolder = assets[i]
            expect( exampleFolder.text ).to.contain(folderName)
    	})
    	
    	let folderPath = '/content/example/assets'
    	it('select folder: example', function(){
    		exampleFolder.label.click()
    		expect( AssetPreviewPanel.selectedPath ).to.contain(folderPath)
    		AssetPreviewPanel.selectPathButton.click()
    		// wait for 1 second till the animation ends
    		browser.pause(1000)
    	})
    	
    	let newFolder
    	let newAsset
        it('clicking example folder', function(){
        	Explorer.container.waitForVisible()
            const folders = Explorer.folders
            const i = folders.findIndex( folder => folder.text.indexOf(folderName) > -1 ) 
            newFolder = folders[i]
            expect( newFolder.text ).to.contain(folderName)
        })
    	
    	it('asset logo.png should have been moved to this new location', function(){
        	newFolder.linkButton.click()
            const assets = Explorer.assets
            const i = assets.findIndex( asset => asset.text.indexOf(assetNewName) > -1 ) 
            newAsset = assets[i]
            expect( newAsset.text ).to.contain(assetNewName)
        })
        
        it('clicking asset item should open up asset preview panel', function(){
        	newAsset.linkButton.click()
        	AssetPreviewPanel.contentContainer.waitForVisible()
            expect( AssetPreviewPanel.contentContainer.isVisible() ).to.equal(true)
        }) 
                
        it('clicking delete button should delete asset', function(){
        	AssetPreviewPanel.deleteButton.link.click()
        }) 
        
        it('shouldn\'t have an asset titled "logo.png"', function(){
        	Explorer.container.waitForVisible()
            const assets = Explorer.assets
            const i = assets.findIndex( asset => asset.text.indexOf(assetNewName) > -1 ) 
            //console.log('i is: ' + i)
            expect( i ).to.equal(-1)
        }) 
    	
    })
    
        
})

