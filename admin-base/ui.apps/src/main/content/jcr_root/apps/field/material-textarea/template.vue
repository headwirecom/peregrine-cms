<template>
	<div class="input-field">
		<textarea 
			ref="textarea"
			class="form-control materialize-textarea" 
			v-model="value" 
			:id="getFieldID(schema)" 
			:disabled="disabled" 
			:maxlength="schema.max" 
			:minlength="schema.min" 
			:placeholder="schema.placeholder" 
			:readonly="schema.readonly" 
			:name="schema.inputName">
		</textarea>
	</div>
</template>

<script>	
	export default {
		mixins: [ VueFormGenerator.abstractField ],
		mounted() {
			$(this.$refs.textarea).trigger('autoresize');
		},
		updated() {
			$(this.$refs.textarea).trigger('autoresize');
		}
	}
</script>

<style>
</style>