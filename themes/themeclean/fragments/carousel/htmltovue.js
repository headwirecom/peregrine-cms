module.exports = {
    convert: function($, f) {
    	f.wrap($, 'themeclean-components-block')
        f.bindAttribute($.parent(),'model','model')

        f.addStyle( $, 'height', 'model.carouselheight', 'vh')
        f.bindAttribute($, "id", "name")
        f.bindAttribute($, "data-ride", "model.autoplay === 'true' ? 'carousel' : 'false'")
        f.bindAttribute($, "data-interval", "model.autoplay === 'true' ? 1000*model.interval : 'false'")
        f.bindAttribute($, "data-pause", "model.autoplay === 'true' && model.pause === 'true' ? 'hover' : 'false'")
        f.bindAttribute($, "data-wrap", "model.wrap === 'true'")
        f.bindAttribute($, "data-keyboard", "model.keyboard === 'true'")
        
        let firstLi = $.find('li').first()
        f.addIf($.find('ol').first(), "model.indicators === 'true'")
        f.addFor(firstLi, 'model.slides')
        f.bindAttribute(firstLi, "data-target", "`#${name}`")
        f.bindAttribute(firstLi, "data-slide-to", "i")
        f.bindAttribute(firstLi, "class", "{active: i === 0}")
        
        let firstSlide = $.find('.carousel-item').first()
        let image = $.find('img').first()
        let slideCaption = $.find('.carousel-caption').first()
        let h3 = $.find('h3').first()
        let p = $.find('p').first()
        f.addFor(firstSlide, 'model.slides')
        f.bindAttribute(firstSlide, "class", "{active: i === 0}", false)
        f.addIf(image, "item.imagepath")
        f.bindAttribute(image, "src", "item.imagepath")
        f.bindAttribute(image, "alt", "item.alt")
        f.addIf(slideCaption, "item.heading || item.text")
        f.addIf(h3, "item.heading")
        f.mapRichField(h3, "item.heading")
        f.addIf(p, "item.text")
        f.mapRichField(p, "item.text")
        
        let link1 = $.find('a').eq(0)
        let link2 = $.find('a').eq(1)
        f.addIf(link1, "model.controls === 'true'")
        f.bindAttribute(link1, "href", "`#${name}`")
        f.addIf(link2, "model.controls === 'true'")
        f.bindAttribute(link2, "href", "`#${name}`")
    }
}