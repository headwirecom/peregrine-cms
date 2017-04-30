import buble from 'rollup-plugin-buble';

export default {
	entry: 'src/main/js/peregrineApp.js',
	moduleName: '$peregrineApp',
	plugins: [ buble() ],
	targets: [
		{dest: 'target/perview.js', format: 'iife' }
	]
}