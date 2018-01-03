const LoginPage = require('./Login.page');
const Explorer = require('./components/Explorer');
const SubNav = require('./components/SubNav');
const AssetPreviewPanel = require('./components/AssetPreviewPanel');
const AssetEditPanel = require('./components/AssetEditPanel');

class AssetsPage extends LoginPage {
    constructor() {
        super();
        this.Explorer = new Explorer();
        this.SubNav = new SubNav();
        this.AssetPreviewPanel = new AssetPreviewPanel();
        this.AssetEditPanel = new AssetEditPanel();
    }

    open() {
        super.open('content/admin/assets.html')
    }
}


module.exports = new AssetsPage();