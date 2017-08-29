let sitesPage = require('../pages/Sites.page');
let {Explorer, Workspace} = sitesPage;
let {ComponentExplorer, ContentView} = Workspace;

describe('Peregrine sites page', function () {
    it('should login', function() {
        sitesPage.open()
        expect( browser.getUrl() ).to.contain('content/admin/pages.html')
    })

    describe('Site Explorer', function() {
        let exampleSite;
        it('should display a list with 2 sites', function(){
            Explorer.container.waitForVisible();
            expect( Explorer.sites.length ).to.equal(2)
        })

        it('should have a site titled "example vuejs site"', function(){
            const sites = Explorer.sites
            const i = sites.findIndex( site => site.text.indexOf('example vuejs site') > -1 ) 
            exampleSite = sites[i]
            expect( exampleSite.text ).to.contain('example vuejs site')
        })

        it('clicking edit item should load the page editor workspace', function(){
            exampleSite.editButton.click();
            Workspace.container.waitForVisible();
            expect( browser.getUrl() ).to.contain('content/sites/example' )
            expect( Workspace.container.isVisible() ).to.equal(true)
        })

    })

    describe('Page Editor', function() {

        it('should display content view', function() {
            ContentView.container.waitForVisible();
            expect( ContentView.container.isVisible() ).to.equal(true)
        })

        it('should display component explorer', function() {
            ComponentExplorer.container.waitForVisible();
            expect( ComponentExplorer.container.isVisible() ).to.equal(true)
        })

    })

    describe('Component Explorer', function() {

        it('should display 9 components in the explorer', function() {
            expect( ComponentExplorer.components.length ).to.equal(9)
        })

        describe('drag n drop', function(){
            it('should add a new component to the content view', function() {
                ComponentExplorer.components[5].dragTo('#editview');
            })
        })

    })

    describe('Content View', function() {

        // it('peregrine app should be visible', function() {
        //     ContentView.perApp.waitForVisible()
        //     expect( ContentView.perApp.isVisible() ).to.equal(true)
        // })

    })

})