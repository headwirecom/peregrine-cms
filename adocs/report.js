const fs = require('fs')

const sizes = fs.readdirSync('./target')
const files = fs.readdirSync('./target/'+sizes[0])

console.log('<table>')
files.forEach( item => {

	sizes.forEach( size => {
	console.log(`<tr>`)

	console.log(`<td>`)
	console.log(`<h3>${size}/${item}</h3>`)
	console.log(`<img style='max-width: 800px;' src="target/`+size+`/${item}">`)
	console.log(`</td>`)

//		console.log(size,item)

	console.log(`</tr>`)
	})


})
console.log('</table>')



//	console.log(`<h1>${item}</h1>`)
//	console.log(`<ul>`)
//	files.forEach( file => {
//		console.log(`<li><img style='max-width: 800px;' src="target/${item}/${file}"></li>`)
//	})
//	console.log(`</ul>`)
