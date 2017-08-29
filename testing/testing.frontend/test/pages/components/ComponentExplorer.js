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
        const drag = this.container;
        const dragTarget = target;
        browser.execute(function(drag,dragTarget) {
            jQuery( drag ).simulate("drag-n-drop", { dropTarget: dragTarget })
        }, drag, dragTarget)
    }

    get text() {
        return this.container.getText();
    }

}

module.exports = ComponentExplorer;