class ContentView {
    get container() {return $('.peregrine-content-view')}
    get editable() {return $('#editable')}

    get dropTargets() {
        browser.frame(0)
        let targets = $$('[data-per-droptarget="true"]')
        browser.frame(null)
        return targets.map( target => new Component(target) )
    }

    get dataComponents() {
        browser.frame(0)
        let targets = $$('[data-per-path]')
        browser.frame(null)
        return targets.map( target => new Component(target) )
    }

    get jumbotrons() {
        browser.frame(0)
        let targets = $$('[class="jumbotron"]')
        browser.frame(null)
        return targets.map( target => new Component(target) )
    }

    get perApp() {
        browser.frame(0);
        let app = $('.peregrine-app')
        browser.frame(null);
        return app;
    } 

}

//A component within the editview frame
class Component {
    constructor(item) {
        this.container = item
    }

    clickAtLocation() {
        browser.frame(0)
        const location = this.container.getLocation()
        browser.frame(null)
        browser.leftClick('.editview-container', location.x + 5, location.y + 5)
    }

    selectorText( selector ) {
        browser.frame(0)
        const text = this.container.$(selector).getText()
        browser.frame(null)
        return text
    }

    get position() {
        browser.frame(0)
        const location = this.container.getLocation()
        browser.frame(null)
        const editPos = $('.editview-container').getLocation()
        return { x: editPos.x + location.x, y: editPos.y + location.y }
    }
    
    get center() {
        let location = this.position
        browser.frame(0)
        const width = this.container.getElementSize('width')
        const height = this.container.getElementSize('height')
        browser.frame(null)
        return {
            x: location.x + width/2,
            y: location.y + height/2
        }
    }

    get text() {
        browser.frame(0)
        const text = this.container.getText();
        browser.frame(null)
        return text
    }

    get deleteButton() { return $('#editable > div > ul > li:nth-child(2) > a') }

}

module.exports = ContentView;