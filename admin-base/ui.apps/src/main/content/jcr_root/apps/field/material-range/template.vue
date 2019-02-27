<template>

	<vue-form-generator :schema="{...schema,...rangeSchema}" :model="{value}"></vue-form-generator>

</template>

<script>	
	export default {
		mixins: [ VueFormGenerator.abstractField ],
		data: function (){
			return {
				rangeSchema: {
            fields: [{
                type: "input",
								inputType: "range",
								debounceFormatTimeout: 0
            }]
				},
				value: parseInt(this.value)
			}
		}
	}
</script>
