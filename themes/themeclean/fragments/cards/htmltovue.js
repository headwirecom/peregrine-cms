module.exports = {
    convert: function($, f) {
    	f.wrap($, 'themeclean-components-block')
        f.bindAttribute($.parent(),'model','model')

        //Card
        let cardClasses = `{
            'bg-dark': model.customcardcolor !== 'true' && model.colorscheme === 'light',
            'bg-light': model.customcardcolor !== 'true' &&  model.colorscheme === 'dark',
            'text-dark': (model.showcard === 'false' && model.colorscheme === 'light') || (model.showcard === 'true' && model.colorscheme === 'dark'),
            'text-light': (model.showcard === 'false' && model.colorscheme === 'dark') || (model.showcard === 'true' && model.colorscheme === 'light'),
            'bg-transparent': model.showcard === 'false'
        }`
        f.addFor($.find('div.col-12').first(), 'model.cards')
        f.addStyle($.find('div.card').first(), 'background-color', "model.customcardcolor === 'true' ? model.cardcolor: ''")
        f.bindAttribute($.find('div.card').first(), 'class', cardClasses, false)

        let cardBodyClasses = `{
            'card-body': model.showcard === 'true',
            'px-3 p-md-0': model.showcard === 'false'
        }`
        f.bindAttribute($.find('div.card-body').first(), 'class', cardBodyClasses)

        //Image
        f.bindAttribute($.find('img').first(), 'class', "model.showcard == 'true' ? 'card-img pb-1' : 'card-img pb-3'")
        f.bindAttribute($.find('img').first(), 'src', f.pathToUrl('item.image'))
        f.bindAttribute($.find('img').first(), 'alt', 'item.imagealttext')
        f.addIf($.find('img').first(), 'item.image')

        //Title
        f.addIf($.find('h5').first(), 'model.showtitle == \'true\'')
        f.mapRichField($.find('h5').first(), "item.title")
        f.addStyle($.find('h5').first(), 'color', 'item.color')

        //Text
        f.addIf($.find('p').first(), 'model.showtext == \'true\'')
        f.mapRichField($.find('p').first(), "item.text")

        //Button
        f.addIf($.find('div.text-center').first(), 'item.buttontext')
        f.addIf($.find('a.btn').first(), 'model.showbutton == \'true\'')
        f.bindAttribute($.find('a.btn').first(), 'href', f.pathToUrl('item.buttonlink'))
        f.mapRichField($.find('a.btn').first(), "item.buttontext")
        f.addStyle($.find('a.btn').first(), 'backgroundColor', 'item.buttoncolor')
        f.addStyle($.find('a.btn').first(), 'borderColor', 'item.buttoncolor')

        f.addIf($.find('.perIsEditAndEmpty').first(), 'isEditAndEmpty')
        f.addIf($.find('h1').first(), 'editAndEmpty')
    }
}