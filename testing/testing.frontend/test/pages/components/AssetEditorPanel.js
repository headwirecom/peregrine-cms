class AssetEditorPanel {
	get inputs() {
        return $$('input,textarea')
    }
	get menuContainer() { return $('.explorer-preview-nav')} 
	get contentContainer() { return $('.vue-form-generator')} 
}

module.exports = AssetEditorPanel