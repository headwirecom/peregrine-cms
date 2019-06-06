import buble from 'rollup-plugin-buble';

import resolve from 'rollup-plugin-node-resolve';
import replace from 'rollup-plugin-replace';
import css from 'rollup-plugin-css-only'
import commonjs from 'rollup-plugin-commonjs'
import vue from 'rollup-plugin-vue';

export default {
	input: 'src/main/js/peregrineApp.js',
	output: {
		file: 'target/classes/etc/felibs/pagerender-vue/js/perview.js',
		format: 'iife',
		name: '$peregrineApp',
	},
	plugins: [
		css(),
		resolve( {include:[ 'mdbvue' ], extensions: ['.vue', '.js', '.css']}),
		replace({
			'process.env.NODE_ENV': JSON.stringify( 'production' )
		}),
		commonjs(),
		vue(),
		buble({ objectAssign: "Object.assign", transforms: { forOf: false }}) ],
}
