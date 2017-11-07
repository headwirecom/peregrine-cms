module.exports = {
    convert: function($, f) {
        //f.bindPath($)
        f.wrap($, 'themeclean-components-block')
        f.bindAttribute($.parent(),'model','model')
    	f.bindAttribute($.find('a').first(), 'href', 'model.logourl')
    	f.bindAttribute($.find('img').first(), 'src', 'model.logo')
    	f.addStyle($.find('img').first(), 'height', 'parseInt(model.logosize)', 'rem')
    	f.mapRichField($.find('h5').first(), "model.title1")
    	f.mapRichField($.find('p').first(), "model.text1")
    	f.mapRichField($.find('h5').eq(1), "model.title2")
    	f.mapRichField($.find('p').eq(1), "model.text2")
    	f.mapRichField($.find('p').eq(2), "model.copyright")
    }
}