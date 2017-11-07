module.exports = {
    convert: function($, f) {
    	f.wrap($, 'themeclean-components-block')
        f.bindAttribute($.parent(),'model','model')
        f.addIf($.find('h1').first(), 'editAndEmpty')
        
        f.addFor($.find('div.card').first(), 'model.cards')
        f.bindAttribute($.find('img').first(), 'src', 'item.image')
        f.addIf($.find('h5').first(), 'model.showtitle == \'true\'')
        f.mapRichField($.find('h5').first(), "item.title")
        f.addIf($.find('p').first(), 'model.showtext == \'true\'')
        f.mapRichField($.find('p').first(), "item.text")
        f.addIf($.find('a.btn').first(), 'model.showbutton == \'true\'')
        f.bindAttribute($.find('a.btn').first(), 'href', 'item.buttonlink')
        f.mapRichField($.find('a.btn').first(), "item.buttontext")
        f.addStyle($.find('a.btn').first(), 'backgroundColor', 'item.buttoncolor')
        f.addStyle($.find('a.btn').first(), 'borderColor', 'item.buttoncolor')
    }
}