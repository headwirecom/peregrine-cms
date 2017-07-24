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
	/* TODO: clear model when using 'clear' button in UI */
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
						// TODO: better check that value is a time string (HH:MM:SS.000Z)
						if(isNaN(this.value)) {
							let timeString = this.value.trim()
							let today = new Date().toJSON()
							let fauxDate = today.substring(0, today.indexOf('T'))
							// create date object with time from model so we can use native date methods
							let d = new Date(fauxDate + 'T' + timeString)
							let time = d.toLocaleTimeString([], { hour: '2-digit', minute:'2-digit' })
							this.$el.value = time
						} else {
							console.warn('model must be a date string')
						}
					}
				})
				
			} else {
				console.warn("jQuery or Materialize.js v0.99 is missing.");
			}	
		}
	}
</script>
