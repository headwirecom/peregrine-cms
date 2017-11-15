module.exports = {
    convert: function($, f) {
        //f.bindPath($)
    	f.wrap($, 'themeclean-components-block')
        f.bindAttribute($.parent(),'model','model')
    	f.addFor($.find('li.nav-item').first(), 'model.tabs')
    	f.bindAttribute($.find('a').first(), 'href', '`#tab${_uid}${parseInt(i)+1}`')
    	f.bindAttribute($.find('a').first(), 'class', 'i == 0 ? \'nav-link active\' : \'nav-link\'')
    	f.bindAttribute($.find('a').first(), 'id', '`tab-control-${_uid}${parseInt(i)+1}`')
    	f.bindAttribute($.find('a').first(), 'aria-controls', '`tab${_uid}${parseInt(i)+1}`')
    	f.mapRichField($.find('a').first(), "item.title")
    	
    	f.addFor($.find('div.tab-pane').first(), 'model.tabs')
    	f.bindAttribute($.find('div.tab-pane').first(), 'id', '`tab${_uid}${parseInt(i)+1}`')
        f.bindAttribute($.find('div.tab-pane').first(), 'aria-labelledby', '`tablabel${_uid}${parseInt(i)+1}`')
        f.bindAttribute($.find('div.tab-pane').first(), 'class', 'i == 0 ? \'tab-pane fade show active\' : \'tab-pane fade\'')
        f.mapRichField($.find('div.article').first(), "item.text")
    	f.addIf($.find('.perIsEditAndEmpty').first(), 'isEditAndEmpty')
        
    }
}