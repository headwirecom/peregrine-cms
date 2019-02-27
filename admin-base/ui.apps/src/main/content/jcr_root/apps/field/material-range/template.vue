<template>
	<p class="range-field">
		<input
			type="range"
			v-model="value"
			:id="getFieldID(schema)"
			:class="schema.fieldClasses"
			:disabled="schema.disabled || schema.preview"
			:alt="schema.alt"
			:max="schema.max"
			:min="schema.min"
			:name="schema.inputName"
			:required="schema.required"
			:step="schema.step"
		/>
	</p>
</template>

<script>	
	export default {
		mixins: [ VueFormGenerator.abstractField ],
	}
</script>
