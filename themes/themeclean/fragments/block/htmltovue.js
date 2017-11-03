module.exports = {
    convert: function($, f) {
        f.bindAttribute( $, 'class', 'classes', false)
        f.bindAttribute( $, 'style', '[styles, sticky]', false)
        f.bindAttribute( $, 'data-per-path', 'model.path')

        f.bindAttribute( $.find('div').first(), 'class', "model.fullwidth === 'true' ? 'container-fluid' : 'container'")
        $.find('div>div').first().append( '<slot>')
    }
}