class SubNav {
    get container() { return $('.sub-nav') }
    get addFolderButton() {return browser.element('.sub-nav > span:nth-child(2) > a')}
    get addObjectButton() {return browser.element('.sub-nav > span:nth-child(3) > a')}
}

module.exports = SubNav