const LoginPage = require('./Login.page');
const Explorer = require('./components/Explorer');
const SubNav = require('./components/SubNav');
const AssetPreviewPanel = require('./components/AssetPreviewPanel');
const AssetEditorPanel = require('./components/AssetEditorPanel');

class AssetsPage extends LoginPage {
    constructor() {
        super();
        this.Explorer = new Explorer();
        this.SubNav = new SubNav();
        this.AssetPreviewPanel = new AssetPreviewPanel();
        this.AssetEditorPanel = new AssetEditorPanel();
    }

    open() {
        super.open('content/admin/pages/assets.html')
    }
}


module.exports = new AssetsPage();