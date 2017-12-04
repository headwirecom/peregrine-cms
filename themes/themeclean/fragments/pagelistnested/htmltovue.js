module.exports = {
    convert: function($, f) {
        //f.bindPath($)
        f.addFor($.find('li.children').first(), 'model.childrenPages', 'child')
        f.bindAttribute($.find('li.children a').first(),'href',f.pathToUrl('child.path'))
        f.mapField($.find('li.children a').first(),'child.title')
        f.replace( $.find('ul.nested').eq(0), '<themeclean-components-pagelistnested v-bind:model="child"></themeclean-components-pagelistnested>')
        f.addIf($.find('themeclean-components-pagelistnested').first(), 'child.hasChildren')
    }
}