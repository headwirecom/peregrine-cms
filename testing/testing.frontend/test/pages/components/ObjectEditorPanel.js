class ObjectEditorPanel {
    get inputs() {
        return $$('input,textarea')
    }
    get textEditor() {
    	return $('.trumbowyg-editor')
    }
    get pathBrowserButtons() {
    	return $$('btn-flat')
    }
    get pathBrowserContainer() {
    	return $('.modal-container')
    }
    get previewContainer() { return $('.vfg-preview') }
    get container() { return $('.vue-form-generator')}
    get buttonContainer() { return $('.explorer-confirm-dialog')}
    get save() {return this.buttonContainer.$('button[title="save object"]')}
}

module.exports = ObjectEditorPanel