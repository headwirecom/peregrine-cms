const LoginPage = require('./Login.page');
const Explorer = require('./components/Explorer');
const Workspace = require('./components/Workspace');
const EditorPanel = require('./components/EditorPanel');


class SitesPage extends LoginPage {

    constructor() {
        super();
        this.Explorer = new Explorer();
        this.Workspace = new Workspace();
        this.EditorPanel = new EditorPanel();
    }

    open() {
        super.open('content/admin/pages/pages.html')
        browser.execute( require('../drag-mock') )
    }
}


module.exports = new SitesPage();