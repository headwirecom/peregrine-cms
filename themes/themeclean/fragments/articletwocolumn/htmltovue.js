module.exports = {
    convert: function($, f) {
        f.wrap($, 'themeclean-components-block')
        f.bindAttribute($.parent(),'model','model')
        f.mapRichField( $.find('div').eq(0), 'model.textleft')
        f.mapRichField( $.find('div').eq(1), 'model.textright')
        f.addIf($.find('.perIsEditAndEmpty').first(), 'isEditAndEmpty')
    }
}