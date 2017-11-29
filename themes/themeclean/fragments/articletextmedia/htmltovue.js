module.exports = {
    convert: function($, f) {
    	f.wrap($, 'themeclean-components-block')
        f.bindAttribute($.parent(),'model','model')
        f.bindAttribute($.find('div').first(), 'style', "{flexBasis: `${model.mediawidth}%`}")
        f.bindAttribute($,  'class', "model.mediaposition === 'after' ? 'flex-row-reverse': 'flex-row'", false)
        f.replace( $.find('div.col-md-8').eq(0), '<themeclean-components-media v-bind:model="model"></themeclean-components-media>')
        f.mapRichField( $.find('div').eq(1), "model.text")
        f.addIf($.find('.perIsEditAndEmpty').first(), 'isEditAndEmpty')
    }
}