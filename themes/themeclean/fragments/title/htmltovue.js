module.exports = {
    convert: function($, f) {
        //f.bindPath($)

        //Make block component
       f.wrap($, 'themeclean-components-block')
       f.bindAttribute($.parent(),'model','model')

        //add text in p tag
        f.mapRichField($.find('h1').first(), "model.title")
    }
}