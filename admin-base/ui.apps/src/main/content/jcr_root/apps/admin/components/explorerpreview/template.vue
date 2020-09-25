<template>
	<div :class="explorerPreviewClasses">
	  <button 
	    type="button" 
	    class="toggle-fullscreen waves-effect waves-light"  
			:title="$i18n(isFullscreen ? 'exitFullscreen' : 'enterFullscreen')"
	    v-on:click.prevent="isFullscreen ? onPreviewExitFullscreen() : onPreviewFullscreen()">
	    <i class="material-icons">{{ isFullscreen ? 'fullscreen_exit': 'fullscreen'}}</i>
	  </button>
	  <slot></slot>
	</div>
</template>

<script>
export default {

	data() {
		return{ 
			isFullscreen: false,
		}
	},

	computed: {
		explorerPreviewClasses() {
			if(this.isFullscreen) {
				return 'explorer-preview fullscreen'
			} else {
				return 'col s12 m4 explorer-preview'
			}
		},
	},

	methods: {
		onPreviewExitFullscreen(){
			this.isFullscreen = false
		},
		onPreviewFullscreen(){
			console.log(this)
			this.isFullscreen = true
		},
	}
}
</script>

<style>
	.active_tab_title { 
		display: inline-block;
		position: absolute;
		top: 0;
		left: 0;
		right: 0;
		height: 45px;
		background-color: #cfd8dc;
		border-bottom: 1px solid #b0bec5;
		padding-top: 13px;
    	padding-left: 15px;
	}
</style>