module.exports = {
    convert: function($, f) {
        f.wrap($, 'themeclean-components-block')
        f.bindAttribute($.parent(),'model','model')

        let span = $.find('span').first()
        f.addFor( span, 'model.links' )
        f.bindAttribute( span, 'class', "{active: i === model.links.length - 1}", false)

        let anchor = $.find('a')
        f.addIf( anchor, "i + 1 < model.links.length" )
        f.bindAttribute( anchor, 'href', "item.link")
        f.mapField( anchor, "item.text")

        let spanInner = span.find('span')
        f.addIf( spanInner, "i+1 === model.links.length" )
        f.mapField( spanInner, "item.text")

    }
}