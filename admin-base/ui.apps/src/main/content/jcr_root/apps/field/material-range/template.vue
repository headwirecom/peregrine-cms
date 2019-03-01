<template>
	<p v-if="!schema.preview" class="range-field">
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
	<div v-else>{{value}}</div>
</template>

<script>	
	export default {
		mixins: [ VueFormGenerator.abstractField ],
	}
</script>
