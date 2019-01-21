class AssetPreviewPanel {
	get editButton() {
		const i = this.menuButtons.findIndex( item => item.text.indexOf('edit') > -1 ) 
		return this.menuButtons[i]
	}
	get referenceButton() {
		const i = this.menuButtons.findIndex( item => item.text.indexOf('list') > -1 ) 
		return this.menuButtons[i]
	}
	get renameButton() {
		const i = this.menuButtons.findIndex( item => item.title.indexOf('rename asset') > -1 ) 
		return this.menuButtons[i]
	}
	get moveButton() {
		const i = this.menuButtons.findIndex( item => item.title.indexOf('move asset') > -1 ) 
		return this.menuButtons[i]
	}
	get deleteButton() {
		const i = this.menuButtons.findIndex( item => item.title.indexOf('delete asset') > -1 ) 
		return this.menuButtons[i]
	}
	get menuButtons() {
    	const items = this.menuContainer.$$('li > a')
        return items.map(item => {
            return new MenuButton(item);
        })
    }
	
	// pathbrowser modal elements
    get assets() {
        const items = this.pathBrowserContainer.$$('.modal-content > .col-browse > ul > li')
        return items.map(item => {
            return new Item(item);
        })
    }
    get selectedPath() {
    	return this.pathBrowserContainer.$('.modal-footer > .selected-path').getText()
    }
    get selectPathButton() {
    	return this.pathBrowserContainer.$('.modal-footer > button.modal-action:nth-child(3)')
    }
    // pathbrowser modal elements
	
	get pathBrowserContainer() {
    	return $('.modal-container')
    }
	get menuContainer() { return $('.explorer-preview-nav')} 
	get contentContainer() { return $('.vue-form-generator')} 
	get referenceContentContainer() {return $('.preview-asset')} 
	
}

class Item {
    constructor(container){
        this.container = container;
    }
    get text()     { return this.container.$(`span`).getText()}
    get label()    { return this.container.$(`label`)}
}

class MenuButton {
    constructor(container){
        this.container = container;
    }
    get text()     { return this.container.$('i').getText()}
    get link()    { return this.container}
    get title()   { return this.container.getAttribute('title')}
}

module.exports = AssetPreviewPanel