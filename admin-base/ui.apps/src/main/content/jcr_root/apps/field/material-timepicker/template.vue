<template>
		<datetime v-model="value" type="time" placeholder="Select time"></datetime>
</template>

<script>	
	export default {
		mixins: [ VueFormGenerator.abstractField ],
	  data: function(){
			return {
				time: '20:40:00Z'
			}
		}
	}
</script>
