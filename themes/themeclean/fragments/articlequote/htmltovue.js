module.exports = {
    convert: function($, f) {
    	f.wrap($, 'themeclean-components-block')
        f.bindAttribute($.parent(),'model','model')
        f.addIf($.find('.perIsEditAndEmpty').first(), 'isEditAndEmpty')

        let contentDiv = $.find('div').first()
        f.mapRichField( contentDiv, 'model.text')
        let borderClasses = `{
            'percms-blockquote': model.blockquote === 'true',
            'border-secondary': model.colorscheme === '',
            'border-dark' : model.colorscheme === 'light',
            'border-light' : model.colorscheme === 'dark'
        }`
        f.bindAttribute( contentDiv, 'class', borderClasses, false)

        let hr1 =$.find('hr').eq(0)
        let hr2 =$.find('hr').eq(1)
        let hrClasses = `{
            'border-secondary': model.colorscheme === '',
            'border-dark' : model.colorscheme === 'light',
            'border-light' : model.colorscheme === 'dark'
        }`
        f.addIf( hr1, "model.blockquote == 'false'")
        f.addIf( hr2, "model.blockquote == 'false'")
        f.bindAttribute( hr1, 'class', hrClasses, false)
        f.bindAttribute( hr2, 'class', hrClasses, false)
        f.addStyle( hr1, 'width', '`${model.linewidth}%`')
        f.addStyle( hr2, 'width', '`${model.linewidth}%`')
    }
}