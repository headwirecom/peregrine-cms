var dragAndDrop = require('html-dnd').code;
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
        browser.execute(function( dragTarget, dropTarget) {
            //In browser context
            var dropTheBass = document.getElementById('editable');
            dragMock.dragStart(dragTarget).drop(document, {clientX: 10, clientY: 400})
        }, this.container, target)
    }

    get text() {
        return this.container.getText();
    }

}

module.exports = ComponentExplorer;