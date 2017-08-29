const LoginPage = require('./Login.page');
const Explorer = require('./components/Explorer');
const Workspace = require('./components/Workspace');


class SitesPage extends LoginPage {

    constructor() {
        super();
        this.Explorer = new Explorer();
        this.Workspace = new Workspace();
    }

    open() {
        super.open('content/admin/pages.html')
        browser.execute( require('../drag-mock') )
    }
}


module.exports = new SitesPage();