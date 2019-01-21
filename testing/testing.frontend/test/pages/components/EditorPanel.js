class EditorPanel {
    get inputs() {
        return $$('input,textarea')
    }
    get container() { return $('.editor-panel-content')}
    get buttonContainer() { return $('.editor-panel-buttons')}
    get save() {return this.buttonContainer.$('button[title="save"]')}
}

module.exports = EditorPanel