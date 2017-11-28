module.exports = {
    convert: function($, f) {
    	f.wrap($, 'themeclean-components-block')
        f.bindAttribute($.parent(),'model','model')
        f.addIf($.find('.perIsEditAndEmpty').first(), 'isEditAndEmpty')

        f.mapRichField( $.find('div.text-center'), 'model.text')

        let hr1 =$.find('hr').eq(0)
        let hr2 =$.find('hr').eq(1)
        let hrClasses = `{
            'border-dark' : model.colorscheme === 'light',
            'border-light' : model.colorscheme === 'dark'
        }`
        f.bindAttribute( hr1, 'class', hrClasses, false)
        f.bindAttribute( hr2, 'class', hrClasses, false)
        f.addStyle( hr1, 'width', '`${model.linewidth}%`')
        f.addStyle( hr2, 'width', '`${model.linewidth}%`')
    }
}