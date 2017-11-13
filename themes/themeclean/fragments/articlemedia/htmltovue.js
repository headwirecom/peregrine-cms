module.exports = {
    convert: function($, f) {
    	f.wrap($, 'themeclean-components-block')
        f.bindAttribute($.parent(),'model','model')
        f.replace( $.find('div').eq(0), '<themeclean-components-media v-bind:model="model"></themeclean-components-media>')
        f.mapRichField( $.find('div').eq(0), "model.text")
        f.addIf($.find('.perIsEditAndEmpty').first(), 'isEditAndEmpty')
    }
}