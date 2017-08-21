module.exports = {
    convert: function($, f) {
        f.bindPath($)
        f.mapField($.find('.btn-outline-primary'), 'previous')
        f.mapField($.find('.btn-outline-secondary'), 'next')
    }
}