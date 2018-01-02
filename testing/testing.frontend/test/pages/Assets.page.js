const LoginPage = require('./Login.page');
const Explorer = require('./components/Explorer');
const SubNav = require('./components/SubNav');

class AssetsPage extends LoginPage {
    constructor() {
        super();
        this.Explorer = new Explorer();
        this.SubNav = new SubNav();
    }

    open() {
        super.open('content/admin/assets.html')
    }
}


module.exports = new ObjectsPage();