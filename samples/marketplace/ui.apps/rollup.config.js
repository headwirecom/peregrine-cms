import buble from 'rollup-plugin-buble';

export default {
	input: 'src/main/js/api.js',
	output: {
			file: 'target/classes/etc/felibs/marketplace/js/api.js', 
			format: 'iife',
			name: 'api',
	},
	plugins: [ buble() ],
}
