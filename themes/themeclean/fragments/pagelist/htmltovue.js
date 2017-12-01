module.exports = {
    convert: function($, f) {
        f.wrap($, 'themeclean-components-block')
        f.bindAttribute($.parent(),'model','model')
        f.addIf($.find('li.root').first(), 'model.includeroot === \'true\'')
        f.bindAttribute($.find('li.root a').first(),'href','model.rootPageLink')
        f.mapField($.find('li.root a').first(),'model.rootPageTitle')
        f.addFor($.find('li.children').first(), 'model.childrenPages', 'child')
        f.bindAttribute($.find('li.children a').first(),'href','child.path')
        f.mapField($.find('li.children a').first(),'child.title')

        f.replace( $.find('ul.nested').eq(0), '<themeclean-components-pagelistnested v-bind:model="child"></themeclean-components-pagelistnested>')
    }
}