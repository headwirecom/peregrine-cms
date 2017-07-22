<template>
  <!-- datepicker -->
	<input 
		class="form-control datepicker" 
		type="date"
    :disabled="disabled"
    :placeholder="schema.placeholder"
    :name="schema.inputName" />
</template>

<script>	
	/* TODO: local date format  at midnight (00:00:00) */
	export default {
		mixins: [ VueFormGenerator.abstractField ],
		mounted() {
			if (window.Picker && window.$ && window.$.fn.pickadate) {
				const options = Object.assign({}, this.schema.options, {
				  onSet: context => {
				  	if(context.select){
					    const fullDate = new Date(context.select).toJSON()
					    // get everything before the "T" in the date ( we dont want the time)
					    let date = fullDate.substring(0, fullDate.indexOf('T'))
					    this.value = date
					  } else if(context.clear == null){
					  	this.value = ''
					  } 
				  }
				})
				this.$nextTick(function () {
					$(this.$el).pickadate(options)
					if(this.value){
						// format Date object into human readable date (21 July, 2017)
						this.$el.value = this.value
					}
				})
			}
			
		}
	}
</script>
