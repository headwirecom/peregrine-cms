class ObjectEditorPanel {
    get inputs() {
        return $$('input,textarea')
    }
    get container() { return $('.vue-form-generator')}
    get buttonContainer() { return $('.explorer-confirm-dialog')}
    get save() {return this.buttonContainer.$('button[title="save object"]')}
}

module.exports = ObjectEditorPanel