<template>
	<div class="wrap">
		<template v-if="!schema.preview">
			<input 
				class="form-control"
				:id="getFieldID(schema)"
				type="checkbox" 
				v-model="value" 
				:autocomplete="schema.autocomplete" 
				:disabled="disabled" 
				:name="schema.inputName" />
			<label :for="getFieldID(schema)">{{schema.label}}</label>
		</template>
		<template v-else>
			<label>{{schema.label}}</label>
			<p>{{value}}</p>
		</template>
	</div>
</template>

<script>	
	export default {
		mixins: [ VueFormGenerator.abstractField ]
	}
</script>
