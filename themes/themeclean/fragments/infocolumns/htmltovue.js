module.exports = {
    convert: function($, f) {
        //f.bindPath($)
    	f.wrap($, 'themeclean-components-block')
        f.bindAttribute($.parent(),'model','model')
    	f.addIf($.find('h2').first(), 'model.showtitle == \'true\'')
        f.mapRichField($.find('h2').first(), "model.title")
        f.addIf($.find('h4').first(), 'model.showsubtitle == \'true\'')
        f.mapRichField($.find('h4').first(), "model.subtitle")
        f.addIf($.find('p').first(), 'model.showtext == \'true\'')
        f.mapRichField($.find('p').first(), "model.text")
        f.addIf($.find('div.col-12.p-0').eq(1), 'model.showbutton == \'true\'')
        f.addFor($.find('div.col-12>a').first(), 'model.buttons')
        f.bindAttribute($.find('a.btn').first(), 'href', f.pathToUrl('item.buttonlink'))
        f.mapRichField($.find('a.btn').first(), "item.buttontext")
        f.addStyle($.find('a.btn').first(), 'backgroundColor', 'item.buttoncolor')
        f.addStyle($.find('a.btn').first(), 'borderColor', 'item.buttoncolor')
        $.append('<div v-if="isEditAndEmpty">no content defined for component</div>')
    }
}