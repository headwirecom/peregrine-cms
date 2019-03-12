<template>

	<div v-if="!schema.preview">
		<multiselect 
			v-model="value" 
			:options="schema.values" 
			:multiple="true"
			:taggable="schema.selectOptions.taggable" 
			:custom-label="customLabel"
			@tag="addItem">
		</multiselect>
	</div>

	<ul v-else class="collection">
		<div v-for="item in value" class="collection-item">{{item.name || item}}</div>
	</ul>

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
			},
			customLabel(label) {
				return typeof label === 'object' ? label.name: label
			}
		}
	};
</script>
