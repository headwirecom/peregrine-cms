module.exports = {
    convert: function($, f) {
        f.bindAttribute( $, 'class', 'classes', false)
        f.bindAttribute( $, 'style', '[styles, sticky]', false)
        f.bindAttribute( $, 'data-per-path', 'model.path')

        f.addIf( $.find('div.embed-responsive').first(), "model.backgroundtype == 'video'" )
        f.bindAttribute( $.find('iframe').first(), 'src', "model.bgvideo + '?autoplay=1&loop=1&controls=0&mute=1'" )
        f.addStyle( $.find('div.embed-responsive').first(), 'position', "'absolute'")
        f.addStyle( $.find('div.embed-responsive').first(), 'pointer-events', "'none'")
        f.bindAttribute( $.find('div.container').eq(), 'class', "model.fullwidth === 'true' ? 'container-fluid' : 'container'")
        $.find('div.container>div').first().append( '<slot>')
    }
}