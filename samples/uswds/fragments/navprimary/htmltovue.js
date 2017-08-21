module.exports = {
    convert: function($, f) {
        // f.bindPath($)
        $.find('img').each(function(i, elem) {
            elem.attribs.src = elem.attribs.src.replace('{{ uswds.path }}', '/etc/felibs/uswds')
        })
    }
}