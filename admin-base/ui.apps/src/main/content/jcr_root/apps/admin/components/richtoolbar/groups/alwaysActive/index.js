import preview from './preview'
import viewport from './viewport'
import previewInNewTab from './previewInNewTab'

export default (vm) => {
  return {
    label: 'always-active',
    noCollapse: true,
    rules: () => vm.showAlwaysActive,
    items: [
      preview(vm),
      previewInNewTab(vm),
      viewport(vm)
    ]
  }
}