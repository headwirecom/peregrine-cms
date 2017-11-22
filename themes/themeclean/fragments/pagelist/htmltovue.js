module.exports = {
    convert: function($, f) {
        f.wrap($, 'themeclean-components-block')
        f.bindAttribute($.parent(),'model','model')
        f.addIf($.find('li.root').first(), 'model.includeroot')
        f.bindAttribute($.find('li.root a').first(),'href','model.rootpage')
        f.mapField($.find('li.root a').first(),'model.rootpageTitle')
    }
}