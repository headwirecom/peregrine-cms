import buble from 'rollup-plugin-buble';

export default {
	input: 'src/main/js/peregrineApp.js',
	output: {
			file: 'target/classes/etc/felibs/pagerendervue/js/perview.js',
			format: 'iife',
			name: '$peregrineApp',
	},
	plugins: [ buble() ],
}
