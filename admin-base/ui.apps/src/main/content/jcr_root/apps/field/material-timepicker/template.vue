<template>
  <!-- timepicker -->
	<input 
		class        ="form-control timepicker" 
		type         ="text"
		:disabled    ="disabled"
		:placeholder ="schema.placeholder"
		:name        ="schema.inputName" />
</template>

<script>	
	/* TODO: local time zone date format (everything after t) */
	export default {
		mixins: [ VueFormGenerator.abstractField ],
		mounted() {
			if (window.Picker && window.$ && window.$.fn.pickatime) {
				const options = {
					// TODO: update model to empty string when clicking clear 
					twelvehour: true,
          afterDone: () => { 
          	const parent = this.$el.parentNode
          	let hours = parseInt(parent.querySelector('.clockpicker-span-hours').textContent)
          	let minutes = parseInt(parent.querySelector('.clockpicker-span-minutes').textContent)
          	let ampm = parent.querySelector('.clockpicker-span-am-pm .text-primary').textContent
          	if(ampm === 'AM' && hours === 12) {
          		hours = 0
          	} 
          	if(ampm === 'PM' && hours !== 12) {
          		hours = hours + 12
          	}

          	console.log('hours: ', hours)
          	let date = new Date()
          	date.setHours(hours)
          	date.setMinutes(minutes)
          	date.setSeconds(0)
          	date.setMilliseconds(0)
          	date = date.toJSON()
          	const time = date.substring(date.indexOf('T') + 1)
          	this.value = time
          }
				}
				this.$nextTick(function () {
					$(this.$el).pickatime(options)
					if(this.value){
						// format Date object into human readable date (12:00PM)
						this.$el.value = this.value
					}
				})
				
			} else {
				console.warn("jQuery or Materialize.js v0.99 is missing.");
			}	
		}
	}
</script>
