module.exports = {
    convert: function($, f) {

        let aClasses = `{
            'btn-lg': model.buttonsize === 'large',
            'btn-sm': model.buttonsize === 'small',
            'btn-primary': model.buttoncolor === 'primary',
            'btn-secondary': model.buttoncolor === 'secondary'
        }`

        let a = $.find('a')
    	f.addFor( a, 'model.buttons')
        f.bindAttribute( a, 'href', f.pathToUrl('item.buttonlink'))
        f.bindAttribute( a, 'class', aClasses, false)
        f.mapRichField( a, "item.buttontext")
    }
}