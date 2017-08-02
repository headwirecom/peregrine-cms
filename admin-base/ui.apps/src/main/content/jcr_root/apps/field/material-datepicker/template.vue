<template>
  <!-- datepicker -->
  <div class="wrap">
		<input 
			ref="datepicker"
			class="form-control datepicker" 
			type="text"
	    :placeholder="schema.placeholder"
	    :name="schema.inputName" />
	  <button class="btn-flat" v-on:click="showPicker">
	  	<i class="material-icons">date_range</i>
	  </button>
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
		methods: {
			showPicker(ev){
				ev.preventDefault()
				this.picker.open(false)
			},
			isValidDate(dateString) {
			  var regEx = /^\d{4}-\d{2}-\d{2}$/;
			  return dateString.match(regEx) != null;
			}
		},
		mounted() {
			if (window.Picker && window.$ && window.$.fn.pickadate) {
				const options = Object.assign({}, this.schema.options, {
					editable: true
				})
				let input = $(this.$refs.datepicker).pickadate(options)
				this.picker = input.pickadate('picker')
				this.picker.on({
				  // open: () => {
				  //   console.log('open')
				  // },
				  close: () => {
				  	console.log('close')
				  	// adding focus to input prevents autoopening of picker (if the option: editable  === true)
				  	this.$refs.datepicker.focus()
				  },
				  // render: () => {
				  //   console.log('render')
				  // },
				  // stop: () => {
				  //   console.log('stop')
				  // },
				  set: context => {
				    console.log('set date:', context)
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
					if(this.value){
						// regex to check string is four numbers - two numbers - two numbers 
						if (this.isValidDate(this.value)) {
							// console.log("this.value: ", this.value)
							let dateString = this.value
							let dateParts = dateString.split('-')
							this.picker.set('select', new Date(dateParts[0], dateParts[1] - 1, dateParts[2]))
						} else {
							console.warn('model date format must be yyyy-mm-dd')
						}
					}
				})
			}
			
		}
	}
</script>
