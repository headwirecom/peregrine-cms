class AssetPreviewPanel {
	get editButton() { return this.menuContainer.$$('li:nth-child(4) > a:nth-child(1)')} 
	get menuContainer() { return $('.explorer-preview-nav')} 
	get contentContainer() { return $('.vue-form-generator')} 
	
}

module.exports = AssetPreviewPanel