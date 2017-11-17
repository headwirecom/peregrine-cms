<template>
		<datetime v-model="value" type="date" placeholder="Select date"></datetime>
</template>

<script>	
	export default {
		mixins: [ VueFormGenerator.abstractField ],
	  data: function(){
			return {
				date: '2017-09-25T07:00:00.000Z'
			}
		}
	}
</script>
