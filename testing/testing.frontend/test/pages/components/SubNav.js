class SubNav {
    get buttons() {
        const items = $$('.span > a')
        return items.map(item => {
            return new Item(item);
        })
    }
    get container() { return $('.sub-nav') }
}

class Item {
    constructor(container){
        this.container = container;
    }
    get text()     { return this.container.$(`span > a`).getAttribute('title')}
    get button()    { return this.container.$(`span > a`) }
}

module.exports = SubNav