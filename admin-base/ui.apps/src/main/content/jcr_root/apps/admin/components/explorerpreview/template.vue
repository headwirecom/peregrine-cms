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
		return {
			isFullscreen: false
		}
	},

	computed: {
		explorerPreviewClasses() {
			if(this.isFullscreen) {
				return 'explorer-preview fullscreen'
			} else {
				return 'col s12 m4 explorer-preview'
			}
		}
	},

	methods: {
		onPreviewExitFullscreen(){
      this.isFullscreen = false
    },
    onPreviewFullscreen(){
      this.isFullscreen = true
    }
	}
}
</script>