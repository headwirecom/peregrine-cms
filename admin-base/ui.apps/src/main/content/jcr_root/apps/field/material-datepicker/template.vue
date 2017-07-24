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
						// TODO: better check that value is a date string (yyyy-mm-dd)
						if (isNaN(this.value)) {
							let dateString = this.value
							let dateParts = dateString.split('-')
							let dateObject = new Date(dateParts[0], dateParts[1] - 1, dateParts[2])
							let day = dateObject.getDate()
							let month = dateObject.toLocaleString("en-us", { month: "long" })
							let year = dateObject.getFullYear()
							let formatedDate = `${day} ${month}, ${year}`
							this.$el.value = formatedDate
						} else {
							console.warn('model must be a date string')
						}
					}
				})
			}
			
		}
	}
</script>
