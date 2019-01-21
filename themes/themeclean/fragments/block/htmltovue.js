module.exports = {
    convert: function($, f) {
        f.bindAttribute( $, 'class', '[classes, colors]', false)
        f.bindAttribute( $, 'style', '[styles, sticky]', false)
        f.bindAttribute( $, 'data-per-path', 'model.path')

        let anchor = $.find('a')
        f.bindAttribute( anchor, 'id', 'model.anchorname')

        f.addIf( $.find('div.embed-responsive').first(), "model.custombackground === 'true' && model.backgroundtype == 'video' && model.bgvideo" )
        f.bindAttribute( $.find('iframe').first(), 'src', "model.bgvideo + '?autoplay=1&loop=1&controls=0&mute=1'" )
        f.addStyle( $.find('div.embed-responsive').first(), 'position', "'absolute'")
        f.addStyle( $.find('div.embed-responsive').first(), 'pointer-events', "'none'")
        $.find('div.container>div').first().append( '<slot>')
        f.bindAttribute( $.find('div.container').first(), 'class', "model.fullwidth === 'true' ? 'container-fluid' : 'container'")
    }
}