module.exports = {
    convert: function($, f) {
    	f.wrap($, 'themeclean-components-block')
    	f.addIf($.find('.perIsEditAndEmpty').first(), 'isEditAndEmpty')
        f.bindAttribute($.parent(),'model','model')
        
        let a1 =$.find('a').eq(0)
        let a2 =$.find('a').eq(1)
        let prevLinkClass = `{
            'disabled': model.previous === 'unknown',
            'btn-outline-primary': model.previous !== 'unknown'
        }`
    	let nextLinkClass = `{
            'disabled': model.next === 'unknown',
            'btn-outline-primary': model.next !== 'unknown'
        }`
        	
        f.mapField( a1, 'model.prevlabel')
        f.bindAttribute(a1, 'href', f.pathToUrl('model.previous'))
        f.bindAttribute(a1,'class', prevLinkClass, false)
        
        f.mapField(a2, 'model.nextlabel')
        f.bindAttribute(a2, 'href', f.pathToUrl('model.next'))
        f.bindAttribute(a2,'class', nextLinkClass, false)
    }
}