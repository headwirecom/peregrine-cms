module.exports = {
    convert: function($, f) {
        let a = $.find('a').first()
        f.addFor( a, 'model.icons')
        f.bindAttribute( a, 'href', f.pathToUrl('item.url'))

        let iClasses = `[
            {'text-light': model.colorscheme === 'dark' && model.iconcustomcolor != 'true'},
            {'text-dark': model.colorscheme === 'light' && model.iconcustomcolor != 'true'},
            item.icon.split(':')[1]
        ]`
        let i = $.find('i').first()
        f.mapField( i, "item.icon.split(':')[2]")
        f.bindAttribute( i, 'class', iClasses, false)
        f.addStyle( i, 'font-size', 'model.iconsize', 'px')
        f.addStyle( i, 'color', "model.iconcustomcolor === 'true' ? model.iconcolor : 'inherit'")
    }
}