module.exports = {
    convert: function($, f) {
        //f.bindPath($)
        f.wrap($, 'themeclean-components-block')
		f.bindAttribute($.parent(),'model','model')
		f.addIf($.find('div.col-12').first(), "model.showlogo === 'true'")
    	f.bindAttribute($.find('a').first(), 'href', f.pathToUrl('model.logourl'))
    	f.bindAttribute($.find('img').first(), 'src', f.pathToUrl('model.logo'))
    	f.addStyle($.find('img').first(), 'height', 'parseInt(model.logosize)', 'px')
		f.mapRichField($.find('p').eq(1), "model.copyright")
		f.addFor( $.find('div.col-12').eq(1), "model.columns")
		f.mapRichField($.find('div.col-12').eq(1), "item.text")
    }
}