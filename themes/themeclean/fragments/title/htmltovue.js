module.exports = {
    convert: function($, f) {
       f.wrap($, 'themeclean-components-block')
       f.bindAttribute($.parent(),'model','model')

        f.mapRichField($.find('h1').first(), "model.title")
    }
}