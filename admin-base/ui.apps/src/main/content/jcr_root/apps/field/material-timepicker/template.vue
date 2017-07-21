<template>
  <!-- timepicker -->
	<input 
		class="form-control timepicker" 
		type="text"
    :value="value"
    :disabled="disabled"
    :placeholder="schema.placeholder"
    :name="schema.inputName" />
</template>

<script>	
	export default {
		mixins: [ VueFormGenerator.abstractField ],
		mounted() {
			if (window.Picker && window.$ && window.$.fn.pickatime) {
				var options = Object.assign({}, this.schema.options, {
          afterDone: () => { this.value = this.$el.value }
				})
				this.picker = $(this.$el).pickatime(options)
			} else {
				console.warn("jQuery or Materialize.js v0.99 is missing.");
			}
			
		},
		data: function(){
			return {
				picker: null
			}
		}
	}
</script>
