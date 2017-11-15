module.exports = {
    convert: function($, f) {
        //f.bindPath($)
        f.mapRichField($.find('h5').first(), "model.title")
        f.addFor($.find('a').first(), 'model.links')
        f.bindAttribute($.find('a').first(), 'href', f.pathToUrl('item.link'))
        f.mapRichField($.find('a').first(), "item.text")
    }
}