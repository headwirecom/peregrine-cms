module.exports = {
    convert: function($, f) {
    	f.wrap($, 'themeclean-components-block')
        f.bindAttribute($.parent(),'model','model')

        //Container
        let containerClasses = `{
            'flex-md-row-reverse': model.buttonside === 'left',
            'flex-md-row': model.buttonside === 'right',
        }`
        f.bindAttribute( $, 'class', containerClasses ,false)

        //Text
        let textClasses = `{
            'text-left': model.aligncontent === 'left',
            'text-center': model.aligncontent === 'center',
            'text-right': model.aligncontent === 'right'
        }`
        let textDiv = $.find('div').eq(0)
        f.bindAttribute( textDiv, 'class', textClasses,false)
        f.addStyle( textDiv, 'flex-basis', 'model.textwidth', '%')
    	f.addIf($.find('h2').first(), "model.showtitle === 'true' && model.isprimary === false")
        f.addIf($.find('h4').first(), "model.showsubtitle === 'true' && model.isprimary === false")
        f.addIf($.find('p').first(), "model.showtext === 'true' && model.isprimary === false")
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
        let buttonsDiv = $.find('div').eq(1)
        let link = buttonsDiv.find('a')
        let buttonClasses = `{
            'justify-content-end': model.buttonside === 'right',
            'justify-content-start': model.buttonside === 'left',
        }`
        f.addIf( buttonsDiv, 'model.showbutton == \'true\'')
        f.bindAttribute( buttonsDiv, 'class', buttonClasses,false)
        f.addFor( link, 'model.buttons')
        f.bindAttribute( link, 'href', f.pathToUrl('item.buttonlink'))
        f.mapRichField( link, "item.buttontext")
        f.addStyle( link, 'backgroundColor', 'item.buttoncolor')
        f.addStyle( link, 'borderColor', 'item.buttoncolor')

        $.append('<div v-if="isEditAndEmpty">no content defined for component</div>')
    }
}