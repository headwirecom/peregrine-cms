module.exports = {
    convert: function($, f) {
    	f.wrap($, 'themeclean-components-block')
        f.bindAttribute($.parent(),'model','model')
        f.addFor( $.find('div').first(), 'model.tabs')
        f.mapField($.find('h2').first(), 'model.title')
        f.addIf( $.find('h2').first(), 'model.showtitle == \'true\'')
        f.mapField($.find('a').first(), 'item.title')
        f.mapRichField($.find('div.card-body').first(), 'item.text')
        f.bindAttribute($, 'id', '`accordion${_uid}`')
        f.bindAttribute($.find('a').first(), 'data-parent', '`#accordion${_uid}`')
        f.bindAttribute($.find('a').first(), 'href', '`#accordion${_uid}${i}`')
        f.bindAttribute($.find('a').first(), 'aria-controls', '`accordion${_uid}${i}`')
        f.bindAttribute($.find('div.collapse').first(), 'id', '`accordion${_uid}${i}`')
    }
}