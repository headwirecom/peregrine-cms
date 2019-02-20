import buble from 'rollup-plugin-buble';

export default {
	input: 'src/main/js/perAdminApp.js',
	output: {
		file: 'target/classes/etc/felibs/admin/js/perAdminApp.js', 
		format: 'iife',
		name: '$perAdminApp',
	},
	plugins: [ buble() ],
}
