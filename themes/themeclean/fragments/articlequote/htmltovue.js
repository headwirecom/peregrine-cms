module.exports = {
    convert: function($, f) {
    	f.wrap($, 'themeclean-components-block')
        f.bindAttribute($.parent(),'model','model')
        f.addIf($.find('.perIsEditAndEmpty').first(), 'isEditAndEmpty')
        f.mapRichField( $.find('div.text-center'), 'model.text')
        f.addStyle( $.find('hr'), 'width', '`${model.linewidth}%`')
    }
}