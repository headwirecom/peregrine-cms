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
            'text-right': model.aligncontent === 'right',
            'percms-text-large': model.isprimary === 'true'
        }`
        let textDiv = $.find('div').eq(0)
        f.bindAttribute( textDiv, 'class', textClasses,false)
        f.addStyle( textDiv, 'flex-basis', 'model.textwidth', '%')
        
    	f.addIf($.find('h2').first(), "model.showtitle === 'true'")
        f.addIf($.find('h4').first(), "model.showsubtitle === 'true'")
        f.addIf($.find('p').first(), "model.showtext === 'true'")
        f.mapRichField($.find('h2').first(), "model.title")
        f.mapRichField($.find('h4').first(), "model.subtitle")
        f.mapRichField($.find('p').first(), "model.text")
        
        //Buttons
        let buttonsDiv = $.find('div').eq(1)
        let link = buttonsDiv.find('a')
        let buttonsClasses = `{
            'justify-content-md-end': model.buttonside === 'right',
            'justify-content-md-start': model.buttonside === 'left',
        }`

        let aClasses = `{
            'btn-lg': model.buttonsize === 'large',
            'btn-sm': model.buttonsize === 'small',
            'btn-primary': item.buttoncolor === 'primary',
            'btn-secondary': item.buttoncolor === 'secondary',
            'btn-success': item.buttoncolor === 'success',
            'btn-danger': item.buttoncolor === 'danger',
            'btn-warning': item.buttoncolor === 'warning',
            'btn-info': item.buttoncolor === 'info',
            'btn-light': item.buttoncolor === 'light',
            'btn-dark': item.buttoncolor === 'dark'
        }`
        f.addIf( buttonsDiv, 'model.showbutton == \'true\'')
        f.bindAttribute( buttonsDiv, 'class', buttonsClasses,false)
        f.addFor( link, 'model.buttons')
        f.bindAttribute( link, 'href', f.pathToUrl('item.buttonlink'))
        f.bindAttribute( link, 'class', aClasses, false)
        f.mapRichField( link, "item.buttontext")
        f.addStyle( link, 'backgroundColor', 'item.buttoncolor')
        f.addStyle( link, 'borderColor', 'item.buttoncolor')

        $.append('<div v-if="isEditAndEmpty">no content defined for component</div>')
    }
}