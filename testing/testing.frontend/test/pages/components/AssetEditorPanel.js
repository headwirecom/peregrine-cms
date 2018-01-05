class AssetEditorPanel {
	get descriptionField() {
		return this.contentContainer.$('#description')
	}
	get titleField() {
		return this.contentContainer.$('#title')
	}
	get tagField() {
		return this.inputs[4]
	}
	get inputs() {
        return $$('input,textarea')
    }
	get menuContainer() { return $('.explorer-preview-nav')} 
	get contentContainer() { return $('.vue-form-generator')} 
	get buttonContainer() { return $('.explorer-confirm-dialog')}
    get save() {return this.buttonContainer.$('button[title="save page properties"]')}
}

module.exports = AssetEditorPanel