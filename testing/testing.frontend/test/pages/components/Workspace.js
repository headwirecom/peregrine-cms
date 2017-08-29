const ContentView = require('./ContentView')
const ComponentExplorer = require('./ComponentExplorer')

class EditorWorkspace {
    constructor() {
        this.ContentView = new ContentView();
        this.ComponentExplorer = new ComponentExplorer();
    }
    get container() { return $('.peregrine-workspace') };
}

module.exports = EditorWorkspace