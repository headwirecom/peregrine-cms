<template>

	<div v-if="!schema.preview" class="input-field">
		<multiselect v-model="value" :options="options">
		</multiselect>
	</div>

	<p v-else>{{value}}</p>

</template>

<script>	
	export default {
		mixins: [ VueFormGenerator.abstractField ], 

		data() {
			return {
				options: ['list', 'of', 'options']
			}
		},

		computed: {
		},

		methods: {
		}
	};
</script>
