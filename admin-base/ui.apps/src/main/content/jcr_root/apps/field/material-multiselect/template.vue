<template>

	<div v-if="!schema.preview">
		<multiselect 
			v-model="value" 
			:options="schema.values" 
			:multiple="true"
			:taggable="schema.taggable" 
			@tag="addItem">
		</multiselect>
	</div>

	<p v-else>{{value}}</p>

</template>

<script>	
	export default {
		mixins: [ VueFormGenerator.abstractField ], 

		computed: {
		},

		methods: {
			addItem(item) {
				this.value.push(item);
				this.schema.values.push(item);
			}
		}
	};
</script>
