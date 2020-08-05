export default (vm) => {
  return {
    label: 'preview',
    icon: 'visibility',
    iconLib: 'material-icons',
    class: 'always-active separate',
    isActive: () => vm.preview === 'preview',
    cmd: 'preview'
  }
}