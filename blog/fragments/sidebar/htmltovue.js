module.exports = {
    convert: function($, f) {
        f.bindPath($)
        f.mapField($.find('h4'), 'title')
        f.mapRichField($.find('p'), 'text')
        f.bindAttribute($, 'class', 'style')
    }
}