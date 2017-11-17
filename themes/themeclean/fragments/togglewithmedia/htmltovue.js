module.exports = {
    convert: function($, f) {
        //f.bindPath($)
    	f.wrap($, 'themeclean-components-block')
        f.bindAttribute($.parent(),'model','model')
    	f.addIf($.find('h2').first(), 'model.showtitle == \'true\'')
        f.mapRichField($.find('h2').first(), "model.title")
        f.addFor($.find('div.card').first(), 'model.toggles')
        
        f.bindAttribute($.find('a').first(), 'data-parent', '`#accordion${_uid}`')
        f.bindAttribute($.find('a').first(), 'href', '`#accordion${_uid}${i}`')
        f.bindAttribute($.find('a').first(), 'aria-controls', '`accordion${_uid}${i}`')
        f.mapRichField($.find('a').first(), "item.title")
        
        f.bindAttribute($.find('div.collapse').first(), 'id', '`accordion${_uid}${i}`')
        f.mapRichField($.find('div.card-body').first(), "item.text")
        f.addIf($.find('.perIsEditAndEmpty').first(), 'isEditAndEmpty')
    }
}