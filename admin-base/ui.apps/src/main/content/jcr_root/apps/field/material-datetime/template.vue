<template>
		<datetime v-model="sanitizedValue" type="datetime" placeholder="Select datetime"></datetime>
</template>

<script>	
	export default {
		mixins: [ VueFormGenerator.abstractField ],
		computed: {
			sanitizedValue: {
				get () {
      		return this.value ? this.value : ''
				},
				set (newValue) {
					this.value = newValue
				}
			}
		}
	}
</script>
