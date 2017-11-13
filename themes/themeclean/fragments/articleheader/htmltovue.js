module.exports = {
    convert: function($, f) {
    	f.wrap($, 'themeclean-components-block')
        f.bindAttribute($.parent(),'model','model')
        f.addIf($.find('.perIsEditAndEmpty').first(), 'isEditAndEmpty')
        f.mapField( $.find('h2'), 'model.title')
        f.mapField( $.find('h3'), 'model.subtitle')
        f.addIf( $.find('h2'), 'model.showtitle == \'true\'')
        f.addIf( $.find('h3'), 'model.showsubtitle == \'true\'')
        f.bindAttribute( $ , 'class', 'model.textalign', false)
    }
}