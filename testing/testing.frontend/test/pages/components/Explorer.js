class Explorer {
    get sites() {
        const items = $$('.explorer-main > ul > li')
        return items.map(item => {
            return new Item(item);
        })
    }
    get objects() {
        const items = $$('.explorer-main > ul > li')
        return items.map(item => {
            return new Item(item);
        })
    }
    get folders() {
        const items = $$('.explorer-main > ul > li')
        return items.map(item => {
            return new Item(item);
        })
    }
    get container() { return $('.explorer-main') }
}

class Item {
    constructor(container){
        this.container = container;
    }
    get text()     { return this.container.$(`span > a`).getText()}
    get editButton()    { return this.container.$(`div > span:nth-child(1) > a`) }
}

module.exports = Explorer