<template>
		<datetime v-model="value" type="datetime" placeholder="Select datetime"></datetime>
</template>

<script>	
	export default {
		mixins: [ VueFormGenerator.abstractField ],
	  data: function(){
			return {
				dateTime: '2017-09-25T07:00:00.000Z'
			}
		}
	}
</script>
