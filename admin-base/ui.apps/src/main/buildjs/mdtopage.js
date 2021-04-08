/*-
 * #%L
 * admin base - UI Apps
 * %%
 * Copyright (C) 2017 headwire inc.
 * %%
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * #L%
 */
const fs = require('fs-extra')
const marked = require('marked')
const xmlescape = require('xml-escape');
const p = require('path')

var path = '../../docs/public'

function content(title, html, order) {
html = xmlescape(html).split(".md&quot;&gt;").join(".html&quot;&gt;")
return `<?xml version="1.0" encoding="UTF-8"?>
<jcr:root 
   xmlns:sling="http://sling.apache.org/jcr/sling/1.0" 
   xmlns:jcr="http://www.jcp.org/jcr/1.0"
   xmlns:nt="http://www.jcp.org/jcr/nt/1.0"
   jcr:primaryType="per:Page">
   
   <jcr:content
      jcr:primaryType="per:PageContent"
      sling:resourceType="example/components/page"
      jcr:title="${title}"
      template="/content/example/templates/base">
     
      <content jcr:primaryType="nt:unstructured"
        sling:resourceType="example/components/container">
        
        <row jcr:primaryType="nt:unstructured"
          sling:resourceType="example/components/row">

          <col1 jcr:primaryType="nt:unstructured"
            sling:resourceType="example/components/col"
            classes="col-md-12">
            
            <text2 jcr:primaryType="nt:unstructured"
              sling:resourceType="example/components/text"
              text="${html}"/>
              
          </col1>

        </row>

      </content>

    </jcr:content>
    
    ${order}
   
</jcr:root>`
}

function findOrder(commands) {
  var ret = ''
  var order = /order: (.*)/g.exec(commands)
  if (order[1]) {
    order[1].split(',').forEach(x => {
      var node = x.trim()
      ret += '<' + node + '/>\n'
    })
  }
  return ret;
}

function makeContent(root, path) {
  var isIndex = path.endsWith("/index.md");
  var title;
  if (isIndex) {
    title = path.slice(0, path.lastIndexOf('/'))
    title = title.slice(title.lastIndexOf('/') + 1)
  } else {
    title = path.slice(path.lastIndexOf('/') + 1)
    title = title.slice(0, title.lastIndexOf('.md'))
  }

  var md = fs.readFileSync(path).toString()
    .replace(/!\[(.*)\]\((.*)\)/g, function (math, p1, p2, string) {
      return '![' + p1 + '](' + title + '/' + p2 + ')'
    })

  var order = ''
  // trim commands
  if (md.startsWith('```')) {
    var commands = md.slice(3, md.indexOf('```', 3))
    order = findOrder(commands)
    md = md.slice(md.indexOf('```', 3) + 3)
  }

  var out = marked.parse(md) + '<p>&nbsp;</p>'
  var res = content(title, out, order)

  var relPath = 'target/classes/content/docs/pages/public' + path.slice(root.length)
  console.log(`${relPath}`)
  fs.mkdirsSync(`${relPath}/..`)
  fs.writeFileSync(isIndex ? p.join(`${relPath}`, '../.content.xml') : relPath.replace(".md", ".xml"), res)
}

function copyImage(root, path) {
  var image = fs.readFileSync(path)
  var title = path.slice(0, path.lastIndexOf('/'))
  var name = path.slice(path.lastIndexOf('/'))

  var relPath = 'target/classes/content/docs/pages/public' + title.slice(root.length)
  fs.mkdirsSync(relPath)
  fs.writeFileSync(relPath + '/' + name, image)
}

function processDir(root, path) {
  var files = fs.readdirSync(path)
  files.forEach(file => {
    var fullpath = path + '/' + file
    if (fs.lstatSync(fullpath).isDirectory()) {
      processDir(root, fullpath)
    } else if (fullpath.endsWith('.md')) {
      makeContent(root, fullpath)
    } else if (fullpath.endsWith('.png')) {
      copyImage(root, fullpath)
    }
  })
}

processDir(path, path)
