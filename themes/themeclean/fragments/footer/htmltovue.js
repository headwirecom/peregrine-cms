module.exports = {
    convert: function($, f) {
        f.wrap($, 'themeclean-components-block')
		f.bindAttribute($.parent(),'model','model')

		//Logo
		f.addIf($.find('div.col-12').first(), "model.showlogo === 'true' && model.logo")
    	f.bindAttribute($.find('a').first(), 'href', f.pathToUrl('model.logourl'))
    	f.bindAttribute($.find('img').first(), 'src', f.pathToUrl('model.logo'))
    	f.bindAttribute($.find('img').first(), 'alt', 'model.logoalttext')
		f.addStyle($.find('img').first(), 'height', 'parseInt(model.logosize)', 'px')

		//Columns
		let col = $.find('div.col-12').eq(1)
		let coltitle = col.find('h5').first()
		let colcontent = col.find('div').first()
		f.addFor( col, "model.columns")
		f.addIf( coltitle, "item.title !== ''")
		f.mapField( coltitle, "item.title")
		f.addIf( colcontent, "item.text !== ''")
		f.mapRichField( colcontent, "item.text")

		//Copyright
		f.mapRichField($.find('p').eq(0), "model.copyright")

    }
}