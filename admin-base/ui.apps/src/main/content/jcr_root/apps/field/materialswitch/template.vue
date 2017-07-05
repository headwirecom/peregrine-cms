<template>
  <!-- Switch -->
  <div class="switch">
    <label :for="getFieldID(schema)">
      {{schema.textOff ? schema.textOff : ''}}
      <input 
	      type="checkbox" 
				v-model="value" 
				:autocomplete="schema.autocomplete" 
				:disabled="disabled" 
				:name="schema.inputName" 
				:id="getFieldID(schema)">
      <span 
      	class="lever"></span>
      {{schema.textOn ? schema.textOn : ''}}
    </label>
  </div>

</template>

<script>	
	export default {
		mixins: [ VueFormGenerator.abstractField ],
		methods: {
			formatValueToField(value) {
				if (value != null && this.schema.valueOn)
					return value == this.schema.valueOn;
				return value;
			},
			formatValueToModel(value) {
				if (value != null && this.schema.valueOn) {
					if (value)
						return this.schema.valueOn;
					else
						return this.schema.valueOff;
				}
				return value;
			}		
		}
	}
</script>
