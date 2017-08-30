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

    dragToEditView(position) {
        browser.execute(function( dragTarget, pos) {
            //In browser context
            var editviewoverlay = document.getElementById('editviewoverlay')
            dragMock
                .dragStart(dragTarget)
                .dragOver(editviewoverlay, {clientX: pos.x, clientY: pos.y})
                .drop(editviewoverlay, {clientX: pos.x, clientY: pos.y})
        }, this.container, position)
    }

    get text() {
        return this.container.getText();
    }

}

module.exports = ComponentExplorer;