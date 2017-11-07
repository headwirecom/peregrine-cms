module.exports = {
    convert: function($, f) {
    	f.wrap($, 'themeclean-components-block')
        f.bindAttribute($.parent(),'model','model')
        f.replace( $.find('img'), '<themeclean-components-media :model="model"></themeclean-components-media>')
    }
}