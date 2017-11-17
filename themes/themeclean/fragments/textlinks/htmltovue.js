module.exports = {
    convert: function($, f) {
        //f.bindPath($)
    	f.addFor($.find('li').first(), 'model.links')
        f.bindAttribute($.find('a').first(), 'href', f.pathToUrl('item.link'))
        f.mapRichField($.find('a').first(), "item.text")
        f.bindAttribute($.find('a').first(), 'class', "model.colorscheme === 'light' ? 'text-dark':'text-light'", false)
    }
}