module.exports = {
    convert: function($, f) {
        //f.bindPath($)
    	f.addFor($.find('li').first(), 'model.links')
        f.bindAttribute($.find('a').first(), 'href', 'item.link')
        f.mapRichField($.find('a').first(), "item.text")
    }
}