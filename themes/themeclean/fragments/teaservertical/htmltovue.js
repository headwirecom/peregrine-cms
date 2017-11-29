module.exports = {
    convert: function($, f) {
    	f.wrap($, 'themeclean-components-block')
        f.bindAttribute($.parent(),'model','model')

        //Container
        let textContainerClasses = `{
            'justify-content-start': model.aligncontent === 'left',
            'justify-content-center': model.aligncontent === 'center',
            'justify-content-end': model.aligncontent === 'right'
        }`
        // f.bindAttribute( $, 'class', containerClasses ,false)

        //Text
        let textClasses = `{
            'text-left': model.aligncontent === 'left',
            'text-center': model.aligncontent === 'center',
            'text-right': model.aligncontent === 'right',
        }`
        let textContainer = $.find('div.d-flex.col-12').eq(0)
        let textDiv = textContainer.find('div').first()
        f.bindAttribute( textContainer, 'class', textContainerClasses, false)
        f.bindAttribute( textDiv, 'class', textClasses, false)
        f.addStyle( textDiv, 'width', 'model.textwidth', '%')
    	f.addIf($.find('h2').first(), "model.showtitle === 'true' && (model.isprimary === false || ! model.isprimary)")
        f.addIf($.find('h4').first(), "model.showsubtitle === 'true' && (model.isprimary === false || ! model.isprimary)")
        f.addIf($.find('p').first(), "model.showtext === 'true' && (model.isprimary === false || ! model.isprimary)")
        f.mapRichField($.find('h2').first(), "model.title")
        f.mapRichField($.find('h4').first(), "model.subtitle")
        f.mapRichField($.find('p').first(), "model.text")
        
        f.addIf($.find('h1').first(), "model.showtitle == 'true' && model.isprimary === true")
        f.addIf($.find('h3').first(), "model.showsubtitle === 'true' && model.isprimary === true")
        f.addIf($.find('h5').first(), "model.showtext === 'true' && model.isprimary === true")
        f.mapRichField($.find('h1').first(), "model.title")
        f.mapRichField($.find('h3').first(), "model.subtitle")
        f.mapRichField($.find('h5').first(), "model.text")

        //Buttons
        let buttonsDiv = $.find('div.col-12').eq(1)
        let link = buttonsDiv.find('a')
        f.addIf( buttonsDiv, 'model.showbutton == \'true\'')
        f.bindAttribute( buttonsDiv, 'class', '`justify-content-sm-${model.alignbuttons}`',false)
        f.addFor( link, 'model.buttons')
        f.bindAttribute( link, 'href', f.pathToUrl('item.buttonlink'))
        f.mapRichField( link, "item.buttontext")
        f.addStyle( link, 'backgroundColor', 'item.buttoncolor')
        f.addStyle( link, 'borderColor', 'item.buttoncolor')
        f.addStyle( link, 'margin-left', "i == 0 ? 0 : '0.5rem'")
        f.addStyle( link, 'margin-right', "i == model.buttons.length-1 ? 0: '0.5rem'")

        $.append('<div v-if="isEditAndEmpty">no content defined for component</div>')
    }
}