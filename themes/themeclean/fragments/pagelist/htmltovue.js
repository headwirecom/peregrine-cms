module.exports = {
    convert: function($, f) {
        f.wrap($, 'themeclean-components-block')
        f.bindAttribute($.parent(),'model','model')
        f.addIf($.find('.perIsEditAndEmpty').first(), 'isEditAndEmpty')
        f.addIf($.find('ul.root').first(), 'model.includeroot === \'true\'')
        f.bindAttribute($.find('li.root a').first(),'href',f.pathToUrl('model.rootPageLink'))
        f.mapField($.find('li.root a').first(),'model.rootPageTitle')
        f.addFor($.find('li.children').first(), 'model.childrenPages', 'child')
        f.bindAttribute($.find('li.children a').first(),'href',f.pathToUrl('child.path'))
        f.mapField($.find('li.children a').first(),'child.title')
        f.replace( $.find('ul.nested').eq(0), '<themeclean-components-pagelistnested v-bind:model="child"></themeclean-components-pagelistnested>')
        f.addIf($.find('li.children themeclean-components-pagelistnested').first(), 'child.hasChildren')

        f.addIf($.find('ul.noroot').first(), 'model.includeroot !== \'true\'')
        f.addFor($.find('li.childrennoroot').first(), 'model.childrenPages', 'child')
        f.bindAttribute($.find('li.childrennoroot a').first(),'href',f.pathToUrl('child.path'))
        f.mapField($.find('li.childrennoroot a').first(),'child.title')
        f.replace( $.find('ul.nestednoroot').eq(0), '<themeclean-components-pagelistnested v-bind:model="child"></themeclean-components-pagelistnested>')
        f.addIf($.find('li.childrennoroot themeclean-components-pagelistnested').first(), 'child.hasChildren')
    }
}