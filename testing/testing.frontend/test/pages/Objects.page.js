const LoginPage = require('./Login.page');
const Explorer = require('./components/Explorer');
const SubNav = require('./components/SubNav');
const AddObjectWizard = require('./components/AddObjectWizard');
const ObjectEditorPanel = require('./components/ObjectEditorPanel');

class ObjectsPage extends LoginPage {
    constructor() {
        super();
        this.Explorer = new Explorer();
        this.SubNav = new SubNav();
        this.AddObjectWizard = new AddObjectWizard();
        this.ObjectEditorPanel = new ObjectEditorPanel();
    }

    open() {
        super.open('content/admin/pages/objects.html')
    }
}


module.exports = new ObjectsPage();