<template>
  <!-- timepicker -->
  <div class="wrap clearfix">
		<input 
			v-if 				 ="!schema.preview"
			ref 				 ="timepicker"
			class        ="form-control timepicker" 
			type         ="text"
			:disabled    ="disabled"
			:placeholder ="schema.placeholder"
			:name        ="schema.inputName" />
		<p v-else>{{timeFromModel()}}</p>
	</div>
</template>

<script>	
	/* TODO: clear model when using 'clear' button in UI */
	export default {
		mixins: [ VueFormGenerator.abstractField ],
		created() {
			// create date object from time string
			if(this.value && this.isValidTime(this.value)){
				let time = this.normalizeTimeZone(this.value)
				// create date object
				let tempDate = new Date()
				// update datobject with time from this.value
				let tempDateString = tempDate.toJSON()
				let today = tempDateString.substring(0, tempDateString.indexOf('T'))
				this.dateTime = new Date(today + 'T' + time)
			}
		},
		mounted() {
			if(!this.schema.preview) this.init()
		},
		watch: {
			schema: function (newSchema) {
				if(!this.schema.preview) this.init()
			}
		},
		data () {
			return {
				dateTime: null
			}
		},
		methods: {
			init(){
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
			normalizeTimeZone(timeString){
				let indexTimeEnding = timeString.lastIndexOf('.')
				let timeEnding = timeString.substring(indexTimeEnding + 1)
				let indexDash = timeEnding.lastIndexOf('-')
				if(indexDash > -1){
					let timeZone = timeEnding.substring(indexDash + 1)
					if(timeZone.length <=2){
						timeString = timeString + '00'
					}
				}
				return timeString
			},
			timeFromModel(){
				if(this.dateTime !== null) {
					let time = this.dateTime.toLocaleTimeString([], { hour: '2-digit', minute:'2-digit' })
					let timeParts = time.split(':')
					let hour = timeParts[0]
					if(hour.length === 1){
						time = '0' + time
					}
					let prettyTime = time.replace(/\s+/g,'')
					return prettyTime
				} else {
					console.warn('model must be a date string with format  YYYY-MM-DDTHH:MM:SS.000Z')
				}
			},
			isValidTime(timeString) {
				if(!timeString){
					console.warn('Value is undefined. Are you sure the model property exists?')
					return false
				}
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
