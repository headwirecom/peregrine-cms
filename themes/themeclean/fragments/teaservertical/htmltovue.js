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
            'percms-text-large': model.isprimary === 'true'
        }`
        let textContainer = $.find('div.d-flex.col-12').eq(0)
        let textDiv = textContainer.find('div').first()
        f.bindAttribute( textContainer, 'class', textContainerClasses, false)
        f.bindAttribute( textDiv, 'class', textClasses, false)
        f.addStyle( textDiv, 'width', 'model.textwidth', '%')
    	f.addIf($.find('h2').first(), "model.showtitle === 'true'")
        f.addIf($.find('h4').first(), "model.showsubtitle === 'true'")
        f.addIf($.find('p').first(), "model.showtext === 'true'")
        f.mapRichField($.find('h2').first(), "model.title")
        f.mapRichField($.find('h4').first(), "model.subtitle")
        f.mapRichField($.find('p').first(), "model.text")

        //Buttons
        let buttonsDiv = $.find('div.col-12').eq(1)
        let link = buttonsDiv.find('a')
        let buttonsClasses = `{
            'justify-content-md-start': model.alignbuttons === 'start',
            'justify-content-md-center': model.alignbuttons === 'center',
            'justify-content-md-end': model.alignbuttons === 'end'
        }`
        let aClasses = `{
            'btn-lg': model.buttonsize === 'large',
            'btn-sm': model.buttonsize === 'small',
            'btn-primary': item.buttoncolor === 'primary',
            'btn-secondary': item.buttoncolor === 'secondary'
        }`

        f.addIf( buttonsDiv, 'model.showbutton == \'true\'')
        f.bindAttribute( buttonsDiv, 'class', buttonsClasses,false)

        let buttonClasses = `{
        }`
        f.addFor( link, 'model.buttons')
        f.bindAttribute( link, 'href', f.pathToUrl('item.buttonlink'))
        f.bindAttribute( link, 'class', aClasses, false)
        f.mapRichField( link, "item.buttontext")
        f.addStyle( link, 'backgroundColor', 'item.buttoncolor')
        f.addStyle( link, 'borderColor', 'item.buttoncolor')

        $.append('<div v-if="isEditAndEmpty">no content defined for component</div>')
    }
}