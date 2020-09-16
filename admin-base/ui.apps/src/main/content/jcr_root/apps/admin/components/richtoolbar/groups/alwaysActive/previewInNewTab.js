export default (vm) => {
  return {
    label: 'preview in new tab',
    icon: 'open_in_new',
    iconLib: 'material-icons',
    class: 'always-active separate',
    isActive: () => false,
    cmd: 'previewInNewTab'
  }
}