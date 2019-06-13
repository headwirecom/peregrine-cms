import buble from 'rollup-plugin-buble';
import resolve from 'rollup-plugin-node-resolve';
import css from 'rollup-plugin-css-only';
import replace from 'rollup-plugin-replace';
import commonjs from 'rollup-plugin-commonjs';
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
    resolve( {include:[ 'mdbvue'], extensions: ['.vue', '.js', '.css']}),
    replace({
      'process.env.NODE_ENV': JSON.stringify( 'production' )
    }),
    commonjs(),
    vue(),
    buble({
      objectAssign: "Object.assign",
      transforms: {
        forOf: false
      },
      exclude: 'node_modules/vue/dist/**'
    })
  ]
}
