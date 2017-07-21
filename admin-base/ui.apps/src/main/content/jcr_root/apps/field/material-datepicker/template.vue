<template>
  <!-- datepicker -->
	<input 
		class="form-control datepicker" 
		type="date"
    :value="value"
    :disabled="disabled"
    :placeholder="schema.placeholder"
    :name="schema.inputName" />
</template>

<script>	
	export default {
		mixins: [ VueFormGenerator.abstractField ],
		mounted() {
			if (window.Picker && window.$ && window.$.fn.pickadate) {
				var options = Object.assign({}, this.schema.options, {
				  onSet: context => {
				  	// context is timestamp
				    var pickerValue = this.picker.get()[0].value
				    this.value = pickerValue
				  }
				})
				this.picker = $(this.$el).pickadate(options)
			}
			
		},
		data: function(){
			return {
				picker: null
			}
		}
	}
</script>
