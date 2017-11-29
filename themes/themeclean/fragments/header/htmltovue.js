module.exports = {
    convert: function($, f) {
        //f.bindPath($)
    	f.wrap($, 'themeclean-components-block')
        f.bindAttribute($.parent(),'model','model')
        f.addIf($.find('h1').first(), 'editAndEmpty')
        f.addIf($.find('span.navbar-logo a').first(), 'model.logo')
        let navclasses = "{" +
            "'navbar-expand-lg': model.collapsed === 'false'," +
            "'navbar-light': model.colorscheme === 'light'," +
            "'navbar-dark': model.colorscheme === 'dark'" +
        "}"
        f.bindAttribute($, 'class', navclasses, false)

        f.bindAttribute($.find('a').first(), 'href', f.pathToUrl('model.logourl'))
    	f.bindAttribute($.find('img').first(), 'src', f.pathToUrl('model.logo'))
    	f.bindAttribute($.find('img').first(), 'alt', 'model.logoalttext')
        f.addStyle($.find('img').first(), 'height', 'parseInt(model.logosize)', 'px')

    	f.replace( $.find('div.textlink').eq(0), '<themeclean-components-textlinks v-bind:model="model"></themeclean-components-textlinks>')
    	f.replace( $.find('div.menubutton').eq(0), '<themeclean-components-menubuttons v-bind:model="model"></themeclean-components-menubuttons>')
    }
}