class ComponentExplorer {
    get container() { return $('.component-explorer') }
    get components() {
        const items = this.container.$$('.collection-item')
        return items.map( item => {
            return new Component(item);
        })
    }
}

class Component {
    constructor(item) {
        this.container = item;
    }
    dragTo(target) {
        this.container.moveToObject();
        browser.buttonDown( 0 );
        // browser.moveToObject( '#editview') 
        browser.moveTo( 100, 100);
        browser.pause(5000);
        browser.buttonUp( 0 );
    }
    get text() {
        return this.container.getText();
    }
}

module.exports = ComponentExplorer;