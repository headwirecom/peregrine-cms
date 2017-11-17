module.exports = {
    convert: function($, f) {
        f.wrap($, 'themeclean-components-block')
        f.bindAttribute($.parent(),'model','model')
        f.addIf($.find('.perIsEditAndEmpty').first(), 'isEditAndEmpty')
        const li = $.find('li')
        f.addFor( li, 'model.listitems' )
        f.mapRichField( li, 'item.text' )
        $.find('ul').first().replaceWith( 
            '<component class="col-12" :is="model.numberedlist === \'true\' ? \'ol\':\'ul\'">' +
              $.find('ul').first().html() +
            '</component>'
        )
    }
}