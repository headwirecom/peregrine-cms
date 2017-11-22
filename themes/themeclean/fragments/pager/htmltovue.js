module.exports = {
    convert: function($, f) {
    	f.wrap($, 'themeclean-components-block')
        f.bindAttribute($.parent(),'model','model')
        f.bindAttribute($.find('a').eq(0), 'href', 'model.previous')
        f.bindAttribute($.find('a').eq(1), 'href', 'model.next')

    }
}