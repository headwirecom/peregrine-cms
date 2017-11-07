module.exports = {
    convert: function($, f) {
        //f.bindPath($)
    	f.wrap($, 'themeclean-components-block')
        f.bindAttribute($.parent(),'model','model')
        f.addIf($.find('h2').first(), 'model.showtitle == \'true\'')
        f.mapRichField($.find('h2').first(), "model.title")
        f.addIf($.find('h4').first(), 'model.showsubtitle == \'true\'')
        f.mapRichField($.find('h4').first(), "model.subtitle")
        f.addIf($.find('div.col-md').first(), 'model.showbutton == \'true\'')
        f.addFor($.find('div.col-md>a').first(), 'model.buttons')
        f.bindAttribute($.find('a.btn').first(), 'href', 'item.buttonlink')
        f.mapRichField($.find('a.btn').first(), "item.buttontext")
        f.addStyle($.find('a.btn').first(), 'backgroundColor', 'item.buttoncolor')
        f.addStyle($.find('a.btn').first(), 'borderColor', 'item.buttoncolor')
    }
}