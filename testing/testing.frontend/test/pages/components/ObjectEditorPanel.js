class ObjectEditorPanel {
    get inputs() {
        return $$('input,textarea')
    }
    get textEditor() {
    	return $('.trumbowyg-editor')
    }
    get pathBrowserButtons() {
    	return this.container.$$('.btn-flat')
    }
    get checkboxLabel() {
        return this.container.$('.field-wrap label[for="checkbox"]')
    }
    get checkboxInput() {
    	return this.container.$('.field-material-checkbox input').getValue()
    }
    get radioButtons() {
    	const items = this.container.$$('.radio-list > li')
        return items.map(item => {
            return new RadioButton(item);
        })
    }
    get switchLabel() {
    	return this.container.$('.field-wrap label[for="material-switch"]')
    }
    get rangeInput() {
    	return this.container.$('.range-field input')
    }        
    // pathbrowser modal elements
    get sites() {
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
    get previewContainer() { return $('.vfg-preview') }
    get container() { return $('.vue-form-generator')}
    get buttonContainer() { return $('.explorer-confirm-dialog')}
    get save() {return this.buttonContainer.$('button[title="save object"]')}
}

class Item {
    constructor(container){
        this.container = container;
    }
    get text()     { return this.container.$(`span`).getText()}
    get label()    { return this.container.$(`label`)}
}

class RadioButton {
    constructor(container){
        this.container = container;
    }
    get text()     { return this.container.$(`input`).getValue()}
    get label()    { return this.container.$(`label`)}
}

module.exports = ObjectEditorPanel