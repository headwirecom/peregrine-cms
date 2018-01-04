class AssetEditorPanel {
	get descriptionField() {
		return this.contentContainer.$('#description')
	}
	get tagField() {
		return this.inputs[4]
	}
	get titleField() {
		return this.inputs[1]
	}
	get inputs() {
        return $$('input,textarea')
    }
	get menuContainer() { return $('.explorer-preview-nav')} 
	get contentContainer() { return $('.vue-form-generator')} 
	get buttonContainer() { return $('.explorer-confirm-dialog')}
    get save() {return this.buttonContainer.$('button[title="save object"]')}
}

module.exports = AssetEditorPanel