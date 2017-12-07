class SubNav {
    get container() { return $('.sub-nav') }
    get addFolderButton() {return browser.element('[title="add folder"]')}
    get addObjectButton() {return browser.element('[title="add object"]')}
}

module.exports = SubNav