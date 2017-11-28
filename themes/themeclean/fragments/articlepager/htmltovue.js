module.exports = {
    convert: function($, f) {
    	f.wrap($, 'themeclean-components-block')
    	f.addIf($.find('.perIsEditAndEmpty').first(), 'isEditAndEmpty')
        f.bindAttribute($.parent(),'model','model')
        f.addIf($.find('a').eq(0), 'model.previous == \'unknown\'')
        f.mapField( $.find('a').eq(0), 'model.prevlabel')
        f.addIf($.find('a').eq(1), 'model.previous != \'unknown\'')
        f.bindAttribute($.find('a').eq(1), 'href', f.pathToUrl('model.previous'))
        f.mapField( $.find('a').eq(1), 'model.prevlabel')
        f.addIf($.find('a').eq(2), 'model.next == \'unknown\'')
        f.mapField( $.find('a').eq(2), 'model.nextlabel')
        f.addIf($.find('a').eq(3), 'model.next != \'unknown\'')
        f.bindAttribute($.find('a').eq(3), 'href', f.pathToUrl('model.next'))
        f.mapField( $.find('a').eq(3), 'model.nextlabel')
    }
}