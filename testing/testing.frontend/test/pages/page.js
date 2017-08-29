class Page {
    constructor() {
    }
    open(path) {
        browser.url('/' + path);
    }
}
module.exports = Page;