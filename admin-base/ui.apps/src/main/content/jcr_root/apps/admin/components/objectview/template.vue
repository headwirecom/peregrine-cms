<template>
	<admin-components-explorerpreviewcontent
		:model="model"
		:nodeType="NodeType.OBJECT"
		:browserRoot="`${getBasePath()}/objects`"
		:currentPath="`${getBasePath()}/objects`"
		:onDelete="onDelete"
	>
	</admin-components-explorerpreviewcontent>
</template>

<script>
	import { NodeType } from "../../../../../../js/constants";

	export default {
		props: {
			model: {
				type: Object,
				required: true,
			},
			onDelete: {
				type: Function,
				default: (type, path) => new Promise(),
			},
		},
		data() {
			return {
				NodeType: NodeType,
			};
		},
		methods: {
			getBasePath() {
				const view = $perAdminApp.getView();
				let tenant = { name: "example" };
				if (view.state.tenant) {
					tenant = view.state.tenant;
				}
				return `/content/${tenant.name}`;
			},
		},
	};
</script>
<style scoped></style>
