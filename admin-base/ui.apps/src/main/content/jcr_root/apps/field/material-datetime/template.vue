<template>
	<div class="wrapper">
		<p v-if="schema.readonly">{{prettyTimeDate(value)}}</p>
		<template v-else-if="!schema.preview">
			<div class="date-wrapper">
				<input ref="datepicker" class="form-control datepicker" type="date" :placeholder="schema.datePlaceholder" />
				<button ref="showPickerBtn" class="btn-flat" v-on:click="showPicker">
					<i class="material-icons">date_range</i>
				</button>
			</div>
			<div class="time-wrapper">
				<input ref="timepicker" class="timepicker" type="text" :placeholder="schema.timePlaceholder" />
			</div>
		</template>
		<p v-else>{{prettyTimeDate(value)}}</p>
	</div>
</template>

<script>	
	export default {
		mixins: [ VueFormGenerator.abstractField ],
		created() {
			// use a non-reactive static object
	    Object.assign(this, this.$options.extra || {})
	  },
		extra: {
	    picker: null,
	  },
	  data: function(){
			return {
				dateTime: null
			}
		},
		mounted() {
			if (window.Picker && window.$ && !this.schema.preview) {
				/* Date Picker */
				if(window.$.fn.pickadate){
					const dateOptions = Object.assign({}, this.schema.options)
					let input = $(this.$refs.datepicker).pickadate(dateOptions)
					this.picker = input.pickadate('picker')
					this.picker.on({
					  close: () => {
					  	// adding focus to diff element prevents auto-opening of picker
					  	this.$refs.showPickerBtn.focus()
					  },
					  set: context => {
					  	if(context.select){
					    	this.dateTime = new Date(context.select)
					    } else if(context.clear == null){
					    	this.dateTime = ''
					    }
					    this.value = this.dateTime.toJSON()
					  }
					})
					/* set inital dateTime from model */
					if(this.isValidDateTime(this.value)){
						this.dateTime = Date.parse(this.value)
						this.$nextTick(function () {
							this.picker.set('select', this.dateTime)
						})
					} else {
						console.warn('model date format must be "yyyy-mm-ddThh:mm:ss.000Z"')
					}
				} else {
					console.warn("jQuery pickadate method does not exist.")
				}

				/* Time Picker */
				if(window.$.fn.pickatime){
					const timeOptions = {
						twelvehour: true,
						init: () => { 
	              this.$refs.timepicker.value = this.timeFromModel()
	          },
	          afterDone: () => { 
	          	this.value = this.modelFromTime()
	          }
					}
					if(this.value){
						// set time in human readable format from a complete date string
					}
					$(this.$refs.timepicker).pickatime(timeOptions)
				} else {
					console.warn("jQuery pickatime method does not exist.")
				}
			} else {
				console.warn("jQuery or Materialize.js v0.99 is missing.")
			}
			
		},
		methods: {
			showPicker(ev){
				ev.preventDefault()
				this.picker.open(false)
			},
			isValidDateTime(dateString){
				if(!dateString){
					console.warn('Value is undefined. Are you sure the model property exists?')
					return false
				}
				let dateParts = dateString.split('T')
				let date = dateParts[0]
				let time = dateParts[1]
				if(this.isValidDate(date) && this.isValidTime(time)){
					return true
				} else {
					return false
				}
			},
			isValidDate(dateString) {
			  var regEx = /^\d{4}-\d{2}-\d{2}$/;
			  return dateString.match(regEx) != null;
			},
			isValidTime(timeString) {
				var regEx = /^(?:(?:([01]?\d|2[0-3]):)?([0-5]?\d):)?([0-5]?\d)$/;
				let firstPart = timeString.substring(0, timeString.indexOf('.'))
				let lastPart = timeString.substring(timeString.indexOf('.') + 1)
				// check timeString ends with '0000Z' and is format hh:mm:ss
				if(firstPart.match(regEx) != null){
					return true
				} else {
					return false
				}
			},
			modelFromTime(){
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
				if(this.dateTime instanceof Date === false) {
					this.dateTime = new Date()
					// update date picker value to today
					//this.$refs.datepicker.value = this.dateTime
				}
				this.dateTime.setHours(hours)
				this.dateTime.setMinutes(minutes)
				this.dateTime.setSeconds(0)
				this.dateTime.setMilliseconds(0)
				return this.dateTime.toJSON()
			},
			timeFromModel(){
				if(this.isValidDateTime(this.value)) {
					let indexT = this.value.lastIndexOf('T')
					let timeString = this.value.substring(indexT + 1)
					timeString = timeString.split('-')[1]
					let today = new Date().toJSON()
					let fauxDate = today.substring(0, today.indexOf('T'))
					// create date object so localization is handled natively
					let d = new Date(fauxDate + 'T' + timeString)
					let time = d.toLocaleTimeString([], { hour: '2-digit', minute:'2-digit' })
					let timeParts = time.split(':')
					let hour = timeParts[0]
					if(hour.length === 1){
						time = '0' + time
					}
					return time.replace(/\s+/g,'')
				} else {
					console.warn('model must be a date string with format  YYYY-MM-DDTHH:MM:SS.000Z')
				}
			},

			prettyTimeDate(value) {
				const timezone = value.match(/-\d+$/)[0]
				const formatted = value.replace(/\-\d+$/, 'Z')
				const initial = new Date(formatted)
				const d = new Date(initial.getTime() - 3600000*(timezone))
				const time = d.toLocaleTimeString([], { hour: '2-digit', minute:'2-digit' })
				const date = `${d.getUTCMonth()+1}/${d.getDate()}/${d.getFullYear()}`
				return `${date} ${time}`;
			},

		}
	}
</script>
