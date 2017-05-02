import buble from 'rollup-plugin-buble';

export default {
	entry: 'src/main/js/perAdminApp.js',
	moduleName: '$perAdminApp',
	plugins: [ buble() ],
	targets: [
		{dest: 'target/classes/etc/felibs/admin/js/perAdminApp.js', format: 'iife' }
	]
}