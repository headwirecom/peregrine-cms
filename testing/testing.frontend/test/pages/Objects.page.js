const LoginPage = require('./Login.page');
const Explorer = require('./components/Explorer');
const SubNav = require('./components/SubNav');


class ObjectsPage extends LoginPage {
    

    constructor() {
        super();
        this.Explorer = new Explorer();
        this.SubNav = new SubNav();
    }

    open() {
        super.open('content/admin/objects.html')
        browser.execute( require('../drag-mock') )
    }
}


module.exports = new ObjectsPage();