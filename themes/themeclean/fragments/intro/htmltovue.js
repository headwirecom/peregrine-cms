module.exports = {
    convert: function($, f) {
        //f.bindPath($)
    	f.wrap($, 'themeclean-components-block')
        f.bindAttribute($.parent(),'model','model')
    	f.addIf($.find('h1').first(), 'model.showtitle == \'true\'')
        f.mapRichField($.find('h1').first(), "model.title")
        f.addIf($.find('h3').first(), 'model.showsubtitle == \'true\'')
        f.mapRichField($.find('h3').first(), "model.subtitle")
        f.addIf($.find('p').first(), 'model.showtext == \'true\'')
        f.mapRichField($.find('p').first(), "model.text")
        
        f.addIf($.find('div.d-flex').first(), 'model.showbutton == \'true\'')
        f.addFor($.find('div.d-flex>a').first(), 'model.buttons')
        f.bindAttribute($.find('a.btn').first(), 'href', 'item.buttonlink')
        f.mapRichField($.find('a.btn').first(), "item.buttontext")
        f.addStyle($.find('a.btn').first(), 'backgroundColor', 'item.buttoncolor')
        f.addStyle($.find('a.btn').first(), 'borderColor', 'item.buttoncolor')
    }
}