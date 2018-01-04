class AssetEditorPanel {
	get descriptionField() {
		return this.inputs[5]
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
}

module.exports = AssetEditorPanel