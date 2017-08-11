<template>
	<textarea 
		class="form-control materialize-textarea" 
		v-model="value" 
		:id="getFieldID(schema)" 
		:disabled="disabled" 
		:maxlength="schema.max" 
		:minlength="schema.min" 
		:placeholder="schema.placeholder" 
		:readonly="schema.readonly" 
		:rows="schema.rows || 2" 
		:name="schema.inputName">
	</textarea>
</template>

<script>	
	export default {
		mixins: [ VueFormGenerator.abstractField ]
	}
</script>
