<template>
	<div :class="explorerPreviewClasses">
	  <span class="active_tab_title">{{$i18n(getActiveTabName)}}</span>
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
			isFullscreen: false,
			activeTabName: "info"
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
		getActiveTabName(){
			switch(this.activeTabName) {
				case 'info':
					return "Properties & Information"
				case 'og-tags':
					return "Open Graph Tags"
				case 'versions':
					return "Versioning"
				case 'publishing':
					return "Web Publishing"
				case 'actions':
					return "Actions"
				case 'references':
					return "References"
			}
		}
	},

	methods: {
		onPreviewExitFullscreen(){
			this.isFullscreen = false
		},
		onPreviewFullscreen(){
			console.log(this)
			this.isFullscreen = true
		},
		setActiveTabName(me, target){
			me.activeTabName = target.activeTab
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