<template>
  <!-- timepicker -->
	<input 
		ref 				 ="timepicker"
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
					init: () => { 
              // console.log("init timepicker")
              this.$refs.timepicker.value = this.timeFromModel()
          },
          // beforeShow: () => {
          //     console.log("before show")
          // },
          // afterShow: () => {
          //     console.log("after show")
          // },
          // beforeHide: () => {
          //     console.log("before hide")
          // },
          // afterHide: () => {
          //     console.log("after hide")
          // },
          // beforeHourSelect: () => {
          //     console.log("before hour selected")
          // },
          // afterHourSelect: () => {
          //     console.log("after hour selected")
          // },
          // beforeDone: () => {
          //     console.log("before done")
          // },
          afterDone: () => { 
          	this.value = this.modelFromTime()
          }
				}
				$(this.$refs.timepicker).pickatime(options)
				
			} else {
				console.warn("jQuery or Materialize.js v0.99 is missing.");
			}	
		},
		methods: {
			modelFromTime(){
				const parent = this.$refs.timepicker.parentNode
      	let hours = parseInt(parent.querySelector('.clockpicker-span-hours').textContent)
      	let minutes = parseInt(parent.querySelector('.clockpicker-span-minutes').textContent)
      	let ampm = parent.querySelector('.clockpicker-span-am-pm .text-primary').textContent
      	if(ampm === 'AM' && hours === 12) {
      		hours = 0
      	} 
      	if(ampm === 'PM' && hours !== 12) {
      		hours = hours + 12
      	}
				// create date object so localization is handled natively
      	let date = new Date()
      	date.setHours(hours)
      	date.setMinutes(minutes)
      	date.setSeconds(0)
      	date.setMilliseconds(0)
      	date = date.toJSON()
      	let time = date.substring(date.indexOf('T') + 1)
      	return time
			},
			timeFromModel(){
				if(this.isValidTime(this.value)) {
					let timeString = this.value.trim()
					let today = new Date().toJSON()
					let fauxDate = today.substring(0, today.indexOf('T'))
					// create date object so localization is handled natively
					let d = new Date(fauxDate + 'T' + timeString)
					let time = d.toLocaleTimeString([], { hour: '2-digit', minute:'2-digit' })
					// console.log('timeFromModel: ', time)
					let timeParts = time.split(':')
					let hour = timeParts[0]
					if(hour.length === 1){
						time = '0' + time
					}
					return time.replace(/\s+/g,'')
				} else {
					console.warn('model must be a date string with format HH:MM:SS.000Z')
				}
			},
			isValidTime(timeString) {
				var regEx = /^(?:(?:([01]?\d|2[0-3]):)?([0-5]?\d):)?([0-5]?\d)$/;
				let firstPart = timeString.substring(0, timeString.indexOf('.'))
				let lastPart = timeString.substring(timeString.indexOf('.') + 1)
				// check timeString ends with '0000Z' and is format hh:mm:ss
				if(lastPart === '000Z' && firstPart.match(regEx) != null){
					return true
				} else {
					return false
				}
			}
		}
	}
</script>
