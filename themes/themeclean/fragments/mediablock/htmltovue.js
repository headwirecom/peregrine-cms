module.exports = {
    convert: function($, f) {
        f.wrap($, 'themeclean-components-block')
        f.bindAttribute($.parent(),'model','model')
        f.replace( $, '<div class="perIsEditAndEmpty" v-if="isEditAndEmpty">no content defined for component</div><themeclean-components-media v-bind:model="model"></themeclean-components-media>')
    }
}