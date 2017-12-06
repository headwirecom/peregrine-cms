const LoginPage = require('./Login.page');
const Explorer = require('./components/Explorer');
const Workspace = require('./components/Workspace');
const EditorPanel = require('./components/EditorPanel');


class ObjectsPage extends LoginPage {

    constructor() {
        super();
        this.Explorer = new Explorer();
        this.Workspace = new Workspace();
        this.EditorPanel = new EditorPanel();
    }

    open() {
        super.open('content/admin/objects.html')
        browser.execute( require('../drag-mock') )
    }
}


module.exports = new ObjectsPage();