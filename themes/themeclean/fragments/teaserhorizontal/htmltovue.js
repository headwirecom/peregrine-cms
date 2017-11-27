module.exports = {
    convert: function($, f) {
    	f.wrap($, 'themeclean-components-block')
        f.bindAttribute($.parent(),'model','model')

        //Container

        //Text
        let textClasses = `{
            'text-left': model.aligncontent === 'left',
            'text-center': model.aligncontent === 'center',
            'text-right': model.aligncontent === 'right'
        }`
        let textDiv = $.find('div').eq(0)
        f.bindAttribute( textDiv, 'class', textClasses,false)
        f.addStyle( textDiv, 'width', 'model.textwidth', '%')
    	f.addIf($.find('h2').first(), 'model.showtitle == \'true\'')
        f.addIf($.find('h4').first(), 'model.showsubtitle == \'true\'')
        f.addIf($.find('p').first(), 'model.showtext == \'true\'')
        f.mapRichField($.find('h2').first(), "model.title")
        f.mapRichField($.find('h4').first(), "model.subtitle")
        f.mapRichField($.find('p').first(), "model.text")

        //Buttons
        let buttonsDiv = $.find('div').eq(1)
        let link = buttonsDiv.find('a')
        f.addIf( buttonsDiv, 'model.showbutton == \'true\'')
        f.bindAttribute( buttonsDiv, 'class', '`align-self-sm-${model.alignbuttons}`',false)
        f.addFor( link, 'model.buttons')
        f.bindAttribute( link, 'href', f.pathToUrl('item.buttonlink'))
        f.mapRichField( link, "item.buttontext")
        f.addStyle( link, 'backgroundColor', 'item.buttoncolor')
        f.addStyle( link, 'borderColor', 'item.buttoncolor')

        $.append('<div v-if="isEditAndEmpty">no content defined for component</div>')
    }
}