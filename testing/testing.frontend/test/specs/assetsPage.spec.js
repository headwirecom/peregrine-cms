let assetsPage = require('../pages/Assets.page')
let {Explorer, SubNav} = assetsPage

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
        
    })
    
})

