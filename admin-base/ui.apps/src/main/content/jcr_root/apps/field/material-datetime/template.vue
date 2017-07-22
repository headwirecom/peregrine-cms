<template>
  <div class="wrapper">
		<input 
			ref="datepicker"
			class="form-control datepicker" 
			type="date" 
			:placeholder="schema.datePlaceholder" />
	  <input 
	  	ref="timepicker" 
			class="timepicker" 
			type="text" 
			:placeholder="schema.timePlaceholder"/>
	</div>
</template>

<script>	
	export default {
		mixins: [ VueFormGenerator.abstractField ],
		mounted() {
			if (window.Picker && window.$) {
				this.$nextTick(function () {
					if(window.$.fn.pickadate){
						const dateOptions = Object.assign({}, this.schema.dateOptions, {
						  onSet: context => {
						  	if(context.select){
						    	this.dateTime = new Date(context.select)
						    } else if(context.clear == null){
						    	this.dateTime = ''
						    }
						    this.value = this.dateTime
						  }
						})
						if(this.value){
							// set date in human readable format from a complete date string
						}
						$(this.$refs.datepicker).pickadate(dateOptions)
					} else {
						console.warn("jQuery pickadate method does not exist.")
					}

					if(window.$.fn.pickatime){
						const timeOptions = {
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
		          	if(this.dateTime instanceof Date === false) {
		          		this.dateTime = new Date()
		          		// update date picker value to today
		          		//this.$refs.datepicker.value = this.dateTime
		          	}
		          	this.dateTime.setHours(hours)
		          	this.dateTime.setMinutes(minutes)
		          	this.dateTime.setSeconds(0)
		          	this.dateTime.setMilliseconds(0)
		          	this.value = this.dateTime.toJSON()
		          }
						}
						if(this.value){
							// set time in human readable format from a complete date string
						}
						$(this.$refs.timepicker).pickatime(timeOptions)
					} else {
						console.warn("jQuery pickatime method does not exist.")
					}
				})
			} else {
				console.warn("jQuery or Materialize.js v0.99 is missing.")
			}
			
		},
		data: function(){
			return {
				dateTime: null
			}
		}
	}
</script>
