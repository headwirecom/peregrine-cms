class AssetPreviewPanel {
	get editButton() {
		const i = this.menuButtons.findIndex( item => item.text.indexOf('edit') > -1 ) 
		return this.menuButtons[i]
	}
	get referenceButton() {
		const i = this.menuButtons.findIndex( item => item.text.indexOf('list') > -1 ) 
		return this.menuButtons[i]
	}
	get menuButtons() {
    	const items = this.menuContainer.$$('li > a')
        return items.map(item => {
            return new MenuButton(item);
        })
    }
	get menuContainer() { return $('.explorer-preview-nav')} 
	get contentContainer() { return $('.vue-form-generator')} 
	get referenceContentContainer() {return $('.preview-asset')} 
	
}

class MenuButton {
    constructor(container){
        this.container = container;
    }
    get text()     { return this.container.$('i').getText()}
    get link()    { return this.container}
}

module.exports = AssetPreviewPanel