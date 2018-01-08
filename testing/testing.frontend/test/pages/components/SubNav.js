class SubNav {
    get container() { return $('.sub-nav') }
    get addFolderButton() {return browser.element('[title="add folder"]')}
    get addObjectButton() {return browser.element('[title="add object"]')}
    get fileUploadButton() {return browser.element('[title="file upload"]')}
    get fileInputField() {return this.fileUploadButton.$('input')}	
}

module.exports = SubNav