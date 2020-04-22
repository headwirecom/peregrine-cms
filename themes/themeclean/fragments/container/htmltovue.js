module.exports = {
    convert: function($, f) {
        f.bindPath($)
        f.addChildren($)
        f.addPlaceholders($)
        // f.addFor($.find('div.container div').first(), 'model.children', 'child', false)
        // f.bindAttribute($.find('component').first(), 'is', 'child.component')
        // f.bindAttribute($.find('component').first(), 'model', 'child')
        // f.bindAttribute($.find('pagerendervue-components-placeholder').first(), 'model', '{ path: model.path, component: model.component, location: \'before\' }')
        // f.bindAttribute($.find('pagerendervue-components-placeholder').last(), 'model', '{ path: model.path, component: model.component, location: \'after\'  }')
    }
}