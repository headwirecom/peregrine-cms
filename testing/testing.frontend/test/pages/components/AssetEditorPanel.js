class AssetEditorPanel {
	get titleField() {
		return this.contentContainer.$('#title')
	}
	get descriptionField() {
		return this.contentContainer.$('#description')
	}
	get tagField() {
		return new MultiSelect(this.contentContainer.$('.multiselect'))
	}
	get inputs() {
        return $$('input,textarea')
    }
	get menuContainer() { return $('.explorer-preview-nav')} 
	get contentContainer() { return $('.vue-form-generator')} 
	get buttonContainer() { return $('.explorer-confirm-dialog')}
    get save() {return this.buttonContainer.$('button[title="save page properties"]')}
}

class MultiSelect {
	constructor(container){
        this.container = container;
    }
	get selectButton() { return this.container.$('.multiselect__select')} 
	get contentWrapper() { return this.container.$('.multiselect__content-wrapper')}
	get tags() { return this.container.$$('.multiselect__tag')} 
	get items(){
		const items = this.container.$$('ul > li')
        return items.map(item => {
            return new MultiSelectItem(item);
        })
	}
}

class MultiSelectItem {
    constructor(container){
        this.container = container;
    }
    get text()     { return this.container.$('.multiselect__option > span').getText()}
    get span()    { return this.container.$('.multiselect__option > span')}
}

module.exports = AssetEditorPanel