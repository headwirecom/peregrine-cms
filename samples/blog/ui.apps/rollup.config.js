import buble from 'rollup-plugin-buble';

export default {
	entry: 'src/main/js/api.js',
	moduleName: 'api',
	plugins: [ buble() ],
	targets: [
		{dest: 'target/classes/etc/felibs/blog/js/api.js', format: 'iife' }
	]
}