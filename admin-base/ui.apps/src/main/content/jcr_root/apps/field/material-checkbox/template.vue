<template>
	<div class="wrap">
		<input 
			class="form-control"
			:id="getFieldID(schema)"
			type="checkbox" 
			v-model="value" 
			:autocomplete="schema.autocomplete" 
			:disabled="disabled" 
			:name="schema.inputName" />
		<label :for="getFieldID(schema)">{{schema.label}}</label>
	</div>
</template>

<script>	
	export default {
		mixins: [ VueFormGenerator.abstractField ]
	}
</script>
