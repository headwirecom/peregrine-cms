class EditorPanel {
    get inputs() {
        return $$('input,textarea')
    }
    get container() { return $('.editor-panel-content')}
    get save() {return this.container.$('button[title="save"]')}
}

module.exports = EditorPanel