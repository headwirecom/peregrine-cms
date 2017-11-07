module.exports = {
    convert: function($, f) {
        //f.bindPath($)
    	f.wrap($, 'themeclean-components-block')
        f.bindAttribute($.parent(),'model','model')
        f.bindAttribute($, 'class', "{'navbar-expand-lg':model.collapsed === 'false'}", false)
        f.bindAttribute($.find('a').first(), 'href', 'model.logourl')
    	f.bindAttribute($.find('img').first(), 'src', 'model.logo')
    	f.addStyle($.find('img').first(), 'height', 'parseInt(model.logosize)', 'rem')
    	f.replace( $.find('div.textlink').eq(0), '<themeclean-components-textlinks v-bind:model="model"></themeclean-components-textlinks>')
    	f.replace( $.find('div.menubutton').eq(0), '<themeclean-components-menubuttons v-bind:model="model"></themeclean-components-menubuttons>')
    }
}