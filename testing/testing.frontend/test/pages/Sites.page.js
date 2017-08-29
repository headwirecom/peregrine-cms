const LoginPage = require('./Login.page');
const Explorer = require('./components/Explorer');
const Workspace = require('./components/Workspace');
const jqSimulate = require('../tools/jq-simulate-custom');


class SitesPage extends LoginPage {

    constructor() {
        super();
        this.Explorer = new Explorer();
        this.Workspace = new Workspace();
    }

    open() {
        super.open('content/admin/pages.html')
    }
}


module.exports = new SitesPage();