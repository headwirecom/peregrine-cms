<template>
		<datetime v-model="sanitizedValue" type="time" placeholder="Select time"></datetime>
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
