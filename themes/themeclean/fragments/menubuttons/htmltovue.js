module.exports = {
    convert: function($, f) {
        //f.bindPath($)
    	f.addFor($.find('a').first(), 'model.buttons')
        f.bindAttribute($.find('a').first(), 'href', 'item.buttonlink')
        f.mapRichField($.find('span').eq(1), "item.buttontext")
    }
}