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
        
    })
    
})

