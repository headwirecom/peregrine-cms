import babel from "@rollup/plugin-babel";

export default {
	input: 'src/main/js/perAdminApp.js',
	output: {
		file: 'target/classes/etc/felibs/admin/js/perAdminApp.js',
		format: 'iife',
		name: '$perAdminApp',
	},
  plugins: [
    babel({
      babelHelpers: 'bundled',
      exclude: 'node_modules/**',
      extensions: [
        '.js',
        '.jsx',
        '.vue',
      ]
    }),
  ],
}
