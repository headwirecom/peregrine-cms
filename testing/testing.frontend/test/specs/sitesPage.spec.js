let sitesPage = require('../pages/Sites.page')
let {Explorer, Workspace, EditorPanel} = sitesPage
let {ComponentExplorer, ContentView} = Workspace

describe('Peregrine sites page', function () {
    it('should login', function() {
        sitesPage.open()
        expect( browser.getUrl() ).to.contain('content/admin/pages/pages/index.html')
    })

    describe('Site Explorer', function() {
        let exampleSite
        
        it('should have a site titled "example vuejs site"', function(){
        	Explorer.container.waitForVisible()
            const sites = Explorer.sites
            const i = sites.findIndex( site => site.text.indexOf('example vuejs site') > -1 ) 
            exampleSite = sites[i]
            expect( exampleSite.text ).to.contain('example vuejs site')
        })

        it('clicking edit item should load the page editor workspace', function(){
            exampleSite.editButton.click()
            Workspace.container.waitForVisible()
            expect( browser.getUrl() ).to.contain('content/example/pages' )
            expect( Workspace.container.isVisible() ).to.equal(true)
        })

    })

    describe('Page Editor', function() {

        it('should display content view', function() {
            ContentView.container.waitForVisible()
            expect( ContentView.container.isVisible() ).to.equal(true)
        })

        it('should display component explorer', function() {
            ComponentExplorer.container.waitForVisible()
            expect( ComponentExplorer.container.isVisible() ).to.equal(true)
        })

    })

    //Shared components for remaining test suites
    let components, jumbotronDraggable, imageDraggable, rowDraggable, colDraggable
    let imageComponent, jumbotronComponent

    describe('Component Explorer', function() {
        it('should contain components for jumbotron, image, row, col', function() {
            components = ComponentExplorer.components
            
            jumbotronDraggable    = components[2]
            imageDraggable    = components[1]
            rowDraggable          = components[7]
            colDraggable          = components[5]

            expect(jumbotronDraggable.text).to.contain('Jumbotron')
            expect(imageDraggable.text).to.contain('Image')
            expect(rowDraggable.text).to.contain('Row')
            expect(colDraggable.text).to.contain('Col')
        })
    })

    // describe('add 3 new grid components', function() {
    //     it('should see 6 new dropTargets (start/end for each container)', function() {
    //         const dropTargetsBefore = ContentView.dropTargets

    //         containerDraggable.dragToEditView( ContentView.dropTargets[0].center )
    //         containerComponent = ContentView.dropTargets[1]
    //         rowDraggable.dragToEditView( container.center )
    //         colDraggable.dragToEditView( ContentView.dropTargets[2].center )

    //         const dropTargetsAfter = ContentView.dropTargets
    //         const newTargets = dropTargetsAfter.length - dropTargetsBefore.length
    //         //Each new dropTaret  component as a start and an end
    //         expect(newTargets).to.equal(6)
    //     })
    // })

    describe('add a new jumbotron', function() {
        it('should see 1 new jumbotron component', function() {
            const jumbotronsBefore = ContentView.jumbotrons

            jumbotronDraggable.dragToEditView( ContentView.dropTargets[0].center )
            jumbotronComponent = ContentView.jumbotrons[0]
            const jumbotronsAfter = ContentView.jumbotrons
            //console.log('before 1:' + jumbotronsBefore)
            //console.log('after 1:' + jumbotronsAfter)
            const diff = jumbotronsAfter.length - jumbotronsBefore.length
            expect(diff).to.equal(1)
        })
    })

    describe('Edit jumbotron', function() {
        let inputs, buttons
        it('should load the jumbotron editor on click',function(){
            jumbotronComponent.clickAtLocation()
            expect( EditorPanel.container.isVisible() ).to.equal(true)
        })

        it('should set the <h1> of the jumbotron', function() {
            inputs = EditorPanel.inputs
            inputs[0].setValue('Peregrine test Jumbotron')
            expect( jumbotronComponent.selectorText('h1') ).to.equal('Peregrine test Jumbotron')
        })

        it('should set the <p> of the jumbotron', function() {
            inputs[1].setValue('This is greatest Jumbotron ever created')
            expect( jumbotronComponent.selectorText('p') ).to.equal('This is greatest Jumbotron ever created')
        })

        it('should save and hide the editor', function() {
            EditorPanel.save.click()
            EditorPanel.container.waitForExist(3000, true);
            expect( EditorPanel.container.isVisible() ).to.equal(false)
        })
    })

    describe('Delete jumbotron', function() {
        it('should delete the jumbotron', function() {
            const jumbotronsBefore = ContentView.jumbotrons
            jumbotronComponent.clickAtLocation()
            jumbotronComponent.deleteButton.waitForExist(3000);
            jumbotronComponent.deleteButton.click()
            jumbotronComponent.deleteButton.waitForExist(3000, true);
            const jumbotronsAfter = ContentView.jumbotrons
            //console.log('before 2:' + jumbotronsBefore)
            //console.log('after 2:' + jumbotronsAfter)
            const diff = jumbotronsBefore.length - jumbotronsAfter.length
            expect( diff ).to.equal(1)
        })
    })

})