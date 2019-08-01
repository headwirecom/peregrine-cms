<template>
	<div class="editor-panel-buttons">
		<button v-for="btn in schema.list"
				class="control-btn waves-effect waves-light btn btn-raised"
				title="btn.title" v-on:click="handleControlClick( btn )">
			<i class="material-icons">{{btn.label}}</i>
		</button>
	</div>
</template>

<script>
	export default {
      mixins: [VueFormGenerator.abstractField],
      methods: {
        handleControlClick: function( btn ){
          $perAdminApp.getApp().$adminEventBus.$emit( btn.event );
        }
      }
    }
</script>

<style scoped>
	.control-btn {
		width: 50px;
	}
</style>
