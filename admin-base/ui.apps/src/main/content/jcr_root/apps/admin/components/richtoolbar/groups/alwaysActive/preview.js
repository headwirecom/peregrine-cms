export default (vm) => {
  return {
    label: 'preview',
    icon: 'visibility',
    iconLib: 'material-icons',
    class: 'always-active',
    isActive: () => vm.preview === 'preview',
    cmd: 'preview'
  }
}