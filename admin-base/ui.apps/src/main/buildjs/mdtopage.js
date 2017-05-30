const fs = require('fs-extra')
const marked = require('marked')
const xmlescape = require('xml-escape');

var path = '../../docs/public'

function content(title, html) {
html = xmlescape(html)
return `<?xml version="1.0" encoding="UTF-8"?>
<jcr:root xmlns:sling="http://sling.apache.org/jcr/sling/1.0" xmlns:jcr="http://www.jcp.org/jcr/1.0"
          xmlns:nt="http://www.jcp.org/jcr/nt/1.0"
          jcr:primaryType="per:Page"
>
    <jcr:content
            jcr:primaryType="per:PageContent"
            sling:resourceType="example/components/page"
            jcr:title="${title}"
            brand="peregrine"
            template="/content/templates/example"
    >
        <content jcr:primaryType="nt:unstructured"
                 sling:resourceType="pagerender/vue/structure/container">

            <row jcr:primaryType="nt:unstructured"
                 sling:resourceType="example/components/row">

                <col1 jcr:primaryType="nt:unstructured"
                      sling:resourceType="example/components/col"
                      classes="col-md-12"
                >

                    <text1 jcr:primaryType="nt:unstructured"
                           sling:resourceType="pagerender/vue/components/base"
                           text="${html}"/>
                </col1>

            </row>


        </content>

    </jcr:content>
</jcr:root>`
}

function makeContent(root, path) {

    var md = fs.readFileSync(path).toString()
    var title = path.slice(0, path.lastIndexOf('/'))
    title = title.slice(title.lastIndexOf('/')+1)
    var out = marked.parse(md)
    var res = content(title, out)

    var relPath = 'target/classes/content/sites/docs'+path.slice(root.length)
    relPath = relPath.replace('index.md', '.content.xml')
    console.log(relPath)
    fs.mkdirsSync(relPath.replace('.content.xml', ''))
    fs.writeFileSync(relPath, res)
}

function processDir(root, path) {
    var files = fs.readdirSync(path)
    files.forEach( file => {
        var fullpath = path + '/' + file
        if(fs.lstatSync(fullpath).isDirectory()) {
            processDir(root, fullpath)
        } else if(fullpath.endsWith('.md')) {
            makeContent(root, fullpath)
        }
    })
}

processDir(path, path)
