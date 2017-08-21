module.exports = {
    convert: function($, f) {
        f.bindPath($)
        f.addChildren($.find('.blog-main'))
        f.addPlaceholders($.find('.blog-main'))
        f.addChildren($.find('.blog-sidebar'))
        f.addPlaceholders($.find('.blog-sidebar'))
    }
}