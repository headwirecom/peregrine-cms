<template>
	<div class="datetime-input-wrapper">
		<datetime
      v-model="sanitizedValue"
        type="date"
        placeholder="Select date"
      />
		<button
			:disabled="!value || schema.readonly"
			@click="clearValue"
			type="button"
			class="clear-button btn-flat"
			title="Clear date"
		>
			<i class="icon material-icons">delete</i>
		</button>
	</div>
</template>

<script>
	export default {
		mixins: [ VueFormGenerator.abstractField ],
			computed: {
			sanitizedValue: {
				get () {
					return this.value ? this.value : ''
				},
				set (newValue) {
					this.value = newValue
				}
			}
		},
		methods: {
			clearValue() {
              this.value = "";
			}
		}
	}
</script>

<style scoped>
.icon {
  color: inherit;
  width: 21px;
}
button {
  height: 100%;
}
</style>
