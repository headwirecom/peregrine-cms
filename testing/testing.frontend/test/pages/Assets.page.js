const LoginPage = require('./Login.page');
const Explorer = require('./components/Explorer');
const SubNav = require('./components/SubNav');
const AssetPreviewPanel = require('./components/AssetPreviewPanel');

class AssetsPage extends LoginPage {
    constructor() {
        super();
        this.Explorer = new Explorer();
        this.SubNav = new SubNav();
        this.AssetPreviewPanel = new AssetPreviewPanel();
    }

    open() {
        super.open('content/admin/assets.html')
    }
}


module.exports = new AssetsPage();