module.exports = {
    convert: function($, f) {
    	f.wrap($, 'themeclean-components-block')
        f.bindAttribute($.parent(),'model','model')

        //Header
    	f.addIf($.find('h2').first(), 'model.showtitle == \'true\'')
        f.mapRichField($.find('h2').first(), "model.title")
        f.addFor($.find('div.card').first(), 'model.accordiontoggle')

        //Media
        let mediaDiv  = $.find('div.row>div').first()
        f.bindAttribute( mediaDiv, 'style', "{width:`${model.mediawidth}%`}")
        f.replace( mediaDiv.find('img'), '<themeclean-components-media :model="model"></themeclean-components-media>')

        //Accordion Container
        let accordionContainer = $.find('div.col-12').eq(1)
        f.bindAttribute( accordionContainer, 'ref', 'collapsible')
        f.bindAttribute( accordionContainer, 'id', "model.toggletype === 'accordion' ? `accordion${_uid}` : ''")

        //Accordion Item Title Bar
        f.bindAttribute($.find('a').first(), 'data-parent', '`#accordion${_uid}`')
        f.bindAttribute($.find('a').first(), 'href', '`#accordion${_uid}${i}`')
        f.bindAttribute($.find('a').first(), 'aria-controls', '`accordion${_uid}${i}`')
        f.mapRichField($.find('a').first(), "item.title")

        //Acocordion Item Body
        f.bindAttribute($.find('div.collapse').first(), 'id', '`accordion${_uid}${i}`')
        f.mapRichField($.find('div.card-body').first(), "item.text")
        f.addIf($.find('.perIsEditAndEmpty').first(), 'isEditAndEmpty')
        f.bindAttribute($.find('div.flex-wrap').first(),  'class', "model.mediaposition === 'after' ? 'flex-row-reverse': 'flex-row'", false)
    }
}