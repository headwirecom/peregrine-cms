module.exports = {
    convert: function($, f) {
        f.bindPath($)
        f.addPlaceholders($)
        f.mapField($.find('h2'), 'title')
        // f.mapField($.find('a'), 'author')
        // f.mapField($.find('p'), 'date')
    }
}