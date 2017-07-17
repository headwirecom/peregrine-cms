module.exports = {
    convert: function($, f) {
        f.bindPath($)
        f.mapField($.find('.blog-title'), 'title')
        f.mapField($.find('.blog-description'), 'description')
    }
}