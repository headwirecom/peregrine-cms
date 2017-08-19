module.exports = {
    convert: function($, f) {
        f.bindPath($)
        f.addChildren($.find('.usa-layout-docs-main_content'))
        f.addPlaceholders($.find('.usa-layout-docs-main_content'))
    }
}