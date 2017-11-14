module.exports = {
    convert: function($, f) {
        //f.bindPath($)
        f.addFor($.find('div').eq(0), 'model.icons')
        f.bindAttribute($.find('a').first(), 'href', 'item.url')
        f.mapField($.find('i').first(), "item.icon.split(':')[2]")
        f.bindAttribute($.find('i').first(), 'class', "item.icon.split(':')[1]", false)
        f.addStyle( $.find('i').first(), 'font-size', 'model.iconsize', 'px')
        f.addStyle( $.find('i').first(), 'color', 'model.iconcolor')
    }
}