module.exports = {
    convert: function($, f) {
    	f.wrap($, 'themeclean-components-block')
        f.bindAttribute($.parent(),'model','model')

        f.replace( $.find('img'), '<themeclean-components-media :model="model"></themeclean-components-media>')

        let imageDiv = $.find('div.col-md-auto').first()
        f.addIf( imageDiv, 'model.showmedia == \'true\'')

        let containerClasses = `{
            'flex-row': model.mediaposition === 'before',
            'flex-row-reverse': model.mediaposition === 'after'
        }`
        f.bindAttribute($.find('div.row').eq(1), 'class', containerClasses, false)

        f.bindAttribute($.find('div.row>div.col').first(), 'style', "{width:`${model.mediawidth}%`}")
    	f.addIf($.find('h2').first(), 'model.showtitle == \'true\' && model.title')
        f.mapRichField($.find('h2').first(), "model.title")
        f.addIf($.find('h3').first(), 'model.showsubtitle == \'true\' && model.subtitle')
        f.mapRichField($.find('h3').first(), "model.subtitle")

        f.addFor($.find('li.nav-item').first(), 'model.tabs')
        let a = $.find('a').first()
        let aClasses = `[
            {'nav-link': true},
            {'active': i == 0},
            {'bg-primary': model.tabcolor === 'primary'},
            {'bg-secondary': model.tabcolor === 'secondary'},
            {'bg-success': model.tabcolor === 'success'},
            {'bg-danger': model.tabcolor === 'danger'},
            {'bg-warning': model.tabcolor === 'warning'},
            {'bg-info': model.tabcolor === 'info'},
            {'bg-light': model.tabcolor === 'light'},
            {'bg-dark': model.tabcolor === 'dark'},
            textClasses
        ]`
    	f.bindAttribute( a, 'href', '`#tab${_uid}${parseInt(i)+1}`')
    	f.bindAttribute( a, 'class', aClasses)
    	f.bindAttribute( a, 'id', '`tab-control-${_uid}${parseInt(i)+1}`')
    	f.bindAttribute( a, 'aria-controls', '`tab${_uid}${parseInt(i)+1}`')
    	f.mapRichField( a, "item.title")
    	
    	f.addFor($.find('div.tab-pane').first(), 'model.tabs')
    	f.bindAttribute($.find('div.tab-pane').first(), 'id', '`tab${_uid}${parseInt(i)+1}`')
        f.bindAttribute($.find('div.tab-pane').first(), 'aria-labelledby', '`tablabel${_uid}${parseInt(i)+1}`')
        f.bindAttribute($.find('div.tab-pane').first(), 'class', 'i == 0 ? \'tab-pane fade show active\' : \'tab-pane fade\'')
        f.mapRichField($.find('div.text-center').first(), "item.text")
    	f.addIf($.find('.perIsEditAndEmpty').first(), 'isEditAndEmpty')
        
    }
}