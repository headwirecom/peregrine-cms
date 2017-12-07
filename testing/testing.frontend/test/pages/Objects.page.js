const LoginPage = require('./Login.page');
const Explorer = require('./components/Explorer');
const SubNav = require('./components/SubNav');
const AddObjectWizard = require('./components/AddObjectWizard');

class ObjectsPage extends LoginPage {
    constructor() {
        super();
        this.Explorer = new Explorer();
        this.SubNav = new SubNav();
        this.AddObjectWizard = new AddObjectWizard();
    }

    open() {
        super.open('content/admin/objects.html')
        browser.execute( require('../drag-mock') )
    }
}


module.exports = new ObjectsPage();