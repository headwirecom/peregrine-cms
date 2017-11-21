module.exports = {
    convert: function($, f) {
    	f.wrap($, 'themeclean-components-block')
        f.bindAttribute($.parent(),'model','model')

        //Container
        let justifyClasses = 
            "{'justify-content-start': model.aligncontent == 'left'," +
            "'justify-content-center': model.aligncontent == 'center',"+
            "'justify-content-end': model.aligncontent == 'right'}"

        f.bindAttribute( $, 'class', justifyClasses ,false)

        //Text
        f.bindAttribute($.find('div.col-12.p-0').first(), 'class', "`text-${model.aligncontent}`",false)
        f.addStyle( $.find('div.col-12.p-0').first(), 'flex-basis', 'model.textwidth', '%')
    	f.addIf($.find('h2').first(), 'model.showtitle == \'true\'')
        f.mapRichField($.find('h2').first(), "model.title")
        f.addIf($.find('h4').first(), 'model.showsubtitle == \'true\'')
        f.mapRichField($.find('h4').first(), "model.subtitle")
        f.addIf($.find('p').first(), 'model.showtext == \'true\'')
        f.mapRichField($.find('p').first(), "model.text")

        //Buttons
        f.addIf($.find('div.col-12.p-0').eq(1), 'model.showbutton == \'true\'')
        f.bindAttribute($.find('div.col-12.p-0').eq(1), 'class', '`justify-content-sm-${model.alignbuttons}`',false)
        f.addFor($.find('div.col-12>a').first(), 'model.buttons')
        f.bindAttribute($.find('a.btn').first(), 'href', f.pathToUrl('item.buttonlink'))
        f.mapRichField($.find('a.btn').first(), "item.buttontext")
        f.addStyle($.find('a.btn').first(), 'backgroundColor', 'item.buttoncolor')
        f.addStyle($.find('a.btn').first(), 'borderColor', 'item.buttoncolor')
        f.addStyle($.find('a.btn').first(), 'margin-left', "i == 0 ? 0 : '0.5rem'")
        f.addStyle($.find('a.btn').first(), 'margin-right', "i == model.buttons.length-1 ? 0: '0.5rem'")

        $.append('<div v-if="isEditAndEmpty">no content defined for component</div>')
    }
}