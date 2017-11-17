module.exports = {
    convert: function($, f) {
        //f.bindPath($)
    	f.addFor($.find('a').first(), 'model.buttons')
        f.bindAttribute($.find('a').first(), 'href', f.pathToUrl('item.buttonlink'))
        f.mapRichField($.find('span').eq(1), "item.buttontext")
        f.addStyle($.find('a.btn').first(), 'backgroundColor', 'item.buttoncolor')
        f.addStyle($.find('a.btn').first(), 'borderColor', 'item.buttoncolor')
    }
}