module.exports = {
    convert: function($, f) {
        f.wrap($, 'themeclean-components-block')
        f.bindAttribute($.parent(),'model','model')
        f.replace( $.find('img').first(), '<themeclean-components-media v-bind:model="model"></themeclean-components-media>')
        f.addIf($.find('.perIsEditAndEmpty').first(), 'isEditAndEmpty')
        f.bindAttribute($, 'style', "{flexBasis:`${model.mediawidth}%`}")
    }
}